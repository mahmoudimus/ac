package com.atlassian.labs.remoteapps.loader.external;

/**
 * Marker interface to ensure descriptors get generated before the spring context is available.
 */
public interface DescriptorGenerator
{
    public static final DescriptorGenerator NOOP_INSTANCE = new DescriptorGenerator()
    {
        @Override
        public void init() throws Exception
        {
        }
    };

    void init() throws Exception;
}
