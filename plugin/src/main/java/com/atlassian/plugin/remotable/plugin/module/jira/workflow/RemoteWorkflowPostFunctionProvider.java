package com.atlassian.plugin.remotable.plugin.module.jira.workflow;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.workflow.function.issue.AbstractJiraFunctionProvider;
import com.atlassian.plugin.remotable.plugin.product.jira.JiraRestBeanMarshaler;
import com.atlassian.webhooks.spi.provider.ConsumerKey;
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

import java.util.List;
import java.util.Map;

/**
 * Workflow post-function executed when the transition is fired. Builds a JSON of an issue and transition and
 * publishes an webhook event.
 */
public class RemoteWorkflowPostFunctionProvider extends AbstractJiraFunctionProvider
{
    private final EventPublisher eventPublisher;
    private final JiraRestBeanMarshaler beanMarshaler;
    private final ConsumerKey consumerKey;

    public RemoteWorkflowPostFunctionProvider(final EventPublisher eventPublisher,
            final JiraRestBeanMarshaler jiraRestBeanMarshaler,
            final ConsumerKey consumerKey)
    {
        this.eventPublisher = eventPublisher;
        this.beanMarshaler = jiraRestBeanMarshaler;
        this.consumerKey = consumerKey;
    }

    @Override
    public void execute(final Map transientVars, final Map args, final PropertySet propertySet) throws WorkflowException
    {
        final JSONObject postFunctionJSON = postFunctionJSON(transientVars);
        eventPublisher.publish(new RemoteWorkflowPostFunctionEvent(consumerKey, postFunctionJSON));
    }

    protected JSONObject postFunctionJSON(final Map<?, ?> transientVars)
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

        return new JSONObject(ImmutableMap.of("issue", issueJSON, "transition", transitionJSON));
    }

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
