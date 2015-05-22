package com.atlassian.plugin.connect.spi.applinks;

import com.atlassian.applinks.spi.link.MutatingApplicationLinkService;

public interface MutatingApplicationLinkServiceProvider
{
    MutatingApplicationLinkService getMutatingApplicationLinkService();
}
