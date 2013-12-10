package com.atlassian.plugin.connect.plugin.capabilities;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;

import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;

import org.junit.Test;

import static com.atlassian.plugin.connect.plugin.capabilities.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.plugin.capabilities.beans.nested.VendorBean.newVendorBean;

/**
 * @since 1.0
 */
public class JunkTest
{
    @Test
    public void testName() throws Exception
    {
        WebItemModuleBean bean = newWebItemBean()
                .withName(new I18nProperty("My Web Item", "my.webitem"))
                .withLink("/my-general-page")
                .withLocation("atl.admin/menu")
                .withWeight(100)
                .withTooltip(new I18nProperty("click me!", "click.me"))
                .withStyleClasses("ac-link", "aui-link")
                .withIcon(newIconBean().withUrl("/some/icon.png").withWidth(16).withHeight(16).build())
                .build();

        Gson gson = ConnectModulesGsonFactory.getGson();

        String json = gson.toJson(bean, WebItemModuleBean.class);

        System.out.println(json);
    }

    @Test
    public void testAddon() throws Exception
    {
        Map<String, String> links = new HashMap<String, String>();
        links.put("self", "http://www.example.com/capabilities");
        links.put("homepage", "http://www.example.com");

        ConnectAddonBean addon = newConnectAddonBean()
                .withName("My Plugin")
                .withKey("my-plugin")
                .withVersion("1.0")
                .withLinks(links)
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withModule("webItems", newWebItemBean()
                        .withName(new I18nProperty("My Web Item", "my.webitem"))
                        .withLink("/my-general-page")
                        .withLocation("atl.admin/menu")
                        .withWeight(100)
                        .withTooltip(new I18nProperty("click me!", "click.me"))
                        .withStyleClasses("ac-link", "aui-link")
                        .withIcon(newIconBean().withUrl("/some/icon.png").withWidth(16).withHeight(16).build())
                        .build())
                .withAuthentication(newAuthenticationBean().withType(AuthenticationType.OAUTH).withSharedKey("S0m3Publ1cK3y").build())
                .build();

        Gson gson = ConnectModulesGsonFactory.getGson();

        String json = gson.toJson(addon, ConnectAddonBean.class);

        System.out.println(json);
    }
}
