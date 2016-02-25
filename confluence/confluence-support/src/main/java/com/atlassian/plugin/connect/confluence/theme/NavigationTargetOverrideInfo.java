package com.atlassian.plugin.connect.confluence.theme;

/**
 * store some hardcoded values to map the existing values of action names, package names and result names to the appropriate override files.
 */
public enum NavigationTargetOverrideInfo {
    dashboard("/decorators/main.vmd",
              "velocity/confluence/theme-support/decorators/main.vmd",
              "search",
              "dashboard",
              "success",
              "/velocity/confluence/theme-support/main.vm",
              NavigationTargetName.dashboard),
    blogpost("/decorators/blogpost.vmd",
             "velocity/confluence/theme-support/decorators/blogpost.vmd",
             "pages",
             "viewpage",
             "blogpost",
             "/velocity/confluence/theme-support/blogpost.vm",
             NavigationTargetName.contentview),
    page("/decorators/page.vmd",
         "velocity/confluence/theme-support/decorators/page.vmd",
         "pages",
         "viewpage",
         "page",
         "/velocity/confluence/theme-support/page.vm",
         NavigationTargetName.contentview),
    space("/decorators/space.vmd",
          "velocity/confluence/theme-support/decorators/space.vmd",
          "spaces",
          "viewspace",
          "homepage",
          "/velocity/confluence/theme-support/space.vm",
          NavigationTargetName.spaceview);

    private final String decoratorToOverride;
    private final String decoratorLocation;
    private final String packageToOverride;
    private final String actionToOverride;
    private final String resultToOverride;
    private final String templateLocation;
    private final NavigationTargetName navigationTargetName;

    NavigationTargetOverrideInfo(String decoratorToOverride,
                                 String decoratorLocation,
                                 String packageToOverride,
                                 String actionToOverride,
                                 String resultToOverride,
                                 String templateLocation,
                                 NavigationTargetName navigationTargetName) {
        this.decoratorToOverride = decoratorToOverride;
        this.decoratorLocation = decoratorLocation;
        this.packageToOverride = packageToOverride;
        this.actionToOverride = actionToOverride;
        this.resultToOverride = resultToOverride;
        this.templateLocation = templateLocation;
        this.navigationTargetName = navigationTargetName;
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

    public NavigationTargetName getNavigationTargetName() {
        return navigationTargetName;
    }
}
