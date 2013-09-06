package it;

import com.atlassian.confluence.pageobjects.page.admin.ConfluenceAdminHomePage;
import com.atlassian.confluence.pageobjects.page.setup.LicensePage;
import com.atlassian.jira.pageobjects.pages.admin.ViewLicensePage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.AdminHomePage;
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
    
    protected static final String JIRA_LICENSE = "AAABgw0ODAoPeNp1kkFPwkAQhe/9FZt4LmmrYiTpAegiIAKBgoZ42W4HWF12m9kW5d9bC41tg9e3b958M7M3AxRknEniuMT1Oo7X8dqELkPiOa5rRewQad2aCA7KAI1FKrTy6TSki/litKTWVpg9nKB0hKcE/EGu0RO8d0gAR5A6AbS41EfAmq1fSDXXNDtEgLPtygAa/97iWm1bjKfiCH6KGVjzDPmeGQhYCr7nuq7ttG3nwarkTtkB/ICu6WQ2p4vyhX4nAk9F2fx2WM5VjV4C5jijwO89PYb222p9Zz9vNkO757iv1odAVoMfjxZdQlUKmKAwjVl/qWuT5oLMQPErvnLkvsxMnjbVMRjfaSy+SOkV0n9Nq4RX7sQx4yKS9UP1L2It6IWJvINiOW1jaRz1V9wIyJVa9eU1X+NKfSr9pawZ7pgShhVE3VQyYwRTf0DVG/QRCl/zvOfOVWf576paAIajSIpGIZiUyDMM2Wokicx2QpG4JDXnjVXrL3+0Kv0AGd4bNDAsAhRW+KkhTg9ACxaro+gIxxowDSCtIgIUIWzKR3uE3+3rtlKrTA0zc/5vUtw=X02iq";
    protected static final String CONFLUENCE_LICENSE = "AAACCg0ODAoPeNp1U8uO2jAU3ecrLHVXKciBATRIWfBwB1omg0hoq1E3JrkwbhM78gPK3zc4POICiyxyfe659xwff0oMoFcqEe6ioDPAzwPcQ4s4QW0cdLwY5A7kbBKOXp4T/+fq+5P/7f196o9w8MObsxS4guRQQkQLCBMSJ7PoxYtJFFaf38XYS6XYZ60GMhwfK78GaAI7yEUJ0ksF37RoqtkOQi0NVE0mZescnOKJg/wtmTxMqIZw0Zleoe6IuuhM+c0kdQgXRqYf9IaxzM2WcdX63OKmmAqlIVspkCoMsBeZYg3ybVMX/KABTiSjOeHZhWcCKpWs1EzwMAGlUV5viDZCoroPZeftlEd2NDfUou12V+ah0WIJHPb1wStlXAOnPL3x4uij40NVyA1USMeJsQQ7yDa2g6Dj47ZfqbP9Z4nj3FTSZSQyUCH25lTpV5GxDYMsDDq9djfo9596uNf3BM+goDyr11vTYi3EhWZkf+cipflwC1xb106YW39PB00NNQEilWZZSqZcKVeXmjfr0pCMWVtJlJDlYjmLSZ2F2/GXMD3czJW1hEJoOOkKTll3g3ys3LJtmPqAw705drPHi58b76h1V1vklFuv3+SWcqbqZA11TpVilHuxWV/j2Xxf1Vtf8T9c7Hmdh4dW3HtK/yeqqcbe5tfZcvjoLs/amvgvVY0cXNw/VJ2p1DAtAhQP9ixrO7HDOtP9TH3aawjmjCGd0gIVAIK7drGUn56ZTOmWl0vJp6CQCeUpX02ok";


    @Rule
    public final HtmlDumpRule htmlDump = new HtmlDumpRule(product.getTester().getDriver());

    @After
    public final void logout()
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }
    
    protected void addPluginLicenses()
    {
        logout();
        product.visit(LoginPage.class).login(ADMIN_USERNAME, ADMIN_USERNAME, HomePage.class);
        
        if("jira".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            product.visit(ViewLicensePage.class).updateLicense(TIMEBOMB_WILDCARD_PLUGIN_LICENSE);

        }
        else if("confluence".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            product.visit(AdminHomePage.class);
            product.getTester().getDriver().waitUntilElementIsVisible(By.linkText("License Details"));
            product.getTester().getDriver().findElement(By.linkText("License Details")).click();
            product.getTester().getDriver().waitUntilElementIsLocated(By.name("licenseString"));
            product.getTester().getDriver().findElement(By.name("licenseString")).sendKeys(TIMEBOMB_WILDCARD_PLUGIN_LICENSE);
            product.getTester().getDriver().findElement(By.xpath("//input[@name='update']")).click();
            
        }
        logout();
    }

    protected void resetLicenses()
    {
        logout();
        product.visit(LoginPage.class).login(ADMIN_USERNAME, ADMIN_USERNAME, HomePage.class);

        if("jira".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            product.visit(ViewLicensePage.class).updateLicense(JIRA_LICENSE);

        }
        else if("confluence".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            product.visit(AdminHomePage.class);
            product.getTester().getDriver().waitUntilElementIsVisible(By.linkText("License Details"));
            product.getTester().getDriver().findElement(By.linkText("License Details")).click();
            product.getTester().getDriver().waitUntilElementIsLocated(By.name("licenseString"));
            product.getTester().getDriver().findElement(By.name("licenseString")).sendKeys(CONFLUENCE_LICENSE);
            product.getTester().getDriver().findElement(By.xpath("//input[@name='update']")).click();

        }
        logout();
    }
}
