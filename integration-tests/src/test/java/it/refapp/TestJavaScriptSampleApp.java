package it.refapp;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.remotable.junit.Mode;
import com.atlassian.plugin.remotable.junit.UniversalBinaries;
import com.atlassian.plugin.remotable.junit.UniversalBinariesContainerJUnitRunner;
import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.plugin.remotable.test.OAuthUtils;
import com.atlassian.plugin.remotable.test.RemotePluginAwarePage;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import it.AbstractRemotablePluginTest;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.twdata.pkgscanner.ExportPackageListBuilder;

import static it.TestConstants.BETTY;
import static org.junit.Assert.assertTrue;

@Ignore("Ignoring until we can get rid of UB stuff")
@RunWith(UniversalBinariesContainerJUnitRunner.class)
@UniversalBinaries(value = "${moduleDir}/src/js-sample-app", mode = Mode.CONTAINER)
public final class TestJavaScriptSampleApp extends AbstractRemotablePluginTest
{
    @BeforeClass
    public static void before()
    {
        Logger.getLogger(ExportPackageListBuilder.class).setLevel(Level.ERROR);
    }

    @Test
    public void testContainerSampleGeneralPage()
    {
        product.visit(LoginPage.class).login(BETTY, BETTY, HomePage.class);
        RemotePluginAwarePage page = product.getPageBinder().bind(GeneralPage.class, "general", "Container Sample General");
        assertTrue(page.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = page.clickRemotePluginLink();
        assertTrue(remotePluginTest.getTitle().contains("Container Sample General"));
        Assert.assertEquals("Success", remotePluginTest.getMessage());
        Assert.assertEquals(OAuthUtils.getConsumerKey(), remotePluginTest.getConsumerKey());
        Assert.assertEquals("Betty Admin", remotePluginTest.getFullName());
        Assert.assertEquals(BETTY, remotePluginTest.getUserId());

        // basic tests of the HostHttpClient API
        Assert.assertEquals("200", remotePluginTest.getServerHttpStatus());
        String statusText = remotePluginTest.getServerHttpStatusText();
        assertTrue("OK".equals(statusText));
        String contentType = remotePluginTest.getServerHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain")); // startsWith accounts for possible charset
        Assert.assertEquals(BETTY, remotePluginTest.getServerHttpEntity());

        // basic tests of the RA.request API
        Assert.assertEquals("200", remotePluginTest.getClientHttpStatus());
        statusText = remotePluginTest.getClientHttpStatusText();
        assertTrue("OK".equals(statusText) || "success".equals(statusText)); // differs by jquery version
        contentType = remotePluginTest.getClientHttpContentType();
        assertTrue(contentType != null && contentType.startsWith("text/plain")); // startsWith accounts for possible charset
        Assert.assertEquals(BETTY, remotePluginTest.getClientHttpData());
        Assert.assertEquals(BETTY, remotePluginTest.getClientHttpResponseText());

        // media type tests of the RA.request API
        Assert.assertEquals("{\"name\": \"betty\"}", remotePluginTest.getClientHttpDataJson());
        Assert.assertEquals("<user><name>betty</name></user>", remotePluginTest.getClientHttpDataXml());
    }
}
