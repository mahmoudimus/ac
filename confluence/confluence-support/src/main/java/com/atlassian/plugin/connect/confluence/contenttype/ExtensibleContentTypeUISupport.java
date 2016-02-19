package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.content.ui.ContentUiSupport;
import com.atlassian.confluence.core.ConfluenceEntityObject;
import com.atlassian.confluence.search.v2.SearchResult;
import com.atlassian.gzipfilter.org.apache.commons.lang.StringUtils;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;
import com.atlassian.plugin.connect.modules.beans.nested.contenttype.UISupportBean;

public class ExtensibleContentTypeUISupport implements ContentUiSupport
{
    private final String contentTypeName;
    private final String iconPath;

    ExtensibleContentTypeUISupport(String contentTypeName, ExtensibleContentTypeModuleBean bean)
    {
        UISupportBean uiSupport = bean.getUiSupport();

        this.contentTypeName = contentTypeName;
        this.iconPath = uiSupport.getIcons() == null ? "" : StringUtils.defaultString(uiSupport.getIcons().getItem());
    }

    @Override
    public String getIconFilePath(ConfluenceEntityObject confluenceEntityObject, int i)
    {
        return iconPath;
    }

    @Override
    public String getIconPath(ConfluenceEntityObject confluenceEntityObject, int i)
    {
        return iconPath;
    }

    @Override
    public String getLegacyIconPath(String s, int i)
    {
        return iconPath;
    }

    @Override
    public String getIconCssClass(ConfluenceEntityObject confluenceEntityObject)
    {
        return "aui-iconfont-file-generic";
    }

    @Override
    public String getContentCssClass(ConfluenceEntityObject confluenceEntityObject)
    {
        return "aui-iconfont-file-generic";
    }

    @Override
    public String getContentCssClass(String s, String s1)
    {
        return "aui-iconfont-file-generic";
    }

    @Override
    public String getIconCssClass(SearchResult searchResult)
    {
        return "aui-iconfont-file-generic";
    }

    @Override
    public String getContentTypeI18NKey(ConfluenceEntityObject confluenceEntityObject)
    {
        return contentTypeName;
    }

    @Override
    public String getContentTypeI18NKey(SearchResult searchResult)
    {
        return contentTypeName;
    }
}
