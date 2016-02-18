package it.confluence;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import javax.servlet.http.HttpServlet;

import static com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean.newBlueprintModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean.newBlueprintTemplateBeanBuilder;

/**
 * Helper for creating a remote blueprint add-on for testing
 */
public final class ConfluenceBlueprintTestHelper
{
    private final String key;
    private final String moduleKey;
    private final String completeKey;
    private final ConnectRunner runner;

    private ConfluenceBlueprintTestHelper(String key, String moduleKey, String completeKey, final ConnectRunner connectRunner) throws Exception
    {
        this.key = key;
        this.runner = connectRunner;
        this.moduleKey = moduleKey;
        this.completeKey = completeKey;
    }

    public static void runWithBlueprintContext(TestedProduct product,
                                               HttpServlet blueprintTemplateServlet,
                                               HttpServlet blueprintContextServlet,
                                               ConfluenceBlueprintTestAction<ConfluenceBlueprintTestHelper> action) throws Exception
    {
        ConfluenceBlueprintTestHelper helper = null;

        try
        {
            helper = withContextModule(AddonTestUtils.randomAddonKey(), blueprintTemplateServlet, blueprintContextServlet, product);
            action.apply(helper);
        }
        finally
        {
            if (helper != null && helper.getRunner() != null)
            {
                helper.getRunner().stopAndUninstall();
            }
        }
    }

    public static void run(TestedProduct product,
                           HttpServlet blueprintTemplateServlet,
                           ConfluenceBlueprintTestAction<ConfluenceBlueprintTestHelper> action)
            throws Exception
    {
        ConfluenceBlueprintTestHelper helper = null;

        try
        {
            helper = withoutContextModule(AddonTestUtils.randomAddonKey(), blueprintTemplateServlet, product);
            action.apply(helper);
        }
        finally
        {
            if (helper != null && helper.getRunner() != null)
            {
                helper.getRunner().stopAndUninstall();
            }
        }
    }

    static ConfluenceBlueprintTestHelper withContextModule(String key,
                                                                  HttpServlet blueprintTemplateServlet,
                                                                  HttpServlet blueprintContextServlet,
                                                                  TestedProduct product) throws Exception
    {
        String moduleKey = "my-blueprint";
        String completeKey = "com.atlassian.plugins.atlassian-connect-plugin:" + ModuleKeyUtils.addonAndModuleKey(key, moduleKey) + "-web-item";

        return new ConfluenceBlueprintTestHelper(key,
                                                 moduleKey,
                                                 completeKey,
                                                 new ConnectRunner(product.getProductInstance().getBaseUrl(), key)
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, new InstallHandlerServlet())
                .addModule("blueprints",
                           newBlueprintModuleBean()
                                   .withName(new I18nProperty("My Blueprint", null))
                                   .withKey(moduleKey)
                                   .withTemplate(newBlueprintTemplateBeanBuilder()
                                                         .withUrl("/template.xml")
                                                         .withBlueprintContextUrl("/context")
                                                         .build())
                                   .build())
                .addRoute("/template.xml", blueprintTemplateServlet)
                .addRoute("/context", blueprintContextServlet)
                .addScope(ScopeName.READ)
                .start());
    }

    static ConfluenceBlueprintTestHelper withoutContextModule(String key,
                                                              HttpServlet blueprintTemplateServlet,
                                                              TestedProduct product) throws Exception
    {
        String moduleKey = "my-blueprint-no-context";
        String completeKey = "com.atlassian.plugins.atlassian-connect-plugin:" + ModuleKeyUtils.addonAndModuleKey(key, moduleKey) + "-web-item";

        return new ConfluenceBlueprintTestHelper(key,
                                                 moduleKey,
                                                 completeKey,
                                                 new ConnectRunner(product.getProductInstance().getBaseUrl(), key)
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, new InstallHandlerServlet())
                .addModule("blueprints",
                           newBlueprintModuleBean()
                                   .withName(new I18nProperty("My Blueprint no context", null))
                                   .withKey(moduleKey)
                                   .withTemplate(newBlueprintTemplateBeanBuilder()
                                                         .withUrl("/template-no-context.xml")
                                                         .build())
                                   .build())
                .addRoute("/template-no-context.xml", blueprintTemplateServlet)
                .addScope(ScopeName.READ)
                .start());
    }

    public ConnectRunner getRunner()
    {
        return runner;
    }

    public String getKey()
    {
        return key;
    }

    public String getModuleKey()
    {
        return moduleKey;
    }

    public String getCompleteKey()
    {
        return completeKey;
    }

    interface ConfluenceBlueprintTestAction<T> {
        void apply(T t) throws Exception;
    }
}
