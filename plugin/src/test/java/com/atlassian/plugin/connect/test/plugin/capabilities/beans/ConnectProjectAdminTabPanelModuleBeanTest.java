package com.atlassian.plugin.connect.test.plugin.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.jira.capabilities.provider.ConnectProjectAdminTabPanelModuleProvider;
import com.opensymphony.util.FileUtils;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectProjectAdminTabPanelModuleBean.newProjectAdminTabPanelBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory.getGson;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;


public class ConnectProjectAdminTabPanelModuleBeanTest
{
    @Test
    public void producesCorrectJSON() throws Exception
    {
        String json = getGson().toJson(createBean(), ConnectAddonBean.class);
        assertThat(json, is(sameJSONAs(expectedJson())));
    }

    @Test
    public void producesExactlyOneProjectAdminTabPanelModule()
    {
        List<? extends ModuleBean> modules = createBean().getModules().getJiraProjectAdminTabPanels();

        assertThat(modules, hasSize(1));
    }

    @Test
    public void producesModuleOfCorrectType()
    {
        ModuleBean moduleBean = createBean().getModules().getJiraProjectAdminTabPanels().get(0);

        assertThat(moduleBean, is(instanceOf(ConnectProjectAdminTabPanelModuleBean.class)));
    }

    @Test
    public void prefixesLocationCorrectly()
    {
        ConnectProjectAdminTabPanelModuleBean moduleBean = createBean().getModules().getJiraProjectAdminTabPanels().get(0);

        assertThat(moduleBean.getAbsoluteLocation(), is("atl.jira.proj.config/a-location"));
    }

    private ConnectAddonBean createBean()
    {
        Map<String, String> links = new HashMap<String, String>();
        links.put("self", "http://www.example.com/capabilities");
        links.put("homepage", "http://www.example.com");

        return newConnectAddonBean()
                .withName("My Plugin")
                .withKey("my-plugin")
                .withVersion("1.0")
                .withLinks(links)
                .withBaseurl("http://www.example.com")
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withModule(ConnectProjectAdminTabPanelModuleProvider.PROJECT_ADMIN_TAB_PANELS, newProjectAdminTabPanelBean()
                        .withName(new I18nProperty("My ProjectAdmin Tab Page", "my.projectAdminTabPage"))
                        .withUrl("/my-general-page")
                        .withWeight(100)
                        .withLocation("a-location")
                        .build())
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.OAUTH).withPublicKey("S0m3Publ1cK3y").build())
                .build();
    }

    private static String expectedJson() throws IOException
    {
        return FileUtils.readFile(new DefaultResourceLoader().getResource("classpath:/testfiles/capabilities/projectAdminTabAddon.json").getFile());
    }
}
