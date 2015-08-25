package com.atlassian.plugin.connect.api.module.webitem;

import com.atlassian.plugin.connect.modules.beans.AddOnUrlContext;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import com.atlassian.plugin.web.model.WebLink;

public interface WebLinkFactory
{
    WebLink createRemoteWebLink(WebItemModuleDescriptor remoteConfluenceWebItemModuleDescriptor, WebItemModuleDescriptorData webItemModuleDescriptorData);
}
