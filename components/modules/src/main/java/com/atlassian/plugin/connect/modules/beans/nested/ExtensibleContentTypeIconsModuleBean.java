package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.ExtensibleContentTypeUISupportModuleBeanBuilder;

public class ExtensibleContentTypeIconsModuleBean extends BaseModuleBean
{
    private String createDialog;
    private String singleItem;
    private String collectionItem;

    public ExtensibleContentTypeIconsModuleBean(String createDialog, String singleItem, String collectionItem)
    {
        this.createDialog = createDialog;
        this.singleItem = singleItem;
        this.collectionItem = collectionItem;
    }

    public String getCreateDialog()
    {
        return createDialog;
    }

    public String getSingleItem()
    {
        return singleItem;
    }

    public String getCollectionItem()
    {
        return collectionItem;
    }
}
