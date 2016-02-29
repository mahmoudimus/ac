package com.atlassian.plugin.connect.confluence.blueprint;

import com.atlassian.plugin.connect.modules.beans.nested.BlueprintContextValue;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * A type alias for the response from a blueprint template context request.
 */
public class BlueprintContextResponseTypeToken extends TypeToken<List<BlueprintContextValue>> {
}
