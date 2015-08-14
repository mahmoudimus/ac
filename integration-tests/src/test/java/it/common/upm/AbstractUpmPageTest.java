package it.common.upm;

import com.atlassian.fugue.Option;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.InsufficientPermissionsPage;
import com.atlassian.plugin.connect.test.pageobjects.PluginManagerPage;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.upm.pageobjects.PluginManager;
import it.common.iframe.AbstractPageTestBase;
import it.servlet.ConnectAppServlets;
import it.servlet.InstallHandlerServlet;
import org.junit.BeforeClass;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URISyntaxException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

/**
 * Test of addon configure page in Confluence
 */
public abstract class AbstractUpmPageTest extends AbstractPageTestBase
{
    @Override
    protected <T extends Page> void revealLinkIfNecessary(T page)
    {
        ((PluginManager)page).expandPluginRow(pluginKey);
    }

    @Test
    public void pageIsNotAccessibleWithFalseCondition()
    {
        runner.setToggleableConditionShouldDisplay(false);

        login(testUserFactory.basicUser());

        // note we don't check that the configure link isn't displayed due to AC-973

        // directly retrieving page should result in access denied
        InsufficientPermissionsPage insufficientPermissionsPage = product.visit(InsufficientPermissionsPage.class,
                pluginKey, MY_AWESOME_PAGE_KEY);
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString("You do not have the correct permissions"));
        assertThat(insufficientPermissionsPage.getErrorMessage(), containsString(MY_AWESOME_PAGE));
    }
}
