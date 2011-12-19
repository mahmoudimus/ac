package com.atlassian.labs.remoteapps.product.jira;

import com.atlassian.labs.remoteapps.modules.permissions.scope.ApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.JsonRpcApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.RpcEncodedSoapApiScope;
import com.atlassian.labs.remoteapps.modules.permissions.scope.XmlRpcApiScope;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 *
 */
public class JiraReadUsersAndGroupsScope extends JiraScope
{
    public JiraReadUsersAndGroupsScope()
    {
        super(asList(
            "getUser",
            "getGroup"
        ));
    }
}
