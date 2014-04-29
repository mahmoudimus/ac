package com.atlassian.plugin.connect.test.plugin.capabilities.beans;

import com.atlassian.plugin.connect.modules.beans.WorkflowPostFunctionModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.modules.beans.nested.UrlBean;
import com.atlassian.plugin.connect.modules.gson.ConnectModulesGsonFactory;
import com.google.gson.Gson;
import org.junit.Test;

import java.io.IOException;

import static com.atlassian.plugin.connect.test.plugin.capabilities.TestFileReader.readAddonTestFile;
import static org.junit.Assert.*;

public class WorkflowPostFunctionModuleBeanTest
{
    @Test
    public void verifyName() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = createModuleBean();
        assertEquals("My Post Function", bean.getName().getValue());
    }

    @Test
    public void verifyDescription() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = createModuleBean();
        assertEquals("Some description", bean.getDescription().getValue());
    }

    @Test
    public void verifyCreate() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = createModuleBean();

        assertTrue(bean.hasCreate());
        assertEquals("/create", bean.getCreate().getUrl());
        assertFalse(bean.getCreate().createUri().isAbsolute());
    }

    @Test
    public void verifyEdit() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = createModuleBean();

        assertTrue(bean.hasEdit());
        assertEquals("/edit", bean.getEdit().getUrl());
        assertFalse(bean.getEdit().createUri().isAbsolute());
    }

    @Test
    public void verifyView() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = createModuleBean();

        assertTrue(bean.hasView());
        assertEquals("/view", bean.getView().getUrl());
        assertFalse(bean.getView().createUri().isAbsolute());
    }

    @Test
    public void verifyTriggered() throws Exception
    {
        WorkflowPostFunctionModuleBean bean = createModuleBean();

        assertTrue(bean.hasTriggered());
        assertEquals("http://example.com/endpoint", bean.getTriggered().getUrl());
        assertTrue(bean.getTriggered().createUri().isAbsolute());
    }

    @Test
    public void verifyAddOnMarshalling() throws Exception
    {
        String json = readTestFile();
        System.out.println(json);
        Gson gson = ConnectModulesGsonFactory.getGson();
        WorkflowPostFunctionModuleBean addOn = gson.fromJson(json, WorkflowPostFunctionModuleBean.class);

        assertEquals("my-function", addOn.getRawKey());
        assertEquals("My function", addOn.getName().getValue());
        assertEquals("my.function", addOn.getName().getI18n());
        assertEquals("My function explanation", addOn.getDescription().getValue());
        assertEquals("my.function.desc", addOn.getDescription().getI18n());

        assertEquals("/view", addOn.getView().getUrl());
        assertEquals("/edit", addOn.getEdit().getUrl());
        assertEquals("/create", addOn.getCreate().getUrl());
        assertEquals("/triggered", addOn.getTriggered().getUrl());

        assertTrue(addOn.hasView());
        assertTrue(addOn.hasEdit());
        assertTrue(addOn.hasCreate());
        assertTrue(addOn.hasTriggered());
    }

    private WorkflowPostFunctionModuleBean createModuleBean()
    {
        return WorkflowPostFunctionModuleBean.newWorkflowPostFunctionBean()
                .withName(new I18nProperty("My Post Function", "my.pf.name"))
                .withDescription(new I18nProperty("Some description", "my.pf.desc"))
                .withCreate(new UrlBean("/create"))
                .withEdit(new UrlBean("/edit"))
                .withView(new UrlBean("/view"))
                .withTriggered(new UrlBean("http://example.com/endpoint"))
                .build();
    }

    private static String readTestFile() throws IOException
    {
        return readAddonTestFile("workflowPostFunction.json");
    }
}
