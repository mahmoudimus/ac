package com.atlassian.plugin.connect.plugin.iframe.servlet;

import java.io.IOException;
import java.util.*;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.templaterenderer.TemplateRenderer;

import org.apache.commons.lang3.StringUtils;

public class DialogOptionsServlet extends HttpServlet
{
    public static final String DIALOG_OPTION_PREFIX = "-acopt-";
    private static final String OPTION_TEMPLATE = "/velocity/dialog-options.vm";
    private final PluginAccessor pluginAccessor;
    private final TemplateRenderer templateRenderer;

    public DialogOptionsServlet(WebInterfaceManager webInterfaceManager, PluginAccessor pluginAccessor, TemplateRenderer templateRenderer)
    {
        this.pluginAccessor = pluginAccessor;
        this.templateRenderer = templateRenderer;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        List<WebItemModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(WebItemModuleDescriptor.class);
        Map<String,Map<String,String>> keyedDialogOptions = new HashMap<String, Map<String, String>>();
        Map<String,Map<String,String>> keyedInlineDialogOptions = new HashMap<String, Map<String, String>>();
        
        for(WebItemModuleDescriptor descriptor : descriptors)
        {
            if(descriptor.getStyleClass().contains("ap-dialog"))
            {
                addDialogOptions(descriptor,keyedDialogOptions);
            }
            else if(descriptor.getStyleClass().contains("ap-inline-dialog"))
            {
                addDialogOptions(descriptor,keyedInlineDialogOptions);
            }
        }
        
        Map<String,Object> context = new HashMap<String, Object>();
        context.put("acDialogOptions",keyedDialogOptions);
        context.put("acInlineDialogOptions",keyedInlineDialogOptions);
        
        resp.setContentType("text/html;charset=utf-8");
        templateRenderer.render(OPTION_TEMPLATE, context, resp.getWriter());
        
    }

    private void addDialogOptions(WebItemModuleDescriptor descriptor, Map<String, Map<String, String>> keyedOptions)
    {
        for(Map.Entry<String,String> param : descriptor.getParams().entrySet())
        {
            if(param.getKey().startsWith(DIALOG_OPTION_PREFIX))
            {
                addOption(keyedOptions,descriptor.getCompleteKey(),param.getKey(),param.getValue());
            }
        }
    }

    private void addOption(Map<String, Map<String, String>> keyedOptions, String moduleKey, String key, String value)
    {
        if(!keyedOptions.containsKey(moduleKey))
        {
            keyedOptions.put(moduleKey,new HashMap<String, String>());
        }
        
        keyedOptions.get(moduleKey).put(StringUtils.substringAfter(key,DIALOG_OPTION_PREFIX),value);
    }
}
