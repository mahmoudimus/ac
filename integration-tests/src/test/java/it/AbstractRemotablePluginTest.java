package it;

import com.atlassian.confluence.pageobjects.page.admin.ConfluenceAdminHomePage;
import com.atlassian.confluence.pageobjects.page.setup.LicensePage;
import com.atlassian.jira.pageobjects.pages.admin.ViewLicensePage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.plugin.connect.test.junit.HtmlDumpRule;
import com.atlassian.plugin.connect.test.pageobjects.OwnerOfTestedProduct;
import com.atlassian.webdriver.pageobjects.WebDriverTester;

import org.junit.After;
import org.junit.Rule;
import org.openqa.selenium.By;
import org.openqa.selenium.support.pagefactory.ByChained;

import static it.TestConstants.ADMIN_USERNAME;
import static it.TestConstants.BETTY_USERNAME;

public abstract class AbstractRemotablePluginTest
{
    protected final static TestedProduct<WebDriverTester> product = OwnerOfTestedProduct.INSTANCE;
    protected final static String TIMEBOMB_WILDCARD_PLUGIN_LICENSE = "AAACCg0ODAoPeNp1U8uO2jAU3ecrInVXKcjmMRqQsuDhGWiZDCKhrUbdOMkF3CZ25AcMf9+Q8IgLL\n" +
            "LLI9bnn3nN8/CXaGvcFYreNXfw8QGiAeu4ijNw2wh0nBLkDOZv4o9d+5P1a/eh63z8+pt4I4Z/On\n" +
            "CXAFUSHAgKagx+RMJoFr05IAr/8vB5CTiLFPm01kP74WPk9cCewg0wUIJ1E8HWLJprtwNfSQNlkE\n" +
            "hZnYBVPHOSzYPIwoRr8RWd6hdoj6qI15Q+T1CJcGJls6Q1jkZkN46r1tcVNPhVKQ7pSIJWPkROYP\n" +
            "Ab5vq4LHm6AI8loRnh64ZmASiQrNBPcj0BpN6s3dNdCunWfm563Uw7Z0czQCl1td2UeGi2WwGFfH\n" +
            "7xRxjVwypMbL44+Wj6UhcxAibScGEuoBlWNbYw7HsJeu1/3nyWOM1NKl4FIQfnImVOl30TK1gxSH\n" +
            "3eecLf/3EW93hN2BE8hpzyt14tpHgtxoRlVv3OR0Gy4Aa4r106YW39PB00NNYFLSs2ykEzZUq4uN\n" +
            "W/WpiEpq2wlQUSWi+UsJHUWbsdfwvRwM1vWEnKh4aQLn7JuB/lYuWVbM7WFw7051WaPFz833lFrr\n" +
            "7bIKK+8fpcbypmqkzXUGVWKUe6EJr7Gs/m+yre+4n+52PM6Dw+tuPeU/k9UU011m99my+Gjuzxra\n" +
            "+Jfyho52Lh//5qpzTAtAhUAj8L7xWFb4yfemMFIqVBg6p3ZzS8CFDVulZlFOu+THivlIbkZbjF76\n" +
            "WhHX02ok";
    @Rule
    public final HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public final void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }
    
    protected void addPluginLicenses()
    {
        product.visit(LoginPage.class).login(ADMIN_USERNAME, ADMIN_USERNAME, HomePage.class);
        
        if("jira".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            product.visit(ViewLicensePage.class).updateLicense(TIMEBOMB_WILDCARD_PLUGIN_LICENSE);

        }
        else if("confluence".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            product.visit(ConfluenceAdminHomePage.class);
            product.getTester().getDriver().waitUntilElementIsVisible(By.linkText("License Details"));
            product.getTester().getDriver().findElement(By.linkText("License Details")).click();
            product.getTester().getDriver().waitUntilElementIsLocated(By.name("licenseString"));
            product.getTester().getDriver().findElement(By.name("licenseString")).sendKeys(TIMEBOMB_WILDCARD_PLUGIN_LICENSE);
            product.getTester().getDriver().findElement(new ByChained(By.tagName("input"),By.name("update"))).click();
            
        }
    }
}
