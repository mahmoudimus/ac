package com.atlassian.plugin.connect.bitbucket;

import javax.annotation.Nonnull;

public interface BitbucketAddonAccessor {

    @Nonnull
    String getDescriptor(@Nonnull String addonKey);

    @Nonnull
    String getSharedSecret(@Nonnull String addonKey);
}
