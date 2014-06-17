package it.com.atlassian.plugin.connect.workflow.jira;

import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.workflow.ConfigurableJiraWorkflow;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import org.xml.sax.SAXException;

import java.io.IOException;

public class DefaultWorkflowImporter implements WorkflowImporter
{
    private WorkflowManager workflowManager;
    private JiraAuthenticationContext jiraAuthenticationContext;

    public DefaultWorkflowImporter(WorkflowManager workflowManager, JiraAuthenticationContext jiraAuthenticationContext)
    {
        this.workflowManager = workflowManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
    }

    public JiraWorkflow importWorkflow(String name, String resourcePath) throws SAXException, IOException, InvalidWorkflowDescriptorException
    {
        if (workflowManager.workflowExists(name))
        {
            return workflowManager.getWorkflow(name);
        }

        WorkflowDescriptor workflowDescriptor = WorkflowLoader.load(getClass().getResourceAsStream(resourcePath), true);
        ConfigurableJiraWorkflow newWorkflow = new ConfigurableJiraWorkflow(name, workflowDescriptor, workflowManager);
        workflowManager.createWorkflow(jiraAuthenticationContext.getUser(), newWorkflow);

        return newWorkflow;
    }

}
