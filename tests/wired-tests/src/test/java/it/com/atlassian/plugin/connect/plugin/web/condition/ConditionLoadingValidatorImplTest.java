package it.com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.api.web.condition.ConditionLoadingValidator;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean.newSingleConditionBean;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

@RunWith(AtlassianPluginsTestRunner.class)
public class ConditionLoadingValidatorImplTest
{

    private ConditionLoadingValidator conditionLoadingValidator;
    private PluginAccessor pluginAccessor;

    private ShallowConnectAddonBean addon = ConnectAddonBean.newConnectAddonBean().build();
    private ConnectModuleMeta moduleMeta = new ConnectModuleMeta("someModules", ModuleBean.class) {};

    public ConditionLoadingValidatorImplTest(ConditionLoadingValidator conditionLoadingValidator,
            PluginAccessor pluginAccessor)
    {
        this.conditionLoadingValidator = conditionLoadingValidator;
        this.pluginAccessor = pluginAccessor;
    }

    @Test
    public void shouldIgnoreUnresolvedCondition() throws ConnectModuleValidationException
    {
        validate(newCondition("unmapped_condition"));
    }

    @Test
    public void shouldRejectConditionOnInstantiationFailure() throws ConnectModuleValidationException
    {
        String errorMessage = "The condition fail-on-instantiation (NonInstantiableCondition) could not be loaded";
        validateExpectingValidationException(newCondition("fail-on-instantiation"), errorMessage);
    }

    @Test
    public void shouldRejectConditionOnInitializationFailure() throws ConnectModuleValidationException
    {
        String errorMessage = "Invalid parameters provided for condition feature_flag (DarkFeatureEnabledCondition)";
        validateExpectingValidationException(newCondition("feature_flag"), errorMessage);
    }

    @Test
    public void shouldAcceptValidConditionWithoutParameters() throws ConnectModuleValidationException
    {
        validate(newCondition("user_is_logged_in"));
    }

    @Test
    public void shouldAcceptValidConditionWithParameters() throws ConnectModuleValidationException
    {
        validate(newSingleConditionBean().withCondition("user_is_logged_in").withParam("featureKey", "some-feature").build());
    }

    private void validate(SingleConditionBean conditionBean) throws ConnectModuleValidationException
    {
        conditionLoadingValidator.validate(getConnectPlugin(), addon, moduleMeta, singletonList(conditionBean));
    }

    private Plugin getConnectPlugin()
    {
        return pluginAccessor.getPlugin(ConnectPluginInfo.getPluginKey());
    }

    private SingleConditionBean newCondition(String condition)
    {
        return newSingleConditionBean().withCondition(condition).build();
    }

    private void validateExpectingValidationException(SingleConditionBean conditionBean, String message)
    {
        try
        {
            validate(conditionBean);
            fail("Expected " + ConnectModuleValidationException.class.getSimpleName() + " with message " + message);
        }
        catch (ConnectModuleValidationException e)
        {
            assertEquals(message, e.getMessage());
        }
    }
}
