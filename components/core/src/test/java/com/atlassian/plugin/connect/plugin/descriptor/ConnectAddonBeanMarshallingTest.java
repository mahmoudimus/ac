package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.ModuleBean;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleMeta;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionBean;
import com.atlassian.plugin.connect.modules.beans.nested.CompositeConditionType;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.modules.beans.nested.SingleConditionBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.common.base.Supplier;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.plugin.connect.test.TestFileReader.readAddonTestFile;
import static com.google.common.collect.Sets.newHashSet;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.core.CombinableMatcher.both;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the basic marshalling of module beans
 * <p/>
 * This is the only place where we should actually have to test the marshalling as the adapter factory handles everything.
 * This is also the only class that should be using hard-coded json strings.
 */
public class ConnectAddonBeanMarshallingTest {

    private static final Type JSON_MODULE_LIST_TYPE = new TypeToken<Map<String, Supplier<List<ModuleBean>>>>() {
    }.getType();

    /**
     * Just verifies the basic marshalling of the core properties for the top-level add on bean
     *
     * @throws Exception
     */
    @Test
    public void verifyAddonValues() throws Exception {
        String json = readAddonTestFile("addonNoCapabilities.json");

        Gson gson = ConnectModulesGsonFactory.getGson();
        ConnectAddonBean addon = gson.fromJson(json, ConnectAddonBean.class);

        assertEquals("My Plugin", addon.getName());
        assertEquals("a cool addon", addon.getDescription());
        assertEquals("my-plugin", addon.getKey());
        assertEquals("Atlassian", addon.getVendor().getName());
        assertEquals("http://www.atlassian.com", addon.getVendor().getUrl());
        assertEquals(2, addon.getLinks().size());
        assertEquals("http://www.example.com", addon.getLinks().get("homepage"));
        assertEquals("http://www.example.com/capabilities", addon.getLinks().get("self"));
    }

    /**
     * Verifies basic marshalling and a composite condition copied from our docs.
     *
     * @throws Exception
     */
    @Test
    public void verifyCompositeCondition() throws Exception {
        String json = readAddonTestFile("addonNoCapabilitiesCompositeCondition.json");

        ConnectAddonBean addon = deserializeAddonWithModules(json);

        List<ModuleBean> moduleList = addon.getModules().getValidModuleListOfType("webItems", (e) -> {
        }).get();
        assertThat(moduleList, contains(hasProperty("conditions", contains(
                both(instanceOf(CompositeConditionBean.class)).and(hasProperty("conditions", contains(
                        both(instanceOf(SingleConditionBean.class)).and(hasProperty("condition", is("can_attach_file_to_issue"))),
                        both(instanceOf(SingleConditionBean.class)).and(hasProperty("condition", is("is_issue_assigned_to_current_user")))
                ))).and(hasProperty("type", is(CompositeConditionType.OR))),
                both(instanceOf(SingleConditionBean.class)).and(hasProperty("condition", is("user_is_logged_in")))
        ))));
    }

    /**
     * Verifies that extra stuff in the json that an external developer may put in is ignored without exceptions
     *
     * @throws Exception
     */
    @Test
    public void verifyExtraValuesAreIgnored() throws Exception {
        String json = readAddonTestFile("addonExtraValue.json");

        Gson gson = ConnectModulesGsonFactory.getGson();
        ConnectAddonBean addon = gson.fromJson(json, ConnectAddonBean.class);

        assertEquals("My Plugin", addon.getName());
        assertEquals("a cool addon", addon.getDescription());
        assertEquals("my-plugin", addon.getKey());
        assertEquals("Atlassian", addon.getVendor().getName());
        assertEquals("http://www.atlassian.com", addon.getVendor().getUrl());
        assertEquals(2, addon.getLinks().size());
        assertEquals("http://www.example.com", addon.getLinks().get("homepage"));
        assertEquals("http://www.example.com/capabilities", addon.getLinks().get("self"));
    }

    /**
     * Verifies that a descriptor without a capabilities entry returns an empty capabilities map
     *
     * @throws Exception
     */
    @Test
    public void noCapabilitiesReturnsEmptyList() throws Exception {
        String json = readAddonTestFile("addonNoCapabilities.json");

        Gson gson = ConnectModulesGsonFactory.getGson();
        ConnectAddonBean addon = gson.fromJson(json, ConnectAddonBean.class);

        assertNotNull(addon.getModules());
    }

    /**
     * Tests that a module whose value is an object gets transformed into a list of one
     *
     * @throws Exception
     */
    @Test
    public void singleModule() throws Exception {
        String json = readAddonTestFile("addonSingleCapability.json");

        ConnectAddonBean addon = deserializeAddonWithModules(json);

        List<ModuleBean> moduleList = addon.getModules().getValidModuleListOfType("webItems", (e) -> {
        }).get();

        assertEquals(1, moduleList.size());

        WebItemModuleBean module = (WebItemModuleBean) moduleList.get(0);

        assertEquals("a web item", module.getName().getValue());
    }

    /**
     * Verifies that multiple module entries with mixed object / array values is marshalled properly
     *
     * @throws Exception
     */
    @Test
    public void multiCapabilities() throws Exception {
        String json = readAddonTestFile("addonMultipleCapabilities.json");
        ConnectAddonBean addon = deserializeAddonWithModules(json);
        List<ModuleBean> moduleList = addon.getModules().getValidModuleListOfType("webItems", (e) -> {
        }).get();

        assertEquals(2, moduleList.size());
        assertEquals("a web item", ((WebItemModuleBean) moduleList.get(0)).getName().getValue());
        assertEquals("another web item", ((WebItemModuleBean) moduleList.get(1)).getName().getValue());
        assertEquals("http://www.example.com", addon.getBaseUrl());
    }

    @Test
    public void noScopes() throws IOException {
        String json = readAddonTestFile("addonNoCapabilities.json");
        Gson gson = ConnectModulesGsonFactory.getGsonBuilder().create();
        ConnectAddonBean addon = gson.fromJson(json, ConnectAddonBean.class);
        assertThat(addon.getScopes(), is(Collections.<ScopeName>emptySet()));
    }

    @Test
    public void singleScope() throws IOException {
        String json = readAddonTestFile("singleScope.json");
        ConnectAddonBean addon = ConnectModulesGsonFactory.getGson().fromJson(json, ConnectAddonBean.class);
        assertThat(addon.getScopes(), is((Set<ScopeName>) newHashSet(ScopeName.READ)));
    }

    @Test
    public void multipleScopes() throws IOException {
        String json = readAddonTestFile("multipleScopes.json");
        ConnectAddonBean addon = ConnectModulesGsonFactory.getGson().fromJson(json, ConnectAddonBean.class);
        assertThat(addon.getScopes(), is((Set<ScopeName>) newHashSet(ScopeName.READ, ScopeName.WRITE)));
    }

    @Test
    public void badScopeName() throws IOException {
        Set<ScopeName> emptySet = newHashSet();
        String json = readAddonTestFile("badScopeName.json");
        ConnectAddonBean addon = ConnectModulesGsonFactory.getGson().fromJson(json, ConnectAddonBean.class);
        assertThat(addon.getScopes(), is(emptySet));
    }

    private ConnectAddonBean deserializeAddonWithModules(String json) {
        JsonElement element = new JsonParser().parse(json);
        ShallowConnectAddonBean shallowBean = ConnectModulesGsonFactory.shallowAddonFromJson(element);

        Gson gson = ConnectModulesGsonFactory.getGsonBuilder().registerTypeAdapter(JSON_MODULE_LIST_TYPE,
                createDeserializer(shallowBean)).create();
        return gson.fromJson(json, ConnectAddonBean.class);
    }

    private ModuleListDeserializer createDeserializer(ShallowConnectAddonBean descriptor) {
        return new StaticModuleListDeserializer(descriptor, new WebItemModuleMeta());
    }
}
