package it.jira.jsapi;

import com.atlassian.connect.test.jira.pageobjects.RemoteDatePickerGeneralPage;
import com.atlassian.jira.pageobjects.components.CalendarPopup;
import com.atlassian.pageobjects.elements.query.Poller;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.test.common.servlet.ConnectRunner;
import com.atlassian.plugin.connect.test.common.util.AddonTestUtils;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import it.jira.JiraWebDriverTestBase;
import it.jira.servlet.JiraAppServlets;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * Test of AC date picker component in JIRA
 */
public class TestJiraDatePicker extends JiraWebDriverTestBase
{
    private static final String KEY_MY_AWESOME_PAGE = "my-awesome-page";
    private static final String PAGE_NAME = "My Awesome Page";

    private static ConnectRunner remotePlugin;

    private static String addonKey;

    @Rule
    public TestRule resetToggleableCondition = remotePlugin.resetToggleableConditionRule();

    @BeforeClass
    public static void startConnectAddon() throws Exception
    {
        addonKey = AddonTestUtils.randomAddonKey();
        remotePlugin = new ConnectRunner(product.getProductInstance().getBaseUrl(), addonKey)
                .setAuthenticationToNone()
                .addModules(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty(PAGE_NAME, null))
                                .withKey(KEY_MY_AWESOME_PAGE)
                                .withUrl("/pg")
                                .withWeight(1234)
                                .build())
                .addRoute("/pg", JiraAppServlets.datePickerServlet())
                .start();
    }

    @AfterClass
    public static void stopConnectAddon() throws Exception
    {
        if (remotePlugin != null)
        {
            remotePlugin.stopAndUninstall();
        }
    }

    @Test
    public void shouldPickDateWithTime()
    {
        RemoteDatePickerGeneralPage generalPage = loginAndVisit(testUserFactory.basicUser(), RemoteDatePickerGeneralPage.class, addonKey, KEY_MY_AWESOME_PAGE);
        assertThat(generalPage.getSelectedDate("date-time-field"), equalTo(""));
        CalendarPopup calendar = generalPage.openDatePicker("date-time-trigger");
        Poller.waitUntilTrue("Calendar popup should be open", calendar.isOpen());
        calendar.increaseHour();
        calendar.selectDay(23);
        assertThat(generalPage.getSelectedDate("date-time-field"), equalTo("23/Jan/16 04:56 PM"));
        assertThat(generalPage.getSelectedIsoDate("date-time-field"), equalTo("2016-01-23T15:56:00.000Z"));
    }

    @Test
    public void shouldPickDate()
    {
        RemoteDatePickerGeneralPage generalPage = loginAndVisit(testUserFactory.basicUser(), RemoteDatePickerGeneralPage.class, addonKey, KEY_MY_AWESOME_PAGE);
        assertThat(generalPage.getSelectedDate("date-field"), equalTo(""));
        CalendarPopup calendar = generalPage.openDatePicker("date-trigger");
        Poller.waitUntilTrue(calendar.isOpen());
        calendar.selectDay(23);
        assertThat(generalPage.getSelectedDate("date-field"), equalTo("23/Jan/16"));
        assertThat(generalPage.getSelectedIsoDate("date-field"), equalTo("2016-01-22T23:00:00.000Z"));
    }

    @Test
    public void shouldUseTodayAsDefaultDate()
    {
        RemoteDatePickerGeneralPage generalPage = loginAndVisit(testUserFactory.basicUser(), RemoteDatePickerGeneralPage.class, addonKey, KEY_MY_AWESOME_PAGE);
        assertThat(generalPage.getSelectedDate("today-field"), equalTo(""));
        CalendarPopup calendar = generalPage.openDatePicker("today-trigger");
        Poller.waitUntilTrue(calendar.isOpen());
        calendar.selectDay(calendar.getSelectedDay().now());

        DateFormat dateFormat = new SimpleDateFormat("dd/MMM/yy hh:mm aaa");
        Date date = new Date();

        assertThat(generalPage.getSelectedDate("today-field"), equalTo(dateFormat.format(date)));
    }

    @Test
    public void shouldCloseCalendarOnSelection()
    {
        RemoteDatePickerGeneralPage generalPage = loginAndVisit(testUserFactory.basicUser(), RemoteDatePickerGeneralPage.class, addonKey, KEY_MY_AWESOME_PAGE);
        assertThat(generalPage.getSelectedDate("today-field"), equalTo(""));
        CalendarPopup calendar = generalPage.openDatePicker("today-trigger");
        Poller.waitUntilTrue(calendar.isOpen());
        calendar.selectDay(calendar.getSelectedDay().now());

        Poller.waitUntilTrue(calendar.isClosed());
    }
}
