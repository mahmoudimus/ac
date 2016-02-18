package com.atlassian.plugin.connect.confluence.theme;

/**
 * store some hardcoded values to map the existing values of action names, package names and result names to the appropriate override files.
 */
public enum LayoutType {
    main("/decorators/main.vmd",
         "velocity/confluence/theme-support/main.vmd",
         "search", /*TODO: check that search is the dashboard?!*/
         "dashboard",
         "success",
         "/velocity/confluence/theme-support/main.vm"),
    blog("/decorators/blogpost.vmd",
         "velocity/confluence/theme-support/page.vmd",
         "pages",
         "viewpage",
         "blogpost",
         "/velocity/confluence/theme-support/blogpost.vm"),
    page("/decorators/page.vmd",
         "velocity/confluence/theme-support/page.vmd",
         "pages",
         "viewpage",
         "page",
         "/velocity/confluence/theme-support/page.vm");

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
