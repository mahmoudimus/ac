package it.com.atlassian.plugin.connect.workflow.jira;

import com.atlassian.jira.workflow.JiraWorkflow;
import com.opensymphony.workflow.InvalidWorkflowDescriptorException;
import org.xml.sax.SAXException;

import java.io.IOException;

/**
 * Allows to import XML workflow definitions
 */
public interface WorkflowImporter
{
    /**
     * Imports a workflow based on its previously exported XML definition. If the workflow exists already, the existing
     * workflow will be returned.
     *
     * @param name The name of the workflow
     * @param resourcePath The path of the workflow XML file in the /resources directory
     * @return The newly created workflow (or the existing one if a workflow with the same name exists already)
     * @throws SAXException
     * @throws IOException
     * @throws InvalidWorkflowDescriptorException
     */
    JiraWorkflow importWorkflow(String name, String resourcePath) throws SAXException, IOException, InvalidWorkflowDescriptorException;
}
