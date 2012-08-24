package com.atlassian.labs.remoteapps.plugin.product.refapp;

import com.atlassian.labs.remoteapps.plugin.product.ProductAccessor;
import com.atlassian.mail.Email;
import com.atlassian.plugin.web.WebInterfaceManager;
import com.atlassian.plugin.web.descriptors.DefaultWebItemModuleDescriptor;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static java.util.Collections.emptyMap;

/**
 *
 */
public class RefappProductAccessor implements ProductAccessor
{
    private final WebInterfaceManager webInterfaceManager;
    private static final Logger log = LoggerFactory.getLogger(RefappProductAccessor.class);

    public RefappProductAccessor(WebInterfaceManager webInterfaceManager)
    {
        this.webInterfaceManager = webInterfaceManager;
    }

    @Override
    public WebItemModuleDescriptor createWebItemModuleDescriptor()
    {
        return new DefaultWebItemModuleDescriptor(webInterfaceManager);
    }

    @Override
    public String getPreferredAdminSectionKey()
    {
        return "system.admin/general";
    }

    @Override
    public int getPreferredAdminWeight()
    {
        return 10;
    }

    @Override
    public String getKey()
    {
        return "refapp";
    }

    @Override
    public int getPreferredGeneralWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredGeneralSectionKey()
    {
        return "index.links";
    }

    @Override
    public int getPreferredProfileWeight()
    {
        return 100;
    }

    @Override
    public String getPreferredProfileSectionKey()
    {
        return "system.admin/general";
    }

    @Override
    public Map<String, String> getLinkContextParams()
    {
        return emptyMap();
    }

    @Override
    public void sendEmail(String userName, Email email, String bodyAsHtml, String bodyAsText)
    {
        log.info("Would have sent email to " + userName + ": \n" + email);
    }
}
