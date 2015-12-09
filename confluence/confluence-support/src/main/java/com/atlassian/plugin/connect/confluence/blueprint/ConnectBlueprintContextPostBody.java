package com.atlassian.plugin.connect.confluence.blueprint;

/**
 * Pojo to hold the body POST'ed to the addon's blueprint context url.
 */
final class ConnectBlueprintContextPostBody
{
    String addonKey;
    String blueprintKey;
    String spaceKey;
    String userKey;
    String parentPageId;
}
