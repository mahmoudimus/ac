package com.atlassian.plugin.connect.jira.field;

import com.atlassian.plugin.connect.modules.beans.IssueFieldType;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class ConnectFieldMapperTest
{
    @Test
    public void testFieldMapperMapsAllTypes() throws Exception
    {
        ConnectFieldMapper connectFieldMapper = new ConnectFieldMapper();

        for (IssueFieldType issueFieldType : IssueFieldType.values())
        {
            ConnectFieldMapper.ConnectFieldTypeDefinition mapping = connectFieldMapper.getMapping(issueFieldType);
            assertNotNull("Mapping for " + issueFieldType.toString() + " was missing", mapping);
        }
    }
}