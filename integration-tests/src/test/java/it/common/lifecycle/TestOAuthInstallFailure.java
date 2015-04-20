package it.common.lifecycle;

import com.atlassian.fugue.Option;
import com.atlassian.jwt.exception.JwtIssuerLacksSharedSecretException;
import com.atlassian.jwt.exception.JwtParseException;
import com.atlassian.jwt.exception.JwtUnknownIssuerException;
import com.atlassian.jwt.exception.JwtVerificationException;
import com.atlassian.plugin.connect.modules.beans.builder.ConnectPageModuleBeanBuilder;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.test.AddonTestUtils;
import com.atlassian.plugin.connect.test.pageobjects.ConnectAddOnEmbeddedTestPage;
import com.atlassian.plugin.connect.test.pageobjects.LinkedRemoteContent;
import com.atlassian.plugin.connect.test.server.ConnectRunner;
import com.atlassian.upm.pageobjects.PluginManager;
import it.modules.ConnectAsserts;
import it.servlet.ConnectAppServlets;
import it.util.ConnectTestUserFactory;
import it.util.TestUser;
import org.junit.Before;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.security.NoSuchAlgorithmException;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static com.atlassian.plugin.connect.test.pageobjects.RemoteWebItem.ItemMatchingMode.LINK_TEXT;

public class TestOAuthInstallFailure extends TestInstallFailure
{

    @Override
    @Before
    public void setup() throws NoSuchAlgorithmException, IOException
    {
        int query = URL.indexOf("?");
        String route = query > -1 ? URL.substring(0, query) : URL;

        ConnectPageModuleBeanBuilder pageBeanBuilder = newPageBean();
        pageBeanBuilder.withName(new I18nProperty(MY_AWESOME_PAGE, null))
                .withKey(MY_AWESOME_PAGE_KEY)
                .withUrl(URL)
                .withWeight(1234);
        
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), AddonTestUtils.randomAddOnKey())
        .addInstallLifecycle()
        .addUninstallLifecycle()
        .addModule("configurePage", pageBeanBuilder.build())
        .addOAuth()
        .addRoute(route, ConnectAppServlets.helloWorldServlet())
        .addRoute(ConnectRunner.INSTALLED_PATH, installUninstallHandler)
        .addRoute(ConnectRunner.UNINSTALLED_PATH, installUninstallHandler)
        .addScope(ScopeName.ADMIN)
        .disableInstallationStatusCheck();
    }
    
    @Override
    public void assertPageLinkWorks() throws MalformedURLException, URISyntaxException, JwtVerificationException, JwtIssuerLacksSharedSecretException, JwtUnknownIssuerException, JwtParseException
    {
        login(testUserFactory.admin());

        PluginManager page = product.visit(PluginManager.class);
        revealLinkIfNecessary(page);

        LinkedRemoteContent addonPage = connectPageOperations.findConnectPage(LINK_TEXT,
                "Configure",
                Option.<String>none(), awesomePageModuleKey);

        ConnectAddOnEmbeddedTestPage addonContentPage = addonPage.click();

        ConnectAsserts.verifyContainsStandardAddOnQueryParamters(addonContentPage.getIframeQueryParams(),
                product.getProductInstance().getContextPath());
    }

}
