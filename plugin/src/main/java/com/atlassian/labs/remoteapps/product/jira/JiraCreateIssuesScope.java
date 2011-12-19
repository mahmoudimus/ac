package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.RpcEncodedSoapApiScope;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;

import static java.util.Arrays.asList;

/**
 *
 */
public class JiraCreateIssuesScope extends JiraScope
{
    public JiraCreateIssuesScope()
    {
        super(asList(
                "createIssue",
                "createIssueWithParent",
                "createIssueWithParentWithSecurityLevel",
                "createIssueWithSecurityLevel",
                "getComponents",
                "getFieldsForCreate",
                "getIssueTypesForProject",
                "getPriorities",
                "getSecurityLevels",
                "getStatuses",
                "getSubTaskIssueTypesForProject",
                "getVersions"
        ));
    }
}
