package com.atlassian.labs.remoteapps.installer;

import com.atlassian.labs.remoteapps.PermissionDeniedException;
import com.atlassian.labs.remoteapps.PermissionManager;
import com.atlassian.labs.remoteapps.modules.page.jira.JiraProfileTabModuleGenerator;
import com.atlassian.labs.remoteapps.util.zip.ZipBuilder;
import com.atlassian.labs.remoteapps.util.zip.ZipHandler;
import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.consumer.ConsumerService;
import com.atlassian.oauth.util.RSAKeys;
import com.atlassian.plugin.JarPluginArtifact;
import com.atlassian.plugin.PluginController;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.net.*;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.ws.rs.core.UriBuilder;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URI;
import java.util.List;
import java.util.Set;

import static com.atlassian.labs.remoteapps.util.Dom4jUtils.getRequiredAttribute;
import static com.atlassian.labs.remoteapps.util.ServletUtils.encodeGetUrl;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Handles the remote app installation dance
 */
@Component
public class DefaultRemoteAppInstaller implements RemoteAppInstaller
{
    private final ConsumerService consumerService;
    private final RequestFactory requestFactory;
    private final PluginController pluginController;
    private final ApplicationProperties applicationProperties;
    private final PermissionManager permissionManager;

    private static final Set<String> ALLOWED_ACCESS_LEVELS = ImmutableSet.of(
            (System.getProperty("remoteapps.access.levels", "user").split(",")));

    @Autowired
    public DefaultRemoteAppInstaller(ConsumerService consumerService,
                                     RequestFactory requestFactory,
                                     PluginController pluginController,
                                     ApplicationProperties applicationProperties, PermissionManager permissionManager
    )
    {
        this.consumerService = consumerService;
        this.requestFactory = requestFactory;
        this.pluginController = pluginController;
        this.applicationProperties = applicationProperties;
        this.permissionManager = permissionManager;
    }

    @Override
    public void install(String username, final String registrationUrl, String registrationSecret) throws PermissionDeniedException
    {
        if (!permissionManager.canInstallRemoteApps(username))
        {
            throw new PermissionDeniedException("Unauthorized access by '" + username + "'");
        }
        Consumer consumer = consumerService.getConsumer();
        final URI registrationUri = URI.create(encodeGetUrl(registrationUrl, ImmutableMap.of(
                "key", consumer.getKey(),
                "publicKey", RSAKeys.toPemEncoding(consumer.getPublicKey()),
                "description", consumer.getDescription())));

        Request request = requestFactory.createRequest(Request.MethodType.GET, registrationUri.toString());
        try
        {
            request.execute(new ResponseHandler()
            {
                @Override
                public void handle(Response response) throws ResponseException
                {
                    if (response.getStatusCode() != 200)
                    {
                        throw new InstallationFailedException("Missing registration url: " + response.getStatusCode());
                    }
                    String descriptorXml = response.getResponseBodyAsString();
                    validateDescriptorXml(registrationUrl, descriptorXml);
                    String pluginXml = transformDescriptorToPluginXml(descriptorXml);
                    JarPluginArtifact jar = createJarPluginArtifact(registrationUri.getHost(), pluginXml);
                    pluginController.installPlugins(jar);
                }
            });
        }
        catch (ResponseException e)
        {
            throw new InstallationFailedException(e);
        }
    }

    private void validateDescriptorXml(String registrationUrl, String descriptorXml)
    {
        Document doc;
        try
        {
            doc = new SAXReader().read(new StringReader(descriptorXml));
        }
        catch (DocumentException e)
        {
            throw new InstallationFailedException("Invalid remote app xml: \n" + descriptorXml, e);
        }
        Element root = doc.getRootElement();

        if (root.attribute("rpc-url") != null)
        {
            throw new InstallationFailedException("rpc-url not allowed");
        }

        String displayUrl = root.attributeValue("display-url");
        if (displayUrl == null || !registrationUrl.startsWith(displayUrl))
        {
            throw new InstallationFailedException("display-url '" + displayUrl + "' must match registration URL");
        }

        String accessLevel = root.attributeValue("access-level");
        if (!ALLOWED_ACCESS_LEVELS.contains(accessLevel))
        {
            throw new InstallationFailedException("access-level '" + accessLevel + "' must be one of " + ALLOWED_ACCESS_LEVELS);
        }
    }

    private static JarPluginArtifact createJarPluginArtifact(String host, final String pluginXml)
    {
        return new JarPluginArtifact(ZipBuilder.buildZip("install-" + host, new ZipHandler()
        {
            @Override
            public void build(ZipBuilder builder) throws IOException
            {
                builder.addFile("atlassian-plugin.xml", pluginXml);
            }
        }));
    }

    private static String transformDescriptorToPluginXml(String descriptorXml)
    {
        try
        {
            Document doc = new SAXReader().read(new StringReader(descriptorXml));
            Element oldRoot = doc.getRootElement();

            Element plugin = doc.getRootElement().addElement("atlassian-plugin");
            plugin.detach();
            plugin.addAttribute("plugins-version", "2");
            plugin.addAttribute("key", getRequiredAttribute(oldRoot, "key"));
            plugin.addAttribute("name", getRequiredAttribute(oldRoot, "name"));
            Element info = plugin.addElement("plugin-info");
            info.addElement("version").setText(getRequiredAttribute(oldRoot, "version"));
            if (oldRoot.element("description") != null)
            {
                info.add(oldRoot.element("description").detach());
            }
            if (oldRoot.element("vendor") != null)
            {
                info.add(oldRoot.element("vendor").detach());
            }
            info.addElement("bundle-instructions")
                    .addElement("Import-Package")
                        .setText(JiraProfileTabModuleGenerator.class.getPackage().getName() + ";resolution:=optional");

            plugin.add(oldRoot.detach());
            doc.setRootElement(plugin);

            StringWriter out = new StringWriter();
            new XMLWriter(out).write(doc);
            descriptorXml = out.toString();
        }
        catch (DocumentException e)
        {
            throw new InstallationFailedException("Invalid document", e);
        }
        catch (IOException e)
        {
            throw new InstallationFailedException("Unable to transform descriptor", e);
        }
        return descriptorXml;
    }

}
