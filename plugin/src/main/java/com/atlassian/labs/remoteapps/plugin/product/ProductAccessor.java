package com.atlassian.labs.remoteapps.plugin.product;

import com.atlassian.mail.Email;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import java.util.Map;

/**
 * Product-specific accessors
 */
public interface ProductAccessor
{
    WebItemModuleDescriptor createWebItemModuleDescriptor();

    String getPreferredAdminSectionKey();

    int getPreferredAdminWeight();

    String getKey();

    int getPreferredGeneralWeight();

    String getPreferredGeneralSectionKey();

    int getPreferredProfileWeight();

    String getPreferredProfileSectionKey();

    Map<String,String> getLinkContextParams();

    void sendEmail(String user, Email email, String bodyAsHtml, String bodyAsText);

    void flushEmail();

    Map<String,Class<? extends Condition>> getConditions();
}
