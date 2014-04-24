package com.atlassian.plugin.connect.plugin.threeleggedauth;

import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;

import java.util.Collection;

public class NoAgentScopeException extends Exception
{
    public NoAgentScopeException(String addOnKey, Collection<ScopeName> actualScopes)
    {
        super(String.format("The add-on '%s' cannot request to act on behalf of users because it does not request the '%s' scope. Its requested scopes are: [%s].",
                addOnKey, ScopeName.AGENT, StringUtils.join(actualScopes, ',')));
    }
}
