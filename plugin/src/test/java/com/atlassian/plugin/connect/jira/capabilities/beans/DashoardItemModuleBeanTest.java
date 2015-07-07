//package com.atlassian.plugin.connect.jira.capabilities.beans;
//
//import com.atlassian.plugin.connect.modules.beans.ConditionalBean;
//import com.atlassian.plugin.connect.modules.beans.DashboardItemModuleBean;
//import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
//import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
//import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
//import com.google.common.collect.Iterables;
//import org.junit.BeforeClass;
//import org.junit.Test;
//
//import java.io.IOException;
//
//import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
//import static org.hamcrest.Matchers.hasItems;
//import static org.hamcrest.Matchers.is;
//import static org.junit.Assert.assertThat;
//
//public class DashoardItemModuleBeanTest
//{
//    private static DashboardItemModuleBean expectedBean;
//    private static DashboardItemModuleBean actualBean;
//
//    @BeforeClass
//    public static void setUp() throws IOException
//    {
//        expectedBean = createModuleBean();
//        actualBean = ConnectModulesGsonFactory.getGson().fromJson(readAddonTestFile("dashboardItem.json"), DashboardItemModuleBean.class);
//    }
//
//    @Test
//    public void titleParsed() throws Exception
//    {
//        assertThat(expectedBean.getName(), is(actualBean.getName()));
//    }
//
//    @Test
//    public void descriptionParsed() throws Exception
//    {
//        assertThat(expectedBean.getDescription(), is(actualBean.getDescription()));
//    }
//
//    @Test
//    public void iconParsed()
//    {
//        assertThat(expectedBean.getThumbnailUrl(), is(actualBean.getThumbnailUrl()));
//    }
//
//    @Test
//    public void urlParsed()
//    {
//        assertThat(expectedBean.getUrl(), is(actualBean.getUrl()));
//    }
//
//    @Test
//    public void conditionParsed()
//    {
//        assertThat(expectedBean.getConditions(), hasItems(Iterables.toArray(expectedBean.getConditions(), ConditionalBean.class)));
//    }
//
//    @Test
//    public void configurableParsed()
//    {
//        assertThat(expectedBean.isConfigurable(), is(expectedBean.isConfigurable()));
//    }
//
//    private static DashboardItemModuleBean createModuleBean()
//    {
//        return DashboardItemModuleBean.newBuilder()
//                .withDescription(new I18nProperty("Title", "title.key"))
//                .withDescription(new I18nProperty("Description", "description.key"))
//                .withThumbnailUrl("/thumbnail.jpg")
//                .withUrl("some-iframe-url")
//                .configurable(true)
//                .withConditions(SingleConditionBean.newSingleConditionBean()
//                        .withCondition("some_condition")
//                        .build())
//                .build();
//    }
//
//
//}
