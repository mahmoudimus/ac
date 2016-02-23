package com.atlassian.connect.test.jira.pageobjects;

import com.atlassian.jira.pageobjects.JiraTestedProduct;
import com.atlassian.jira.pageobjects.components.CalendarPopup;
import com.atlassian.pageobjects.Page;
import com.atlassian.plugin.connect.test.common.pageobjects.ConnectAddonPage;
import com.atlassian.plugin.connect.test.common.util.IframeUtils;
import com.atlassian.plugin.connect.test.jira.product.JiraTestedProductAccessor;
import com.atlassian.webdriver.AtlassianWebDriver;
import org.openqa.selenium.By;

import javax.inject.Inject;

/**
 * Page with a date picker control
 */
public class RemoteDatePickerGeneralPage extends ConnectAddonPage implements Page {

    public static String TEMPLATE_PATH = "jira/iframe-date-picker.mu";

    public static String DATE_TRIGGER = "date-trigger";
    public static String DATE_FIELD = "date-field";
    public static String DATE_TIME_TRIGGER = "date-time-trigger";
    public static String DATE_TIME_FIELD = "date-time-field";
    public static String TODAY_TRIGGER = "today-trigger";
    public static String TODAY_FIELD = "today-field";

    protected static JiraTestedProduct product = new JiraTestedProductAccessor().getJiraProduct();

    @Inject
    protected AtlassianWebDriver driver;

    public RemoteDatePickerGeneralPage(String addonKey, String moduleKey) {
        super(addonKey, moduleKey, true);
    }

    @Override
    public String getUrl() {
        return IframeUtils.iframeServletPath(addonKey, pageElementKey);
    }

    public CalendarPopup openDatePicker(String triggerId) {
        runInFrame(() -> {
            driver.findElement(By.id(triggerId)).click();
            return null;
        });
        return product.getPageBinder().bind(CalendarPopup.class);
    }

    public String getSelectedDate(String fieldId) {
        return runInFrame(() -> driver.findElement(By.id(fieldId)).getAttribute("value"));
    }

    public String getSelectedIsoDate(String fieldId) {
        return runInFrame(() -> driver.findElement(By.id(fieldId)).getAttribute("data-iso"));
    }

}
