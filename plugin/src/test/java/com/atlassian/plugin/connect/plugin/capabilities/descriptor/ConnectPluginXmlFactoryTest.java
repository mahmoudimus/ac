package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import javax.annotation.Nullable;
import java.util.List;

import com.atlassian.plugin.connect.plugin.capabilities.beans.ConfigurePageModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.ConnectAddonBean;
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

import static org.dom4j.DocumentHelper.selectNodes;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConnectPluginXmlFactoryTest
{

    private static final String EXPECTED_CONFIGURE_URL = "/plugins/servlet/ac/addonKey/config-page";
    private ConnectAddonBean addonWithNoConfigurePages;
    private ConnectAddonBean addonWithOneConfigurePage;
    private ConnectAddonBean addonWithTwoConfigurePagesOneDefault;

    @Before
    public void init()
    {
        addonWithNoConfigurePages = ConnectAddonBean.newConnectAddonBean().build();
        addonWithOneConfigurePage = ConnectAddonBean.newConnectAddonBean()
                .withKey("addonKey")
                .withModule("configurePages", ConfigurePageModuleBean.newConfigurePageBean()
                        .withKey("myModuleKey")
                        .build())
                .build();

        addonWithTwoConfigurePagesOneDefault = ConnectAddonBean.newConnectAddonBean()
                .withKey("addonKey")
                .withModule("configurePages", ConfigurePageModuleBean.newConfigurePageBean()
                        .withKey("myModuleKey")
                        .build())
                .withModule("configurePages", ConfigurePageModuleBean.newConfigurePageBean()
                        .withKey("myModuleKey2")
                        .setAsDefault()
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

    @Test
    public void oneConfigureUrlParamAddedWhenTwoConfigureModulesWithOneMarkedDefault() throws DocumentException
    {
        assertThat(getConfigUrls(addonWithTwoConfigurePagesOneDefault), hasSize(1));
    }

    @Test
    public void theConfigureUrlIsCorrectForAddonKeyWhenTwoModules() throws DocumentException
    {
        assertThat(getConfigUrls(addonWithTwoConfigurePagesOneDefault).get(0), equalTo(EXPECTED_CONFIGURE_URL));
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
