package com.atlassian.plugin.connect.jira.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleMeta;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyIndexType;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyType;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.installer.ModuleListDeserializer;
import com.atlassian.plugin.connect.plugin.installer.StaticAvailableModuleTypes;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.connect.modules.beans.ConnectAddonBean.newConnectAddonBean;
import static com.atlassian.plugin.connect.modules.beans.nested.VendorBean.newVendorBean;
import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
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
        actualBean = ConnectModulesGsonFactory.getGson().fromJson(readTestFile("entityProperty.json"), EntityPropertyModuleBean.class);
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
        ModuleListDeserializer deserializer = new ModuleListDeserializer(new StaticAvailableModuleTypes(new EntityPropertyModuleMeta()));
        Gson gson = ConnectModulesGsonFactory.getGsonBuilder().registerTypeAdapter(ConnectModulesGsonFactory.getModuleJsonType(), deserializer).create();
        String expectedJson = gson.toJson(bean, ConnectAddonBean.class);

        assertThat(readTestFile("entityPropertyAddon.json"), is(sameJSONAs(expectedJson)));
    }

    private static EntityPropertyModuleBean createModuleBean()
    {
        List<EntityPropertyIndexExtractionConfigurationBean> extractions = Lists.newArrayList(
                new EntityPropertyIndexExtractionConfigurationBean("attachment.size", EntityPropertyIndexType.number),
                new EntityPropertyIndexExtractionConfigurationBean("attachment.extension", EntityPropertyIndexType.string, "attachmentExtension")
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
