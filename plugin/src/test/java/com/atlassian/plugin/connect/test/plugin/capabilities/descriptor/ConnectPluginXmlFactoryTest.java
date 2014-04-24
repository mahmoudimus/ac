package com.atlassian.plugin.connect.test.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.ConnectPluginXmlFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Text;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import javax.annotation.Nullable;
import java.util.List;

import static org.dom4j.DocumentHelper.selectNodes;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConnectPluginXmlFactoryTest
{
    private static final String EXPECTED_CONFIGURE_URL = "/plugins/servlet/ac/addonKey/my-config-module";
    private ConnectAddonBean addonWithNoConfigurePages;
    private ConnectAddonBean addonWithOneConfigurePage;

    @Before
    public void init()
    {
        addonWithNoConfigurePages = ConnectAddonBean.newConnectAddonBean().build();
        addonWithOneConfigurePage = ConnectAddonBean.newConnectAddonBean()
                .withKey("addonKey")
                .withModule("configurePage", ConnectPageModuleBean.newPageBean()
                        .withName(new I18nProperty("myConfigModule", null))
                        .withKey("my-config-module")
                        .withUrl("/configure-page-url")
                        .build())
                .build();
    }

    @Test
    public void noConfigureUrlParamAddedWhenNoConfigureModules() throws DocumentException
    {
        assertThat(getConfigUrls(addonWithNoConfigurePages), hasSize(0));
    }

    @Test
    public void oneConfigureUrlParamAddedWhenExactlyOneConfigureModule() throws DocumentException
    {
        assertThat(getConfigUrls(addonWithOneConfigurePage), hasSize(1));
    }

    @Test
    public void theConfigureUrlIsCorrectForAddonKey() throws DocumentException
    {
        assertThat(getConfigUrls(addonWithOneConfigurePage).get(0), equalTo(EXPECTED_CONFIGURE_URL));
    }

    private List<String> getConfigUrls(ConnectAddonBean bean) throws DocumentException
    {
        ConnectPluginXmlFactory factory = new ConnectPluginXmlFactory();
        String pluginXml = factory.createPluginXml(bean);
        Document document = DocumentHelper.parseText(pluginXml);

        @SuppressWarnings("unchecked") // pre generics code :-(
        List<Text> configUrlNodes = selectNodes("/atlassian-plugin/plugin-info/param[@name='configure.url']/text()",
                document.getRootElement());
        return Lists.transform(configUrlNodes, new Function<Text, String>()
        {
            @Override
            public String apply(@Nullable Text input)
            {
                return input.getText();
            }
        });
    }


}