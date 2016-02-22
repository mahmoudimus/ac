package com.atlassian.plugin.connect.confluence.blueprint;

import java.util.List;

import com.atlassian.plugin.connect.modules.beans.nested.BlueprintContextValue;

import com.google.gson.reflect.TypeToken;

/**
 * A type alias for the response from a blueprint template context request.
 */
public class BlueprintContextResponseTypeToken extends TypeToken<List<BlueprintContextValue>> {
}
