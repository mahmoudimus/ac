package com.atlassian.plugin.connect.jira.workflow;

import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean;
import static com.atlassian.plugin.connect.test.TestFileReader.readAddonTestFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkflowPostFunctionModuleBeanTest {
    private static final UrlBean ABSOLUTE_BEAN = new UrlBean("https://twitter.com");

    @Test
    public void verifyName() throws Exception {
        WorkflowPostFunctionModuleBean bean = createModuleBean();
        assertEquals("My Post Function", bean.getName().getValue());
    }

    @Test
    public void verifyDescription() throws Exception {
        WorkflowPostFunctionModuleBean bean = createModuleBean();
        assertEquals("Some description", bean.getDescription().getValue());
    }

    @Test
    public void verifyCreate() throws Exception {
        WorkflowPostFunctionModuleBean bean = createModuleBean();

        assertTrue(bean.hasCreate());
        assertEquals("/create", bean.getCreate().getUrl());
        assertFalse(bean.getCreate().createUri().isAbsolute());
    }

    @Test
    public void verifyEdit() throws Exception {
        WorkflowPostFunctionModuleBean bean = createModuleBean();

        assertTrue(bean.hasEdit());
        assertEquals("/edit", bean.getEdit().getUrl());
        assertFalse(bean.getEdit().createUri().isAbsolute());
    }

    @Test
    public void verifyView() throws Exception {
        WorkflowPostFunctionModuleBean bean = createModuleBean();

        assertTrue(bean.hasView());
        assertEquals("/view", bean.getView().getUrl());
        assertFalse(bean.getView().createUri().isAbsolute());
    }

    @Test
    public void verifyTriggered() throws Exception {
        WorkflowPostFunctionModuleBean bean = createModuleBean();

        assertTrue(bean.hasTriggered());
        assertEquals("/endpoint", bean.getTriggered().getUrl());
    }

    @Test
    public void verifyAddonMarshalling() throws Exception {
        String json = readTestFile();
        Gson gson = ConnectModulesGsonFactory.getGson();
        WorkflowPostFunctionModuleBean addon = gson.fromJson(json, WorkflowPostFunctionModuleBean.class);

        assertEquals("my-function", addon.getRawKey());
        assertEquals("My function", addon.getName().getValue());
        assertEquals("my.function", addon.getName().getI18n());
        assertEquals("My function explanation", addon.getDescription().getValue());
        assertEquals("my.function.desc", addon.getDescription().getI18n());

        assertEquals("/view", addon.getView().getUrl());
        assertEquals("/edit", addon.getEdit().getUrl());
        assertEquals("/create", addon.getCreate().getUrl());
        assertEquals("/triggered", addon.getTriggered().getUrl());

        assertTrue(addon.hasView());
        assertTrue(addon.hasEdit());
        assertTrue(addon.hasCreate());
        assertTrue(addon.hasTriggered());
    }

    @Test(expected = IllegalArgumentException.class)
    public void absoluteUrlsAreNotAllowedOnView() {
        newWorkflowPostFunctionBean().withView(ABSOLUTE_BEAN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void absoluteUrlsAreNotAllowedOnEdit() {
        newWorkflowPostFunctionBean().withEdit(ABSOLUTE_BEAN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void absoluteUrlsAreNotAllowedOnCreate() {
        newWorkflowPostFunctionBean().withCreate(ABSOLUTE_BEAN);
    }

    @Test(expected = IllegalArgumentException.class)
    public void absoluteUrlsAreNotAllowedOnTriggered() {
        newWorkflowPostFunctionBean().withTriggered(ABSOLUTE_BEAN);
    }

    private WorkflowPostFunctionModuleBean createModuleBean() {
        return newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Post Function", "my.pf.name"))
                .withDescription(new I18nProperty("Some description", "my.pf.desc"))
                .withCreate(new UrlBean("/create"))
                .withEdit(new UrlBean("/edit"))
                .withView(new UrlBean("/view"))
                .withTriggered(new UrlBean("/endpoint"))
                .build();
    }

    private static String readTestFile() throws IOException {
        return readAddonTestFile("workflowPostFunction.json");
    }
}
