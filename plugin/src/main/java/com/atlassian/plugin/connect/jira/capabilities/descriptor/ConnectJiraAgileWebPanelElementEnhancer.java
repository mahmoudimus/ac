package com.atlassian.plugin.connect.jira.capabilities.descriptor;

import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import org.dom4j.Element;

/**
 * Augment webPanels with conditional elements
 */
public class ConnectJiraAgileWebPanelElementEnhancer
{

    public static void enhance(WebPanelModuleBean bean, Element webPanelElement)
    {

        /**
         * AC-765 Jira Agile requires icon font and value to render a webPanel
         */
        if ("atl.gh.issue.details.tab".equals(bean.getLocation()))
        {
            webPanelElement.addElement("param")
                    .addAttribute("name", "iconFont")
                    .addAttribute("value", "none");
        }
    }
}
