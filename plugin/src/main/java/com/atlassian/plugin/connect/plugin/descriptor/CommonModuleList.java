package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.ConnectPageModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebHookModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebItemModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import com.atlassian.plugin.connect.modules.beans.WebSectionModuleBean;

import java.util.List;

/**
 * A container class used for generation of JSON schema for common modules.
 */
@SuppressWarnings("UnusedDeclaration")
@ObjectSchemaAttributes(additionalProperties = false)
public class CommonModuleList extends BaseModuleBean
{

    /**
     * The Web Item module allows you to define new links in application menus.
     */
    private List<WebItemModuleBean> webItems;

    /**
     * The Web Panel module allows you to define panels, or sections, on an HTML page.
     * A panel is an iFrame that will be inserted into a page.
     */
    private List<WebPanelModuleBean> webPanels;

    /**
     * The Web Section plugin module allows you to define new sections in application menus.
     */
    private List<WebSectionModuleBean> webSections;

    /**
     * The Web Hook module allows you be notified of key events that occur in the host product
     */
    private List<WebHookModuleBean> webhooks;

    /**
     * A general page module is used to provide a generic chrome for add-on content in the product.
     */
    private List<ConnectPageModuleBean> generalPages;

    /**
     * An admin page module is used to provide an administration chrome for add-on content.
     */
    private List<ConnectPageModuleBean> adminPages;

    /**
     * A configure page module is a page module used to configure the addon itself.
     * It's link will appear in the add-ons entry in 'Manage Add-ons'.
     */
    private ConnectPageModuleBean configurePage;

    private CommonModuleList()
    {
    }
}
