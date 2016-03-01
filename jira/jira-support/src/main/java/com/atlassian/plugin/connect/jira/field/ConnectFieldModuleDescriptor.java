package com.atlassian.plugin.connect.jira.field;

import com.atlassian.jira.bc.ServiceResult;
import com.atlassian.jira.bc.ServiceResultImpl;
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
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollections;
import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.collect.Lists;
import org.dom4j.Element;
import org.ofbiz.core.entity.GenericEntityException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

import static com.atlassian.plugin.connect.jira.field.CustomFieldSearcherModuleDescriptorFactory.searcherKeyFromCustomFieldTypeKey;

/**
 * This module adds a new custom field type along with a locked custom field instance of this type.
 */
public class ConnectFieldModuleDescriptor extends CustomFieldTypeModuleDescriptorImpl {
    private static final Logger log = LoggerFactory.getLogger(ConnectFieldModuleDescriptor.class);

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
            ManagedConfigurationItemService managedConfigurationItemService) {
        super(authenticationContext, rendererManager, moduleFactory, customFieldDefaultVelocityParams);
        this.customFieldManager = customFieldManager;
        this.projectManager = projectManager;
        this.managedConfigurationItemService = managedConfigurationItemService;
    }

    @Override
    public void init(final Plugin plugin, final Element element) throws PluginParseException {
        super.init(plugin, element);
        this.customFieldTypeKey = this.getCompleteKey();
    }

    @Override
    public void enabled() {
        super.enabled();
        ensureLockedCustomFieldInstanceExists();
    }

    private void ensureLockedCustomFieldInstanceExists() {
        getOrCreateFieldInstance().filter(field -> !isFieldLocked(field)).ifPresent(unlockedField -> {
            ServiceResult lockOutcome = lockField(unlockedField);
            if (!lockOutcome.isValid()) {
                removeCustomField(unlockedField);
                instanceCreationFail(lockOutcome.getErrorCollection());
            }
        });
    }

    private Optional<CustomField> getOrCreateFieldInstance() {
        Optional<CustomField> customField = getCustomField();
        if (!customField.isPresent()) {
            customField = createCustomField();
        }
        return customField;
    }

    private void removeCustomField(final CustomField customField) {
        try {
            customFieldManager.removeCustomField(customField);
        } catch (RemoveException e) {
            instanceCreationFail(e);
        }
    }

    private Optional<CustomField> getCustomField() {
        return customFieldManager.getCustomFieldObjects()
                .stream()
                .filter(cf -> cf.getCustomFieldType().getKey().equals(customFieldTypeKey))
                .findFirst();
    }

    private Optional<CustomField> createCustomField() {
        final List<JiraContextNode> contexts = CustomFieldUtils.buildJiraIssueContexts(true, new Long[0], projectManager);
        final List<IssueType> returnIssueTypes = Lists.newArrayList((IssueType) null);

        String name = getName();
        String desc = getDescription();

        CustomFieldType type = customFieldManager.getCustomFieldType(customFieldTypeKey);
        CustomFieldSearcher searcher = customFieldManager.getCustomFieldSearcher(searcherKeyFromCustomFieldTypeKey(customFieldTypeKey));

        try {
            return Optional.of(customFieldManager.createCustomField(
                    name,
                    desc,
                    type,
                    searcher,
                    contexts,
                    returnIssueTypes));
        } catch (GenericEntityException e) {
            instanceCreationFail(e);
            return Optional.empty();
        }
    }

    private boolean isFieldLocked(CustomField field) {
        ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(field);
        return managedConfigurationItem.getConfigurationItemAccessLevel() == ConfigurationItemAccessLevel.LOCKED;
    }

    private ServiceResult lockField(CustomField field) {
        ManagedConfigurationItem managedConfigurationItem = managedConfigurationItemService.getManagedCustomField(field);
        if (managedConfigurationItem.getConfigurationItemAccessLevel() == ConfigurationItemAccessLevel.LOCKED) {
            return new ServiceResultImpl(ErrorCollections.empty());
        }

        managedConfigurationItem = managedConfigurationItem.newBuilder()
                .setManaged(true)
                .setConfigurationItemAccessLevel(ConfigurationItemAccessLevel.LOCKED)
                .setSource(plugin)
                .build();

        return managedConfigurationItemService.updateManagedConfigurationItem(managedConfigurationItem);
    }

    private void instanceCreationFail(Exception ex) {
        log.error("Exception when trying to create and lock issue field instance", ex);
    }

    private void instanceCreationFail(ErrorCollection errors) {
        log.error("Exception when trying to create and lock issue field instance. " + errors.toString());
    }
}
