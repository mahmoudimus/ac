package it.confluence;

import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.servlet.InstallHandlerServlet;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;
import com.atlassian.plugin.util.collect.Consumer;

import static com.atlassian.plugin.connect.modules.beans.BlueprintModuleBean.newBlueprintModuleBean;
import static com.atlassian.plugin.connect.modules.beans.nested.BlueprintTemplateBean.newBlueprintTemplateBeanBuilder;
import static it.confluence.servlet.ConfluenceAppServlets.blueprintTemplateServlet;

public final class ConfluenceBlueprintTestHelper
{
    private final String key;
    private final String moduleKey;
    private final String completeKey;

    private final ConnectRunner runner;

    public static ConfluenceBlueprintTestHelper getInstance(String key, TestedProduct product) throws Exception {
        return new ConfluenceBlueprintTestHelper(key, product);
    }

    private ConfluenceBlueprintTestHelper(String key, TestedProduct product) throws Exception
    {
        this.key = key;
        this.moduleKey = "my-blueprint";
        this.completeKey = "com.atlassian.plugins.atlassian-connect-plugin:" + ModuleKeyUtils.addonAndModuleKey(key,
                moduleKey) + "-web-item";

        this.runner = new ConnectRunner(product.getProductInstance().getBaseUrl(), key)
                .addInstallLifecycle()
                .addRoute(ConnectRunner.INSTALLED_PATH, new InstallHandlerServlet())
                .addModule("blueprints",
                        newBlueprintModuleBean()
                                .withName(new I18nProperty("My Blueprint", null))
                                .withKey(moduleKey)
                                .withTemplate(newBlueprintTemplateBeanBuilder()
                                        .withUrl("/template.xml")
                                        .build())
                                .build())
                .addRoute("/template.xml", blueprintTemplateServlet())
                .addScope(ScopeName.READ)
                .start();
    }

    public static void runInRunner(TestedProduct product, Consumer<ConfluenceBlueprintTestHelper> action)
            throws Exception
    {
        ConfluenceBlueprintTestHelper helper = null;

        try
        {
            helper = getInstance(AddonTestUtils.randomAddonKey(), product);
            action.consume(helper);
        }
        finally
        {
            if (helper != null && helper.getRunner() != null)
            {
                helper.getRunner().stopAndUninstall();
            }
        }
    }

    public ConnectRunner getRunner()
    {
        return runner;
    }

    public String getKey()
    {
        return key;
    }

    public String getCompleteKey()
    {
        return completeKey;
    }
}
