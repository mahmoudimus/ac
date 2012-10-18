package com.atlassian.plugin.remotable.container.service.sal;

import com.atlassian.sal.api.ApplicationProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Properties;

/**
 * Simple implementation of application properties
 */
public final class ContainerApplicationProperties implements ApplicationProperties
{
    private final String baseUrl;

    public ContainerApplicationProperties(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }

    @Override
    public String getBaseUrl()
    {
        return baseUrl;
    }

    @Override
    public String getDisplayName()
    {
        return "Atlassian Remotable Plugins - Standalone Container";
    }

    @Override
    public String getVersion()
    {
        return MavenUtils.getVersion("com.atlassian.plugins", "remotable-plugins-container");
    }

    @Override
    public Date getBuildDate()
    {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getBuildNumber()
    {
        return String.valueOf(0);
    }

    @Override
    public File getHomeDirectory()
    {
        return new File(".");
    }

    @Override
    public String getPropertyValue(String key)
    {
        throw new UnsupportedOperationException();
    }

    private static final class MavenUtils
    {
        private static final Logger logger = LoggerFactory.getLogger(MavenUtils.class);

        private static final String UNKNOWN_VERSION = "unknown";

        static String getVersion(String groupId, String artifactId)
        {
            final Properties props = new Properties();
            InputStream resourceAsStream = null;
            try
            {
                resourceAsStream = MavenUtils.class.getResourceAsStream(String.format("/META-INF/maven/%s/%s/pom.properties", groupId, artifactId));
                props.load(resourceAsStream);
                return props.getProperty("version", UNKNOWN_VERSION);
            }
            catch (Exception e)
            {
                logger.debug("Could not find version for maven artifact {}:{}", groupId, artifactId);
                logger.debug("Got the following exception", e);
                return UNKNOWN_VERSION;
            }
            finally
            {
                if (resourceAsStream != null)
                {
                    try
                    {
                        resourceAsStream.close();
                    }
                    catch (IOException ioe)
                    {
                        // ignore
                    }
                }
            }
        }
    }
}
