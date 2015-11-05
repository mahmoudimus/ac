package com.atlassian.plugin.connect.plugin.auth.scope;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.PluginParseException;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.spi.scope.AddOnScope;
import com.atlassian.plugin.connect.spi.scope.helper.AddOnScopeLoadJsonFileHelper;
import com.atlassian.plugin.descriptors.AbstractModuleDescriptor;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.util.validation.ValidationPattern;
import com.google.gson.JsonSyntaxException;
import org.dom4j.Element;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static com.atlassian.plugin.util.validation.ValidationPattern.test;

public class ConnectApiScopeWhitelistModuleDescriptor extends AbstractModuleDescriptor<ConnectApiScopeWhitelist>
{

    private ConnectApiScopeWhitelist whitelist;

    public ConnectApiScopeWhitelistModuleDescriptor(ModuleFactory moduleFactory)
    {
        super(moduleFactory);
    }

    @Override
    public void init(Plugin plugin, Element element) throws PluginParseException
    {
        super.init(plugin, element);
        String resourcePath = element.attributeValue("resource");
        URL resource = plugin.getResource(resourcePath);
        assertResourceExists(resourcePath, resource);
        whitelist = loadWhitelist(resourcePath, resource);
    }

    @Override
    protected void provideValidationRules(ValidationPattern pattern)
    {
        super.provideValidationRules(pattern);
        pattern.
                rule(
                        test("@resource and string-length(@resource) > 0")
                                .withError("An API scope whitelist resource must be specified via the 'resource' attribute"));
    }

    @Override
    public ConnectApiScopeWhitelist getModule()
    {
        return whitelist;
    }

    private void assertResourceExists(String resourcePath, URL resource)
    {
        if (resource == null)
        {
            throw new PluginParseException(String.format("Unable to load API scope whitelist resource (%s)", resourcePath));
        }
    }

    private ConnectApiScopeWhitelist loadWhitelist(String resourcePath, URL resource)
    {
        Map<ScopeName, AddOnScope> scopes = new HashMap<>();
        try
        {
            AddOnScopeLoadJsonFileHelper.addProductScopesFromFile(scopes, resource);
        }
        catch (JsonSyntaxException e)
        {
            String errorMessage = String.format("Unable to parse API scope whitelist (%s) - invalid JSON: %s", resourcePath, e.getMessage());
            throw new PluginParseException(errorMessage, e);
        }
        catch (IOException e)
        {
            String errorMessage = String.format("Unable to parse API scope whitelist (%s): %s", resourcePath, e.getMessage());
            throw new PluginParseException(errorMessage, e);
        }
        return new ConnectApiScopeWhitelist(scopes);
    }
}
