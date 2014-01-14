package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.plugin.scopes.AddOnScope;

import java.io.IOException;
import java.util.Collection;

public interface ScopeService
{
    /**
     * Build the scopes for the current product.
     * @return The scopes for the current product.
     */
    Collection<AddOnScope> build() throws IOException;
}
