package com.atlassian.plugin.connect.bitbucket;

import com.atlassian.plugin.connect.plugin.ConnectAddonRegistry;
import com.atlassian.plugin.spring.scanner.annotation.component.BitbucketComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;

@BitbucketComponent
@ExportAsService(BitbucketAddonAccessor.class)
public class DefaultBitbucketAddonAccessor implements BitbucketAddonAccessor {

    private final ConnectAddonRegistry registry;

    @Autowired
    public DefaultBitbucketAddonAccessor(ConnectAddonRegistry registry) {
        this.registry = registry;
    }

    @Nonnull
    @Override
    public String getDescriptor(@Nonnull String addonKey) {
        return registry.getDescriptor(addonKey);
    }

    @Nonnull
    @Override
    public String getSharedSecret(@Nonnull String addonKey) {
        return registry.getSecret(addonKey);
    }
}
