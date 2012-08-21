package it;

import com.atlassian.labs.remoteapps.junit.UniversalBinaries;
import com.atlassian.labs.remoteapps.junit.UniversalBinariesContainerJUnitRunner;
import com.atlassian.labs.remoteapps.test.GeneralPage;
import com.atlassian.labs.remoteapps.test.RemoteAppAwarePage;
import com.atlassian.labs.remoteapps.test.RemoteAppTestPage;
import com.atlassian.pageobjects.page.HomePage;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

@RunWith(UniversalBinariesContainerJUnitRunner.class)
@UniversalBinaries("${moduleDir}/target/remoteapps-universal-binary-sample.jar")
public final class TestActiveObjects extends AbstractRemoteAppTest
{
    @Test
    public void testSomething() throws InterruptedException
    {
        product.visit(HomePage.class);
        final RemoteAppAwarePage homePage = product.getPageBinder().bind(GeneralPage.class, "ao", "Active Objects Testing");

        assertTrue(homePage.isRemoteAppLinkPresent());
        RemoteAppTestPage remoteAppTest = homePage.clickRemoteAppLink();
        assertTrue(remoteAppTest.getTitle().contains("Active Objects Testing Page"));
        assertTrue(remoteAppTest.getMessage().contains("AO Success"));
    }
}
