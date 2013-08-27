package com.atlassian.plugin.connect.plugin.product;

import com.atlassian.plugin.connect.spi.Permissions;
import com.atlassian.plugin.connect.spi.permission.scope.AbstractApiScope;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 * Cross-product API Scope for sending e-mails via the host application
 */
public final class SendEmailScope extends AbstractApiScope
{
    public SendEmailScope()
    {
        super(Permissions.SEND_EMAIL,
                new RestApiScopeHelper(asList(
                        new RestApiScopeHelper.RestScope("atlassian-connect", asList("1", "latest"), "/email", asList("post")),
                        new RestApiScopeHelper.RestScope("atlassian-connect", asList("1", "latest"), "/email/flush", asList("get"))
                )));
    }
}
