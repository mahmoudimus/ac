package com.atlassian.plugin.connect.confluence.theme;

/**
 * store some hardcoded values to map the existing values of action names, package names and result names to the appropriate override files.
 */
public enum LayoutType {
    main("/decorators/main.vmd",
         "velocity/confluence/theme-support/remote-theme-support.vmd",
         "search", /*TODO: check that search is the dashboard?!*/
         "dashboard",
         "success",
         "/theme-support/remote-theme-support.vmd"),
    page("/decorators/page.vmd",
         "velocity/confluence/theme-support/remote-theme-support.vmd",
         "pages",
         "viewpage",
         "page",
         "/theme-support/remote-theme-support.vmd");

    private final String decoratorToOverride;
    private final String decoratorLocation;
    private final String packageToOverride;
    private final String actionToOverride;
    private final String resultToOverride;
    private final String templateLocation;

    LayoutType(String decoratorToOverride,
               String decoratorLocation,
               String packageToOverride,
               String actionToOverride,
               String resultToOverride,
               String templateLocation) {
        this.decoratorToOverride = decoratorToOverride;
        this.decoratorLocation = decoratorLocation;
        this.packageToOverride = packageToOverride;
        this.actionToOverride = actionToOverride;
        this.resultToOverride = resultToOverride;
        this.templateLocation = templateLocation;
    }

    public String getDecoratorToOverride() {
        return decoratorToOverride;
    }

    public String getDecoratorLocation() {
        return decoratorLocation;
    }

    public String getPackageToOverride() {
        return packageToOverride;
    }

    public String getActionToOverride() {
        return actionToOverride;
    }

    public String getResultToOverride() {
        return resultToOverride;
    }

    public String getTemplateLocation() {
        return templateLocation;
    }
}
