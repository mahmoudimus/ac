package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.GeneralPageModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.atlassian.plugin.connect.plugin.condition.PageConditionsFactoryImpl;
import com.atlassian.plugin.connect.plugin.installer.AvailableModuleTypes;
import com.atlassian.plugin.connect.plugin.installer.ModuleBeanDeserializer;
import com.atlassian.plugin.connect.plugin.installer.StaticAvailableModuleTypes;
import com.atlassian.plugin.connect.spi.capabilities.provider.PageConditionsValidator;
import com.atlassian.plugin.connect.spi.condition.PageConditionsFactory;
import com.atlassian.plugin.connect.spi.module.provider.ConnectModuleValidationException;
import com.atlassian.sal.api.message.I18nResolver;
import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static com.atlassian.plugin.connect.util.io.TestFileReader.readAddonTestFile;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PageConditionValidationTest
{
    @Mock
    private I18nResolver i18nResolver;
    private AvailableModuleTypes moduleTypes = new StaticAvailableModuleTypes(new GeneralPageModuleMeta());
    private PageConditionsFactory pageConditionsFactory = new PageConditionsFactoryImpl();
    private PageConditionsValidator conditionsValidator;

    @Before
    public void setup()
    {
        when(i18nResolver.getText(anyString(), anyString(), anyString())).thenReturn("error message");
        this.conditionsValidator = new PageConditionsValidator(pageConditionsFactory);
    }

    @Test
    public void validBuiltInConditionPasses() throws Exception
    {
        validateFully(readConditionJson("valid-builtin-condition.json"));
    }

    @Test
    public void validRemoteConditionPasses() throws Exception
    {
        validateFully(readConditionJson("valid-remote-condition.json"));
    }

    @Test
    public void validNestedBuiltInConditionPasses() throws Exception
    {
        validateFully(readConditionJson("valid-nested-builtin-condition.json"));
    }

    @Test
    public void validNestedRemoteConditionPasses() throws Exception
    {
        validateFully(readConditionJson("valid-nested-remote-condition.json"));
    }

    @Test(expected = ConnectModuleValidationException.class)
    public void invalidConditionFails() throws Exception
    {
        validateFully(readConditionJson("invalid-condition.json"));
    }

    @Test(expected = ConnectModuleValidationException.class)
    public void invalidNestedConditionFails() throws Exception
    {
        validateFully(readConditionJson("invalid-nested-condition.json"));
    }

    private String readConditionJson(String filename) throws IOException
    {
        return readAddonTestFile("pageconditions/" + filename);
    }

    public void validateFully(final String jsonDescriptor) throws Exception
    {
        JsonElement element = new JsonParser().parse(jsonDescriptor);
        ShallowConnectAddonBean addon = ConnectModulesGsonFactory.shallowAddonFromJsonWithI18nCollector(element, null);

        ModuleBeanDeserializer deserializer = new ModuleBeanDeserializer(moduleTypes);
        Map<String, Supplier<List<ModuleBean>>> moduleMap = ConnectModulesGsonFactory.moduleListFromJson(element, deserializer);

        List<ModuleBean> modules = moduleMap.get("generalPages").get();
        List<ConnectPageModuleBean> pageModules = Lists.transform(modules, genericBean -> (ConnectPageModuleBean) genericBean);
        conditionsValidator.validate(addon, pageModules, "generalPages");
    }
}
