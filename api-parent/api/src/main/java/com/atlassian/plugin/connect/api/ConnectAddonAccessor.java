package com.atlassian.plugin.connect.api;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Service which checks if Connect add-on is enabled.
 * @since 1.1.32
 */
public interface ConnectAddonAccessor
{
    /**
     * @param addOnKey key of the add-on to retrieve the descriptor for
     * @return the add-on descriptor, or {@code null} if no add-on is found for the provided {@code addOnKey}
     */
    @Nullable
    String getDescriptor(@Nonnull String addOnKey);

    /**
     * @param addOnKey key of the add-on to retrieve the shared secret for
     * @return the add-on shared secret, or {@code null} if no add-on is found for the provided {@code addOnKey}
     */
    @Nullable
    String getSharedSecret(@Nonnull String addOnKey);

    /**
     * Checks if the Connect add-on is installed and enabled.
     *
     * @param addOnKey key of the add-on to check
     * @return true if the Connect add-on is installed and enabled, otherwise false
     */
    boolean isAddonEnabled(@Nonnull String addOnKey);
}
