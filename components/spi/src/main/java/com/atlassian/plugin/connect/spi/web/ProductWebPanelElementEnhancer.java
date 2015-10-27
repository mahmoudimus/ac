package com.atlassian.plugin.connect.spi.web;

import com.atlassian.plugin.connect.modules.beans.WebPanelModuleBean;
import org.dom4j.Element;

public interface ProductWebPanelElementEnhancer
{
    void enhance(WebPanelModuleBean bean, Element webPanelElement);
}
