package com.atlassian.plugin.connect.plugin.capabilities.module.serialise;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.EntityPropertyModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.EntityPropertyType;
import com.atlassian.plugin.connect.spi.module.provider.Module;
import com.google.common.collect.Lists;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public abstract class BaseConnectAddonBeanGsonSerialiserTest
{

    private static String jsonStr;
    private final ConnectAddonBeanGsonSerialiser serialiser;

//    final String jsonStr = readTestFile();



    public BaseConnectAddonBeanGsonSerialiserTest(ModuleListSerialiser moduleListSerialiser)
    {
        serialiser = new ConnectAddonBeanGsonSerialiser(moduleListSerialiser);
    }


    @BeforeClass
    public static void setUp() throws IOException
    {
        jsonStr = readTestFile();
    }

    protected abstract EntityPropertyModuleBean convert(Object bean);

    @Test
    public void deserialise()
    {
        System.out.println(jsonStr);
        ConnectAddonBean addonBean = serialiser.deserialise(jsonStr);
        Map<String, Object> moduleMap = addonBean.getModules().getModules();
        System.out.println(moduleMap);

        List<Object> modules = (List<Object>) moduleMap.get("jiraEntityProperties");

        List<EntityPropertyModuleBean> entityProperties = Lists.newArrayList();
        for (Object entityProperty : modules)
        {
            entityProperties.add(convert(entityProperty));
        }

        System.out.println(entityProperties);

        assertThat(entityProperties, hasSize(1));

        EntityPropertyModuleBean bean = entityProperties.get(0);
        assertThat(bean.getEntityType(), is(EntityPropertyType.issue));
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("entityproperty/entityPropertyAddon.json");
    }

}