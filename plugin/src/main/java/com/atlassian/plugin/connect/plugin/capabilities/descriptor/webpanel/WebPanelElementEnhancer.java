package com.atlassian.plugin.connect.plugin.capabilities.descriptor.webpanel;

import org.dom4j.Element;
import org.dom4j.dom.DOMElement;
import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;

/**
 * Augment webPanels with conditional elements
 */
public class WebPanelElementEnhancer
{

    public static void enhance(WebPanelModuleBean bean, Element webPanelElement)
    {

        /**
         * AC-765 Jira Agile requires icon font and value to render a webPanel
         */
        if (bean.getLocation().equals("atl.gh.issue.details.tab"))
        {
            webPanelElement.addElement("param")
                    .addAttribute("name", "iconFont")
                    .addAttribute("value", "none");
        }
    }

}
