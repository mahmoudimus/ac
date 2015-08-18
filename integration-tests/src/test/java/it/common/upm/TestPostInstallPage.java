//package it.common.upm;
//
//import com.atlassian.fugue.Option;
//import com.atlassian.upm.pageobjects.InstalledPluginDetails;
//import com.atlassian.upm.pageobjects.Link;
//import com.atlassian.upm.pageobjects.PluginManager;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.net.MalformedURLException;
//import java.net.URISyntaxException;
//
///**
//* Test of addon configure page in Confluence
//*/
//public class TestPostInstallPage extends AbstractUpmPageTest
//{
//    private static final String MODULE_NAME = "postInstallPage";
//
//    @BeforeClass
//    public static void startConnectAddOn() throws Exception
//    {
//        startConnectAddOn(MODULE_NAME);
//    }
//
//    @Test
//    public void canClickOnPageLinkAndSeeAddonContents() throws MalformedURLException, URISyntaxException
//    {
//        runCanClickOnPageLinkAndSeeAddonContents(PluginManager.class, Option.some("Get started"), testUserFactory.admin());
//    }
//
//    @Override
//    protected String getModuleType()
//    {
//        return MODULE_NAME;
//    }
//
//    @Override
//    protected Link getLink(InstalledPluginDetails installedPluginDetails)
//    {
//        return installedPluginDetails.getPostInstallLink();
//    }
//}