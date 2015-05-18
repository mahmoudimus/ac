package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.spi.scope.AddOnScope;

import java.io.IOException;
import java.util.Collection;

public interface ScopeService
{
    /**
     * Build the scopes for the current product.
     *
     * @return The scopes for the current product.
     * @throws IOException if an error occurred during scope loading
     */
    Collection<AddOnScope> build() throws IOException;
}
