package com.atlassian.plugin.connect.plugin.oldscopes.confluence;

import com.atlassian.plugin.connect.api.confluence.ConfluencePermissions;
import com.atlassian.plugin.connect.spi.permission.scope.RestApiScopeHelper;

import static java.util.Arrays.asList;

/**
 * API Scope for Confluence that grants Remotable Plugins the ability to add, edit and remove pages and blogs.
 */
public final class ModifyContentScope extends ConfluenceScope
{
    public ModifyContentScope()
    {
        super(ConfluencePermissions.MODIFY_CONTENT,
                asList(
                        "storeBlogEntry",
                        "addComment",
                        "editComment",
                        "removeComment",
                        "movePageToTopLevel",
                        "movePage",
                        "removePage",
                        "storePage",
                        "updatePage",
                        "setContentPermissions",
                        "purgeFromTrash",
                        "emptyTrash"
                ),
                asList(
                        new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/action", asList("delete", "post", "put")),
                        new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/notification", asList("delete", "post", "put")),
                        new RestApiScopeHelper.RestScope("mywork", asList("1", "latest"), "/task", asList("delete", "post", "put")),
                        new RestApiScopeHelper.RestScope("prototype", asList("1", "latest"), "/content", asList("delete", "post", "put")),
                        new RestApiScopeHelper.RestScope("ui", asList("1", "1.0", "latest"), "/content", asList("delete", "post", "put"))
                )
                );
    }
}
