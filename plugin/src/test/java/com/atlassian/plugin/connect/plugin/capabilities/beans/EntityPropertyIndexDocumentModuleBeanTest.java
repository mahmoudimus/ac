package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.EntityPropertyIndexExtractionConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.EntityPropertyIndexKeyConfigurationBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.EntityPropertyIndexType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.EntityPropertyType;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.gson.ConnectModulesGsonFactory;
import com.google.common.collect.Lists;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static com.atlassian.plugin.connect.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EntityPropertyIndexDocumentModuleBeanTest
{
    @Test
    public void producesCorrectBean() throws IOException
    {
        EntityPropertyModuleBean expectedBean = createModuleBean();
        String json = readTestFile();

        EntityPropertyModuleBean addOn =
                ConnectModulesGsonFactory.getGson().fromJson(json, EntityPropertyModuleBean.class);

        assertThat(addOn.getEntityType(), is(expectedBean.getEntityType()));
        assertThat(addOn.getKeyConfigurations(), hasSize(1));

        EntityPropertyIndexKeyConfigurationBean addOnKeyConfigurationBean = addOn.getKeyConfigurations().get(0);
        EntityPropertyIndexKeyConfigurationBean expectedKeyConfigurationBean = expectedBean.getKeyConfigurations().get(0);

        assertThat(addOnKeyConfigurationBean.getPropertyKey(), is(expectedKeyConfigurationBean.getPropertyKey()));
        assertThat(addOnKeyConfigurationBean.getExtractions(), containsInAnyOrder(expectedKeyConfigurationBean.getExtractions().toArray()));
    }

    private EntityPropertyModuleBean createModuleBean()
    {
        List<EntityPropertyIndexExtractionConfigurationBean> extractions = Lists.newArrayList(
                new EntityPropertyIndexExtractionConfigurationBean("attachment.size", EntityPropertyIndexType.number),
                new EntityPropertyIndexExtractionConfigurationBean("attachment.extension", EntityPropertyIndexType.string)
        );

        return EntityPropertyModuleBean.newEntityPropertyModuleBean()
                .withName(new I18nProperty("My Index", "my.index"))
                .withPropertyType(EntityPropertyType.issue)
                .withKeyConfiguration(new EntityPropertyIndexKeyConfigurationBean(extractions, "attachment"))
                .build();
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("entityPropertyIndexDocument.json");
    }
}
