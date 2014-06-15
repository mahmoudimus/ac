package com.atlassian.plugin.connect.plugin.module.jira.workflow;

import java.util.List;
import java.util.Map;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.plugin.connect.plugin.product.jira.JiraRestBeanMarshaler;

import com.google.common.base.Predicate;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.StoreException;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.spi.WorkflowStore;

import org.json.JSONObject;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Workflow post-function executed when the transition is fired. Builds a JSON of an issue and transition and
 * publishes an webhook event.
 */
public class RemoteWorkflowPostFunctionProvider extends AbstractJiraFunctionProvider
{
    private final EventPublisher eventPublisher;
    private final JiraRestBeanMarshaler beanMarshaler;

    public RemoteWorkflowPostFunctionProvider(final EventPublisher eventPublisher,
            final JiraRestBeanMarshaler jiraRestBeanMarshaler)
    {
        this.eventPublisher = eventPublisher;
        this.beanMarshaler = jiraRestBeanMarshaler;
    }

    @Override
    public void execute(final Map transientVars, final Map args, final PropertySet propertySet) throws WorkflowException
    {
        final String fullModuleKey = checkNotNull(args.get("full.module.key"), "Expected arg 'full.module.key' is not present").toString();
        final JSONObject postFunctionJSON = postFunctionJSON(transientVars, args);
        eventPublisher.publish(new RemoteWorkflowPostFunctionEvent(fullModuleKey, postFunctionJSON));
    }

    protected JSONObject postFunctionJSON(final Map<?, ?> transientVars, final Map args)
    {
        final WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");
        final Integer actionId = (Integer) transientVars.get("actionId");
        final Issue issue = getIssue(transientVars);
        final WorkflowDescriptor workflowDescriptor = (WorkflowDescriptor) transientVars.get("descriptor");
        final ActionDescriptor actionDescriptor = workflowDescriptor.getAction(actionId);
        final Step createdStep = (Step) transientVars.get("createdStep");
        final WorkflowStore workflowStore = (WorkflowStore) transientVars.get("store");

        final ImmutableMap.Builder<String, Object> transitionBuilder = ImmutableMap.builder();
        transitionBuilder.put("workflowId", entry.getId())
                .put("workflowName", entry.getWorkflowName())
                .put("transitionId", actionId)
                .put("transitionName", actionDescriptor.getName())
                .put("from_status", findPreviousStatus(createdStep, entry, workflowStore, workflowDescriptor))
                .put("to_status", createdStep.getStatus());

        final JSONObject transitionJSON = new JSONObject(transitionBuilder.build());
        final JSONObject issueJSON = beanMarshaler.getRemoteIssue(issue);
        String configJson = (String) args.get(RemoteWorkflowFunctionPluginFactory.STORED_POSTFUNCTION_CONFIG);
        final JSONObject configuration = new JSONObject(ImmutableMap.of("value", configJson));
        return new JSONObject(ImmutableMap.of("issue", issueJSON, "transition", transitionJSON, "configuration", configuration));
    }

    @SuppressWarnings("unchecked")
    private String findPreviousStatus(final Step currentStep,
            final WorkflowEntry entry,
            final WorkflowStore workflowStore,
            final WorkflowDescriptor workflowDescriptor)
    {
        if (currentStep.getPreviousStepIds().length == 0)
        {
            return "";
        }
        final long previousStepId = currentStep.getPreviousStepIds()[0];
        try
        {
            final List<Step> historySteps = workflowStore.findHistorySteps(entry.getId());
            final Step previousStep = Iterables.find(historySteps, new Predicate<Step>()
            {

                @Override
                public boolean apply(final Step step)
                {
                    return step.getId() == previousStepId;
                }
            }, null);

            return workflowDescriptor.getStep(previousStep.getStepId()).getName();
        }
        catch (StoreException e)
        {
            return "";
        }
    }

}
