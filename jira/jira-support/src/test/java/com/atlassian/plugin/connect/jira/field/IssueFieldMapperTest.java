package com.atlassian.plugin.connect.jira.field;

import com.atlassian.plugin.connect.modules.beans.IssueFieldType;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class IssueFieldMapperTest
{
    @Test
    public void testFieldMapperMapsAllTypes() throws Exception
    {
        IssueFieldMapper issueFieldMapper = new IssueFieldMapper();

        for (IssueFieldType issueFieldType : IssueFieldType.values())
        {
            IssueFieldMapper.IssueFieldTypeDefinition mapping = issueFieldMapper.getMapping(issueFieldType);
            assertNotNull("Mapping for " + issueFieldType.toString() + " was missing", mapping);
        }
    }
}