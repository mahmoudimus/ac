package com.atlassian.plugin.connect.plugin.oldscopes.confluence;

import com.atlassian.plugin.connect.api.confluence.ConfluencePermissions;

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
                )
                );
    }
}
