package com.atlassian.plugin.connect.api.module.webitem;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;

public class WebItemModuleDescriptorData
{
    private final String url;
    private final String pluginKey;
    private final String moduleKey;
    private final boolean absolute;
    private final AddOnUrlContext addOnUrlContext;
    private final boolean isDialog;
    private final String section;

    private WebItemModuleDescriptorData(String url, String pluginKey, String moduleKey, boolean absolute, AddOnUrlContext addOnUrlContext, boolean isDialog, String section)
    {
        this.url = url;
        this.pluginKey = pluginKey;
        this.moduleKey = moduleKey;
        this.absolute = absolute;
        this.addOnUrlContext = addOnUrlContext;
        this.isDialog = isDialog;
        this.section = section;
    }

    public String getUrl()
    {
        return url;
    }

    public String getPluginKey()
    {
        return pluginKey;
    }

    public String getModuleKey()
    {
        return moduleKey;
    }

    public boolean isAbsolute()
    {
        return absolute;
    }

    public AddOnUrlContext getAddOnUrlContext()
    {
        return addOnUrlContext;
    }

    public boolean isDialog()
    {
        return isDialog;
    }

    public String getSection()
    {
        return section;
    }

    public static WebItemModuleDescriptorDataBuilder builder()
    {
        return new WebItemModuleDescriptorDataBuilder();
    }

    public static WebItemModuleDescriptorDataBuilder builder(WebItemModuleDescriptorData webItemModuleDescriptorData)
    {
        return new WebItemModuleDescriptorDataBuilder(webItemModuleDescriptorData);
    }

    static public class WebItemModuleDescriptorDataBuilder
    {
        private String url;
        private String pluginKey;
        private String moduleKey;
        private boolean absolute;
        private AddOnUrlContext addOnUrlContext;
        private boolean isDialog;
        private String section;

        public WebItemModuleDescriptorDataBuilder()
        {
        }

        public WebItemModuleDescriptorDataBuilder(WebItemModuleDescriptorData webItemModuleDescriptorData)
        {
            this.url = webItemModuleDescriptorData.getUrl();
            this.pluginKey = webItemModuleDescriptorData.getPluginKey();
            this.moduleKey = webItemModuleDescriptorData.getModuleKey();
            this.absolute = webItemModuleDescriptorData.isAbsolute();
            this.addOnUrlContext = webItemModuleDescriptorData.getAddOnUrlContext();
            this.isDialog = webItemModuleDescriptorData.isDialog();
            this.section = webItemModuleDescriptorData.getSection();
        }

        public WebItemModuleDescriptorDataBuilder setUrl(String url)
        {
            this.url = url;
            return this;
        }

        public WebItemModuleDescriptorDataBuilder setPluginKey(String pluginKey)
        {
            this.pluginKey = pluginKey;
            return this;
        }

        public WebItemModuleDescriptorDataBuilder setModuleKey(String moduleKey)
        {
            this.moduleKey = moduleKey;
            return this;
        }

        public WebItemModuleDescriptorDataBuilder setAbsolute(boolean absolute)
        {
            this.absolute = absolute;
            return this;
        }

        public WebItemModuleDescriptorDataBuilder setAddOnUrlContext(AddOnUrlContext addOnUrlContext)
        {
            this.addOnUrlContext = addOnUrlContext;
            return this;
        }

        public WebItemModuleDescriptorDataBuilder setIsDialog(boolean isDialog)
        {
            this.isDialog = isDialog;
            return this;
        }

        public WebItemModuleDescriptorDataBuilder setSection(String section)
        {
            this.section = section;
            return this;
        }

        public WebItemModuleDescriptorData build()
        {
            return new WebItemModuleDescriptorData(url, pluginKey, moduleKey, absolute, addOnUrlContext, isDialog, section);
        }
    }
}
