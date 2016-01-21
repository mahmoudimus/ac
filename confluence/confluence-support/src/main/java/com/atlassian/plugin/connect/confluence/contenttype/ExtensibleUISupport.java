package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.content.ui.ContentUiSupport;
import com.atlassian.confluence.core.ConfluenceEntityObject;
import com.atlassian.confluence.search.v2.SearchResult;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;

public class ExtensibleUISupport implements ContentUiSupport
{
    private final ExtensibleContentTypeModuleBean bean;

    ExtensibleUISupport(ExtensibleContentTypeModuleBean bean)
    {
        this.bean = bean;
    }

    @Override
    public String getIconFilePath(ConfluenceEntityObject confluenceEntityObject, int i) {
        return "";
    }

    @Override
    public String getIconPath(ConfluenceEntityObject confluenceEntityObject, int i) {
        return "";
    }

    @Override
    public String getLegacyIconPath(String s, int i) {
        return "";
    }

    @Override
    public String getIconCssClass(ConfluenceEntityObject confluenceEntityObject) {
        return "";
    }

    @Override
    public String getContentCssClass(ConfluenceEntityObject confluenceEntityObject) {
        return "";
    }

    @Override
    public String getContentCssClass(String s, String s1) {
        return "";
    }

    @Override
    public String getIconCssClass(SearchResult searchResult) {
        return "";
    }

    @Override
    public String getContentTypeI18NKey(ConfluenceEntityObject confluenceEntityObject) {
        return bean.getUiSupport().getTypeName().getI18nOrValue();
    }

    @Override
    public String getContentTypeI18NKey(SearchResult searchResult) {
        return bean.getUiSupport().getTypeName().getI18nOrValue();
    }
}
