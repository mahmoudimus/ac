package it.com.atlassian.plugin.connect.workflow.jira;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import org.xml.sax.SAXException;

import java.io.IOException;

public interface WorkflowImporter
{
    JiraWorkflow importWorkflow(String name, String resourcePath) throws SAXException, IOException, InvalidWorkflowDescriptorException;
}
