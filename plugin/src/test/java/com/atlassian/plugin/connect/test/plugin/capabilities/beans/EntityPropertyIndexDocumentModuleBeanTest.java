package com.atlassian.plugin.connect.test.plugin.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.*;
import com.atlassian.plugin.connect.modules.gson.JiraConfluenceConnectModulesGsonFactory;
import com.google.common.collect.Lists;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static uk.co.datumedge.hamcrest.json.SameJSONAs.sameJSONAs;

public class EntityPropertyIndexDocumentModuleBeanTest
{
    private static EntityPropertyModuleBean expectedBean;
    private static EntityPropertyModuleBean actualBean;

    @BeforeClass
    public static void setUp() throws IOException
    {
        expectedBean = createModuleBean();
        actualBean = JiraConfluenceConnectModulesGsonFactory.getGson().fromJson(readTestFile("entityProperty.json"), EntityPropertyModuleBean.class);
    }

    @Test
    public void correctEntityTypeParsed()
    {
        assertThat(actualBean.getEntityType(), is(expectedBean.getEntityType()));
    }

    @Test
    public void keyConfigurationsParsed()
    {
        assertThat(actualBean.getKeyConfigurations(), hasSize(expectedBean.getKeyConfigurations().size()));
        assertThat(actualBean.getKeyConfigurations(), is(expectedBean.getKeyConfigurations()));
    }

    @Test
    public void propertyKeyParsed()
    {
        EntityPropertyIndexKeyConfigurationBean actualKeyConfigurationBean = actualBean.getKeyConfigurations().get(0);
        EntityPropertyIndexKeyConfigurationBean expectedKeyConfigurationBean = expectedBean.getKeyConfigurations().get(0);
        assertThat(actualKeyConfigurationBean.getPropertyKey(), is(expectedKeyConfigurationBean.getPropertyKey()));
    }

    @Test
    public void extractionsParsed()
    {
        EntityPropertyIndexKeyConfigurationBean actualKeyConfigurationBean = actualBean.getKeyConfigurations().get(0);
        EntityPropertyIndexKeyConfigurationBean expectedKeyConfigurationBean = expectedBean.getKeyConfigurations().get(0);

        assertThat(actualKeyConfigurationBean.getExtractions(), containsInAnyOrder(expectedKeyConfigurationBean.getExtractions().toArray()));
    }

    @Test
    public void addOnWithEntityPropertyParsed() throws IOException
    {
        ConnectAddonBean bean = createAddOnBean();
        String expectedJson = JiraConfluenceConnectModulesGsonFactory.getGson().toJson(bean, ConnectAddonBean.class);

        assertThat(readTestFile("entityPropertyAddon.json"), is(sameJSONAs(expectedJson)));
    }

    private static EntityPropertyModuleBean createModuleBean()
    {
        List<EntityPropertyIndexExtractionConfigurationBean> extractions = Lists.newArrayList(
                new EntityPropertyIndexExtractionConfigurationBean("attachment.size", EntityPropertyIndexType.number),
                new EntityPropertyIndexExtractionConfigurationBean("attachment.extension", EntityPropertyIndexType.string)
        );

        return EntityPropertyModuleBean.newEntityPropertyModuleBean()
                .withName(new I18nProperty("My index", "my.index"))
                .withEntityType(EntityPropertyType.issue)
                .withKeyConfiguration(new EntityPropertyIndexKeyConfigurationBean(extractions, "attachment"))
                .build();
    }

    private static ConnectAddonBean createAddOnBean()
    {
        return newConnectAddonBean()
                .withName("My Add-On")
                .withKey("my-add-on")
                .withVersion("2.0")
                .withBaseurl("http://www.example.com")
                .withVendor(newVendorBean().withName("Atlassian").withUrl("http://www.atlassian.com").build())
                .withModule("jiraEntityProperties", createModuleBean())
                .build();
    }

    private static String readTestFile(String filename) throws IOException
    {
        return readAddonTestFile("entityproperty/" + filename);
    }
}
