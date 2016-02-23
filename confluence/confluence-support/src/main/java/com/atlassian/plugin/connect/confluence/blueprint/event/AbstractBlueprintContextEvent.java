package com.atlassian.plugin.connect.confluence.blueprint.event;

/**
 *
 */
public abstract class AbstractBlueprintContextEvent {
    private final String addonKey;
    private final String moduleKey;
    private final String url;

    public AbstractBlueprintContextEvent(String addonKey, String moduleKey, String url) {
        this.addonKey = addonKey;
        this.moduleKey = moduleKey;
        this.url = url;
    }

    public String getAddonKey() {
        return addonKey;
    }

    public String getModuleKey() {
        return moduleKey;
    }

    public String getUrl() {
        return url;
    }
}
