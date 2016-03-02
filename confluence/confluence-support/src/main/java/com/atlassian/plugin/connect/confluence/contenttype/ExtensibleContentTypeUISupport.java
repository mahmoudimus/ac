package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.content.ui.ContentUiSupport;
import com.atlassian.confluence.core.ConfluenceEntityObject;
import com.atlassian.confluence.search.v2.SearchResult;
import com.atlassian.plugin.connect.modules.beans.ExtensibleContentTypeModuleBean;

public class ExtensibleContentTypeUISupport implements ContentUiSupport {
    private final String contentTypeName;

    ExtensibleContentTypeUISupport(String contentTypeName, ExtensibleContentTypeModuleBean bean) {
        this.contentTypeName = contentTypeName;
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
        return "aui-iconfont-file-generic";
    }

    @Override
    public String getContentCssClass(ConfluenceEntityObject confluenceEntityObject) {
        return "aui-iconfont-file-generic";
    }

    @Override
    public String getContentCssClass(String s, String s1) {
        return "aui-iconfont-file-generic";
    }

    @Override
    public String getIconCssClass(SearchResult searchResult) {
        return "aui-iconfont-file-generic";
    }

    @Override
    public String getContentTypeI18NKey(ConfluenceEntityObject confluenceEntityObject) {
        return contentTypeName;
    }

    @Override
    public String getContentTypeI18NKey(SearchResult searchResult) {
        return contentTypeName;
    }
}
