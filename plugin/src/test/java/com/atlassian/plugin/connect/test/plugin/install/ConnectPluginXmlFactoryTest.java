package com.atlassian.plugin.connect.test.plugin.install;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectPluginXmlFactory;

import org.junit.Test;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean.newPageBean;
import static org.junit.Assert.assertTrue;

public class ConnectPluginXmlFactoryTest
{
    @Test
    public void testXml() throws Exception
    {
        ConnectAddonBean bean = newConnectAddonBean()
                .withKey("my-plugin")
                .withModule(
                        "generalPages",
                        newPageBean()
                                .withName(new I18nProperty("Page Two", null))
                                .withKey("page-two")
                                .withUrl("/pg")
                                .withWeight(1234)
                                .build()
                ).build();

        ConnectPluginXmlFactory factory = new ConnectPluginXmlFactory();
        
        String xml = factory.createPluginXml(bean);
        
        assertTrue(xml.contains("<connect-dependency-enforcer"));
    }
}
