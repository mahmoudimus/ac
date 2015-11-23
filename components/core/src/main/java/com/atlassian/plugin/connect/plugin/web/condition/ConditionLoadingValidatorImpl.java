package com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.api.web.condition.ConditionClassAccessor;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.util.ConditionUtils;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.WebFragmentHelper;
import com.atlassian.plugin.web.conditions.ConditionLoadingException;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

/**
 * A descriptor fragment validator that asserts that web fragment conditions can be properly loaded.
 */
@Component
@ExportAsDevService
public class ConditionLoadingValidatorImpl implements ConditionLoadingValidator
{

    private ConditionClassAccessor conditionClassAccessor;
    private WebFragmentHelper webFragmentHelper;

    @Inject
    public ConditionLoadingValidatorImpl(ConditionClassAccessor conditionClassAccessor, WebFragmentHelper webFragmentHelper)
    {
        this.conditionClassAccessor = conditionClassAccessor;
        this.webFragmentHelper = webFragmentHelper;
    }

    @Override
    public void validate(Plugin plugin, ShallowConnectAddonBean addon, ConnectModuleMeta<?> moduleMeta, List<ConditionalBean> conditions)
            throws ConnectModuleValidationException
    {
        List<SingleConditionBean> singleConditions = ConditionUtils.getSingleConditionsRecursively(conditions);
        for (SingleConditionBean singleCondition : singleConditions)
        {
            validateCondition(plugin, addon, moduleMeta, singleCondition);
        }
    }

    private void validateCondition(Plugin plugin, ShallowConnectAddonBean addon, ConnectModuleMeta<?> moduleMeta, SingleConditionBean singleCondition)
            throws ConnectModuleValidationException
    {
        Optional<Class<? extends Condition>> optionalConditionClass = conditionClassAccessor.getConditionClassForHostContext(singleCondition);
        if (optionalConditionClass.isPresent())
        {
            Class<? extends Condition> conditionClass = optionalConditionClass.get();
            Condition condition = null;
            try
            {
                condition = webFragmentHelper.loadCondition(conditionClass.getName(), plugin);
            } catch (ConditionLoadingException e)
            {
                String message = String.format("The condition %s (%s) could not be loaded",
                        singleCondition.getCondition(), conditionClass.getSimpleName());
                rethrowAsModuleValidationException(e, addon, moduleMeta, message, null);
            }

            try
            {
                condition.init(singleCondition.getParams());
            }
            catch (PluginParseException e)
            {
                String message = String.format("Invalid parameters provided for condition %s (%s)",
                        singleCondition.getCondition(), conditionClass.getSimpleName());
                rethrowAsModuleValidationException(e, addon, moduleMeta, message,
                        "connect.install.error.invalid.condition.parameters", singleCondition.getCondition(), e.getLocalizedMessage());
            }
            catch (Throwable e)
            {
                String message = String.format("An error occurred when initializing condition %s (%s)",
                        singleCondition.getCondition(), conditionClass.getSimpleName());
                rethrowAsModuleValidationException(e, addon, moduleMeta, message, null);
            }
        }
    }

    private void rethrowAsModuleValidationException(Throwable cause, ShallowConnectAddonBean addon,
            ConnectModuleMeta<?> moduleMeta, String message, String i18nKey, Serializable... i18nParameters)
            throws ConnectModuleValidationException
    {
        ConnectModuleValidationException exception = new ConnectModuleValidationException(
                addon, moduleMeta, message, i18nKey, i18nParameters);
        exception.initCause(cause);
        throw exception;
    }
}
