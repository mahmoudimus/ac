package com.atlassian.plugin.connect.api.descriptor;

import java.net.URL;

/**
 * A service for validating a JSON document against a JSON schema.
 */
public interface ConnectJsonSchemaValidator
{

    /**
     * Validates the given JSON descriptor against the given JSON schema.
     *
     * @param descriptor the JSON descriptor
     * @param schemaUrl the resource URL of a valid JSON schema
     * @return the descriptor schema validation result
     * @throws IllegalStateException if an exception occurs loading the schema
     */
    ConnectJsonSchemaValidationResult validate(String descriptor, URL schemaUrl);
}
