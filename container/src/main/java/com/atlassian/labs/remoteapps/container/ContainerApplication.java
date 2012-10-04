package com.atlassian.labs.remoteapps.container;

import com.atlassian.labs.remoteapps.container.internal.properties.ResourcePropertiesLoader;
import com.atlassian.plugin.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stats on the container and its version
 */
public class ContainerApplication implements Application
{
    private static final Logger log = LoggerFactory.getLogger(ContainerApplication.class);
    public static final Application INSTANCE = new ContainerApplication();

    private final String version;
    private ContainerApplication()
    {
        version = getVersionFromMavenMetadata("com.atlassian.labs", "remoteapps-container", "1.0.0.SNAPSHOT");
    }

    private String getVersionFromMavenMetadata(String groupId, String artifactId, String defaultValue)
    {
        final String resource = "META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties";

        String version = new ResourcePropertiesLoader(resource).load().get("version");
        if (version == null)
        {
            log.warn("Could not read version from maven metadata for {}:{} will use default {}", new Object[]{groupId, artifactId, defaultValue});
            return defaultValue;
        }
        version = version.replace('-', '.');
        log.debug("Version found from maven metadata for {}:{} is {}", new Object[]{groupId, artifactId, version});
        return version;
    }

    @Override
    public String getKey()
    {
        return "container";
    }

    @Override
    public String getVersion()
    {
        return version;
    }

    @Override
    public String getBuildNumber()
    {
        return "1";
    }
}
