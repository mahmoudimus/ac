package com.atlassian.plugin.connect.jira.field;

import java.util.List;
import java.util.Optional;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldType;
import com.atlassian.jira.issue.customfields.CustomFieldUtils;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.plugin.customfield.CustomFieldDefaultVelocityParams;
import com.atlassian.jira.plugin.customfield.CustomFieldTypeModuleDescriptorImpl;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.Lists;
import org.dom4j.Element;

import static com.atlassian.plugin.connect.jira.field.CustomFieldSearcherModuleDescriptorFactory.searcherKeyFromCustomFieldTypeKey;

/**
 * This module adds a new custom field type along with a locked custom field instance of this type.
 */
public class ConnectFieldModuleDescriptor extends CustomFieldTypeModuleDescriptorImpl
{
    private final CustomFieldManager customFieldManager;
    private final ProjectManager projectManager;
    private final ManagedConfigurationItemService managedConfigurationItemService;

    private String customFieldTypeKey;

    public ConnectFieldModuleDescriptor(
            JiraAuthenticationContext authenticationContext,
            RendererManager rendererManager,
            ModuleFactory moduleFactory,
            CustomFieldDefaultVelocityParams customFieldDefaultVelocityParams,
            CustomFieldManager customFieldManager, ProjectManager projectManager,
            ManagedConfigurationItemService managedConfigurationItemService)
    {
        super(authenticationContext, rendererManager, moduleFactory, customFieldDefaultVelocityParams);
        this.customFieldManager = customFieldManager;
        this.projectManager = projectManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException
    {
        super.init(plugin, element);
        this.customFieldTypeKey = this.getCompleteKey();
    }

    @Override
    public void enabled()
    {
        super.enabled();

        verifyExistOrCreateCustomField();
    }

    private void verifyExistOrCreateCustomField()
    {
        CustomField customField = getCustomField().orElseGet(this::createCustomField);
        if (!isFieldLocked(customField))
        {
            try
            {
                lockField(customField);
            }
            catch (RuntimeException e)
            {
                //rolling back custom field creation
                removeCustomField(customField);
                throw e;
            }
        }
    }

    private void removeCustomField(final CustomField customField)
    {
        try
        {
            customFieldManager.removeCustomField(customField);
        }
        catch (RemoveException e)
        {
            throw new PluginParseException("Exception while trying to lock an issue field. ", e);
        }
    }

    private Optional<CustomField> getCustomField()
    {
        return customFieldManager.getCustomFieldObjects()
                .stream()
                .filter(cf -> cf.getCustomFieldType().getKey().equals(customFieldTypeKey))
                .findFirst();
    }

    private CustomField createCustomField()
    {
        final List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(true, new Long[0], projectManager);
        final List<IssueType> returnIssueTypes = Lists.newArrayList((IssueType) null);

        String name = getName();
        String desc = getDescription();

        CustomFieldType type = customFieldManager.getCustomFieldType(customFieldTypeKey);
        CustomFieldSearcher searcher = customFieldManager.getCustomFieldSearcher(searcherKeyFromCustomFieldTypeKey(customFieldTypeKey));

        try
        {
            return customFieldManager.createCustomField(
                    name,
                    desc,
                    type,
                    searcher,
                    contexts,
                    returnIssueTypes);
        }
        catch (Exception creationException)
        {
            throw new PluginParseException("Exception while trying to create an issue field. ", creationException);
        }
    }

    private boolean isFieldLocked(CustomField field)
    {
        ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(field);
        return managedConfigurationItem.getConfigurationItemAccessLevel() == ConfigurationItemAccessLevel.LOCKED;
    }

    private void lockField(CustomField field)
    {
        ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(field);
        if (managedConfigurationItem.getConfigurationItemAccessLevel() == ConfigurationItemAccessLevel.LOCKED)
        {
            return;
        }

        managedConfigurationItem = managedConfigurationItem.newBuilder()
                .setManaged(true)
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                .setSource(plugin)
                .build();

        ServiceOutcome<ManagedConfigurationItem> resultOutcome = managedConfigurationItemService.updateManagedConfigurationItem(managedConfigurationItem);
        if (!resultOutcome.isValid())
        {
            throw new PluginParseException("Error while trying to lock field" + resultOutcome.getErrorCollection());
        }
    }
}
