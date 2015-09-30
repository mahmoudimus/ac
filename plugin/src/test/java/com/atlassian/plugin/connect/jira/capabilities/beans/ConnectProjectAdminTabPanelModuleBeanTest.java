package com.atlassian.plugin.connect.jira.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.opensymphony.util.FileUtils;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean.newProjectAdminTabPanelBean;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


public class ConnectProjectAdminTabPanelModuleBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        String json = ConnectModulesGsonFactory.getGson().toJson(createBean(), ConnectProjectAdminTabPanelModuleBean.class);
        assertThat(json, is(sameJSONAs(expectedJson())));
    }

    @Test
    public void prefixesLocationCorrectly()
    {
        ConnectProjectAdminTabPanelModuleBean moduleBean = createBean();

        assertThat(moduleBean.getAbsoluteLocation(), is("atl.jira.proj.config/a-location"));
    }

    private ConnectProjectAdminTabPanelModuleBean createBean()
    {
        return newProjectAdminTabPanelBean()
                .withName(new I18nProperty("My ProjectAdmin Tab Page", "my.projectAdminTabPage"))
                .withUrl("/my-general-page")
                .withWeight(100)
                .withLocation("a-location")
                .build();
    }

    private static String expectedJson() throws IOException
    {
        return FileUtils.readFile(new DefaultResourceLoader().getResource("classpath:/testfiles/capabilities/projectAdminTabAddon.json").getFile());
    }
}
