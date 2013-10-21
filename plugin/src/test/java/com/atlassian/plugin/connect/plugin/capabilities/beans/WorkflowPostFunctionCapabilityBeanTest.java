package com.atlassian.plugin.connect.plugin.capabilities.beans;

import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.I18nProperty;
import com.atlassian.plugin.connect.plugin.capabilities.beans.nested.UrlBean;
import com.atlassian.plugin.connect.plugin.capabilities.gson.CapabilitiesGsonFactory;
import com.google.gson.Gson;
import com.opensymphony.util.FileUtils;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class WorkflowPostFunctionCapabilityBeanTest
{
    @Test
    public void verifyName() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = createCapabilityBean();
        assertEquals("My Post Function", bean.getName().getValue());
    }

    @Test
    public void verifyDescription() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = createCapabilityBean();
        assertEquals("Some description", bean.getDescription().getValue());
    }

    @Test
    public void verifyCreate() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = createCapabilityBean();

        assertTrue(bean.hasCreate());
        assertEquals("/create", bean.getCreate().getUrl());
        assertFalse(bean.getCreate().createUri().isAbsolute());
    }

    @Test
    public void verifyEdit() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = createCapabilityBean();

        assertTrue(bean.hasEdit());
        assertEquals("/edit", bean.getEdit().getUrl());
        assertFalse(bean.getEdit().createUri().isAbsolute());
    }

    @Test
    public void verifyView() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = createCapabilityBean();

        assertTrue(bean.hasView());
        assertEquals("/view", bean.getView().getUrl());
        assertFalse(bean.getView().createUri().isAbsolute());
    }

    @Test
    public void verifyTriggered() throws Exception
    {
        WorkflowPostFunctionCapabilityBean bean = createCapabilityBean();

        assertTrue(bean.hasTriggered());
        assertEquals("http://example.com/endpoint", bean.getTriggered().getUrl());
        assertTrue(bean.getTriggered().createUri().isAbsolute());
    }

    @Test
    public void verifyAddOnMarshalling() throws Exception
    {
        String json = readTestFile();
        System.out.println(json);
        Gson gson = CapabilitiesGsonFactory.getGson();
        WorkflowPostFunctionCapabilityBean addOn = gson.fromJson(json, WorkflowPostFunctionCapabilityBean.class);

        assertEquals("myfunction", addOn.getKey());
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

    private WorkflowPostFunctionCapabilityBean createCapabilityBean()
    {
        return WorkflowPostFunctionCapabilityBean.newWorkflowPostFunctionBean()
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
        return FileUtils.readFile(new DefaultResourceLoader().getResource("classpath:/testfiles/capabilities/workflowPostFunction.json").getFile());
    }
}
