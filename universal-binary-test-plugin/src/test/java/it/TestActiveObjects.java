package it;

import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.plugin.remotable.junit.UniversalBinaries;
import com.atlassian.plugin.remotable.junit.UniversalBinariesContainerJUnitRunner;
import com.atlassian.plugin.remotable.test.GeneralPage;
import com.atlassian.plugin.remotable.test.RemotePluginAwarePage;
import com.atlassian.plugin.remotable.test.RemotePluginTestPage;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertTrue;

@RunWith(UniversalBinariesContainerJUnitRunner.class)
@UniversalBinaries(value = "${moduleDir}/target/remotable-plugins-universal-binary-test-plugin.jar")
public final class TestActiveObjects extends AbstractRemotablePluginTest
{
    @Test
    public void testSomething() throws InterruptedException
    {
        product.visit(HomePage.class);
        final RemotePluginAwarePage homePage = product.getPageBinder().bind(GeneralPage.class, "ao", "Active Objects Testing");

        assertTrue(homePage.isRemotePluginLinkPresent());
        RemotePluginTestPage remotePluginTest = homePage.clickRemotePluginLink();
        assertTrue(remotePluginTest.getTitle().contains("Active Objects Testing Page"));
        assertTrue(remotePluginTest.getMessage().contains("AO Success"));
    }
}
