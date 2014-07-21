package com.atlassian.plugin.connect.test;

import com.atlassian.jira.pageobjects.pages.admin.ViewLicensePage;
import com.atlassian.pageobjects.TestedProduct;
import com.atlassian.pageobjects.page.AdminHomePage;
import com.atlassian.pageobjects.page.HomePage;
import com.atlassian.pageobjects.page.LoginPage;
import com.atlassian.webdriver.pageobjects.WebDriverTester;
import it.util.TestUser;
import org.openqa.selenium.By;

public abstract class LicenseUtils
{
    private final static String TIMEBOMB_WILDCARD_PLUGIN_LICENSE = "AAACCg0ODAoPeNp1U8uO2jAU3ecrInVXKcjmMRqQsuDhGWiZDCKhrUbdOMkF3CZ25AcMf9+Q8IgLLLLI9bnn3nN8/CXaGvcFYreNXfw8QGiAeu4ijNw2wh0nBLkDOZv4o9d+5P1a/eh63z8+pt4I4Z/OnCXAFUSHAgKagx+RMJoFr05IAr/8vB5CTiLFPm01kP74WPk9cCewg0wUIJ1E8HWLJprtwNfSQNlkEhZnYBVPHOSzYPIwoRr8RWd6hdoj6qI15Q+T1CJcGJls6Q1jkZkN46r1tcVNPhVKQ7pSIJWPkROYPAb5vq4LHm6AI8loRnh64ZmASiQrNBPcj0BpN6s3dNdCunWfm563Uw7Z0czQCl1td2UeGi2WwGFfH7xRxjVwypMbL44+Wj6UhcxAibScGEuoBlWNbYw7HsJeu1/3nyWOM1NKl4FIQfnImVOl30TK1gxSH3eecLf/3EW93hN2BE8hpzyt14tpHgtxoRlVv3OR0Gy4Aa4r106YW39PB00NNYFLSs2ykEzZUq4uNW/WpiEpq2wlQUSWi+UsJHUWbsdfwvRwM1vWEnKh4aQLn7JuB/lYuWVbM7WFw7051WaPFz833lFrr7bIKK+8fpcbypmqkzXUGVWKUe6EJr7Gs/m+yre+4n+52PM6Dw+tuPeU/k9UU011m99my+Gjuzxra+Jfyho52Lh//5qpzTAtAhUAj8L7xWFb4yfemMFIqVBg6p3ZzS8CFDVulZlFOu+THivlIbkZbjF76WhHX02ok";
    private static final String JIRA_LICENSE = "AAABgw0ODAoPeNp1kkFPwkAQhe/9FZt4LmmrYiTpAegiIAKBgoZ42W4HWF12m9kW5d9bC41tg9e3b958M7M3AxRknEniuMT1Oo7X8dqELkPiOa5rRewQad2aCA7KAI1FKrTy6TSki/litKTWVpg9nKB0hKcE/EGu0RO8d0gAR5A6AbS41EfAmq1fSDXXNDtEgLPtygAa/97iWm1bjKfiCH6KGVjzDPmeGQhYCr7nuq7ttG3nwarkTtkB/ICu6WQ2p4vyhX4nAk9F2fx2WM5VjV4C5jijwO89PYb222p9Zz9vNkO757iv1odAVoMfjxZdQlUKmKAwjVl/qWuT5oLMQPErvnLkvsxMnjbVMRjfaSy+SOkV0n9Nq4RX7sQx4yKS9UP1L2It6IWJvINiOW1jaRz1V9wIyJVa9eU1X+NKfSr9pawZ7pgShhVE3VQyYwRTf0DVG/QRCl/zvOfOVWf576paAIajSIpGIZiUyDMM2Wokicx2QpG4JDXnjVXrL3+0Kv0AGd4bNDAsAhRW+KkhTg9ACxaro+gIxxowDSCtIgIUIWzKR3uE3+3rtlKrTA0zc/5vUtw=X02iq";
    private static final String CONFLUENCE_LICENSE = "AAAA+A0ODAoPeNpdUMtqwzAQvOcrDD0r+JFCHRA0iQV92LGJ3d43Yt0KbMmsZNP8fVPLhTbXmdmZ2bkrjA5qHIIwDaJkm2y2cRpUWRPEYZSspNHt+mC0A+lEAarjdhwGQ+4RXAfWKtBraXqvu2rUhNzRiB6ogJxGOkKP3CO5kqgtNpcBZzQT7yIvK3HydEkfoJUFp4zmu98Ez4kJutEzLXR2ibh20g41aInia1B0ycAhr5KnVY00IT1nfC9eX9h9khfssIkbFp2Knb89jv0ZqWzfLJLlLFpKjyQ/weJs9DMCC1MWPvzrfxP1d6X5rdqPtBCEc+1bw28YQn/2MCwCFC3+hPGY7A6LdmE9+mz/4N6y+NHPAhRXP7A5iwkYrQn8+dzDVBCHxVrbGw==X02cs";

    public static void addPluginLicenses(TestedProduct<WebDriverTester> product)
    {
        logout(product);
        product.visit(LoginPage.class).login(TestUser.ADMIN.getUsername(), TestUser.ADMIN.getPassword(), HomePage.class);

        if("jira".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            product.visit(ViewLicensePage.class).updateLicense(LicenseUtils.TIMEBOMB_WILDCARD_PLUGIN_LICENSE);

        }
        else if("confluence".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            product.visit(AdminHomePage.class);
            product.getTester().getDriver().waitUntilElementIsVisible(By.linkText("License Details"));
            product.getTester().getDriver().findElement(By.linkText("License Details")).click();
            product.getTester().getDriver().waitUntilElementIsLocated(By.name("licenseString"));
            product.getTester().getDriver().findElement(By.name("licenseString")).sendKeys(LicenseUtils.TIMEBOMB_WILDCARD_PLUGIN_LICENSE);
            product.getTester().getDriver().findElement(By.xpath("//input[@name='update']")).click();
        }
    }

    public static void resetLicenses(TestedProduct<WebDriverTester> product)
    {
        logout(product);
        product.visit(LoginPage.class).login(TestUser.ADMIN.getUsername(), TestUser.ADMIN.getPassword(), HomePage.class);

        if("jira".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            product.visit(ViewLicensePage.class).updateLicense(LicenseUtils.JIRA_LICENSE);

        }
        else if("confluence".equalsIgnoreCase(product.getProductInstance().getInstanceId()))
        {
            product.visit(AdminHomePage.class);
            product.getTester().getDriver().waitUntilElementIsVisible(By.linkText("License Details"));
            product.getTester().getDriver().findElement(By.linkText("License Details")).click();
            product.getTester().getDriver().waitUntilElementIsLocated(By.name("licenseString"));
            product.getTester().getDriver().findElement(By.name("licenseString")).sendKeys(LicenseUtils.CONFLUENCE_LICENSE);
            product.getTester().getDriver().findElement(By.xpath("//input[@name='update']")).click();

        }
    }

    private static void logout(final TestedProduct<WebDriverTester> product)
    {
        product.getTester().getDriver().manage().deleteAllCookies();
    }
}
