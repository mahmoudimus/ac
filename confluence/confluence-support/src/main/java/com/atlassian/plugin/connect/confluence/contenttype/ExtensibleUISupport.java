package com.atlassian.plugin.connect.confluence.contenttype;

import com.atlassian.confluence.content.ui.ContentUiSupport;
import com.atlassian.confluence.core.ConfluenceEntityObject;
import com.atlassian.confluence.search.v2.SearchResult;

public class ExtensibleUISupport implements ContentUiSupport
{
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
        return "";
    }

    @Override
    public String getContentTypeI18NKey(SearchResult searchResult) {
        return "";
    }
}
