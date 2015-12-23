package com.atlassian.plugin.connect.jira.field;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.config.managedconfiguration.ConfigurationItemAccessLevel;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItem;
import com.atlassian.jira.config.managedconfiguration.ManagedConfigurationItemService;
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

import java.util.List;

import static com.atlassian.plugin.connect.jira.field.CustomFieldSearcherDescriptorFactory.searcherKeyFromCustomFieldTypeKey;

public class RemoteIssueFieldDescriptor extends CustomFieldTypeModuleDescriptorImpl
{
    private final CustomFieldManager customFieldManager;
    private final ProjectManager projectManager;
    private final ManagedConfigurationItemService managedConfigurationItemService;

    private String customFieldTypeKey;

    public RemoteIssueFieldDescriptor(
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
    public void enabled() {
        super.enabled();

        verifyExistOrCreateCustomField();
    }

    private void verifyExistOrCreateCustomField()
    {
        if (!customFieldExists())
        {
            createCustomField();
        }
    }

    private boolean customFieldExists()
    {
        return customFieldManager.getCustomFieldObjects()
                .stream()
                .anyMatch(cf -> cf.getCustomFieldType().getKey().equals(customFieldTypeKey));
    }

    private void createCustomField()
    {
        final List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(true, new Long[0], projectManager);
        final List<IssueType> returnIssueTypes = Lists.newArrayList((IssueType)null);

        String name = getName();
        String desc = getDescription();

        CustomFieldType type = customFieldManager.getCustomFieldType(customFieldTypeKey);
        CustomFieldSearcher searcher = customFieldManager.getCustomFieldSearcher(searcherKeyFromCustomFieldTypeKey(customFieldTypeKey));

        CustomField customField;
        try
        {
            customField = customFieldManager.createCustomField(name, desc, type, searcher,
                    contexts, returnIssueTypes);
            lockField(customField);
        }
        catch (Exception creationException)
        {
            throw new PluginParseException("Exception while trying to create an issue field. ", creationException);
        }
    }

    private ServiceOutcomeImpl<Void> lockField(CustomField field)
    {
        ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(field);
        if(managedConfigurationItem.getConfigurationItemAccessLevel() == ConfigurationItemAccessLevel.LOCKED)
        {
            return ServiceOutcomeImpl.ok(null);
        }

        managedConfigurationItem = managedConfigurationItem.newBuilder()
                .setManaged(true)
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                .setSource(plugin)
                .build();

        ServiceOutcome<ManagedConfigurationItem> resultOutcome = managedConfigurationItemService.updateManagedConfigurationItem(managedConfigurationItem);
        return ServiceOutcomeImpl.from(resultOutcome.getErrorCollection());
    }
}
