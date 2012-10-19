package com.atlassian.plugin.remotable.plugin.product;

import com.atlassian.plugin.remotable.api.InstallationMode;
import com.atlassian.plugin.remotable.host.common.HostProperties;
import com.atlassian.mail.Email;
import com.atlassian.plugin.web.Condition;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;

import java.util.Map;
import java.util.Set;

/**
 * Product-specific accessors
 */
public interface ProductAccessor extends HostProperties
{
    WebItemModuleDescriptor createWebItemModuleDescriptor();

    String getPreferredAdminSectionKey();

    int getPreferredAdminWeight();

    int getPreferredGeneralWeight();

    String getPreferredGeneralSectionKey();

    int getPreferredProfileWeight();

    String getPreferredProfileSectionKey();

    Map<String,String> getLinkContextParams();

    void sendEmail(String user, Email email, String bodyAsHtml, String bodyAsText);

    void flushEmail();

    Map<String,Class<? extends Condition>> getConditions();

    Set<String> getAllowedPermissions(InstallationMode installationMode);
}
