package com.atlassian.plugin.connect.plugin.capabilities;

import com.atlassian.plugin.connect.modules.beans.AuthenticationType;
import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.connect.modules.beans.AuthenticationBean.newAuthenticationBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.WebItemModuleBean.newWebItemBean;
import static com.atlassian.plugin.connect.modules.beans.nested.IconBean.newIconBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;

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
                .withUrl("/my-general-page")
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

        ConnectAddonBean addon = newConnectAddonBean()
                .withName("Double Module Addon")
                .withKey("double-module-addon")
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withAuthentication(
                        newAuthenticationBean().withType(AuthenticationType.NONE).build()
                )
                .withBaseurl("http://localhost/ac-test-addon")
                .withModules("webItems", newWebItemBean()
                        .withName(new I18nProperty("My Web Item", "my.webitem"))
                        .withUrl("/webitem")
                        .withLocation("atl.admin/menu")
                        .build()
                        
                        ,newWebItemBean()
                        .withName(new I18nProperty("My Other Web Item", "my.other.webitem"))
                        .withUrl("/other-webitem")
                        .withLocation("atl.admin/menu")
                        .build()
                )
                .build();

        Gson gson = ConnectModulesGsonFactory.getGson();

        String json = gson.toJson(addon, ConnectAddonBean.class);

        System.out.println(json);
    }
}
