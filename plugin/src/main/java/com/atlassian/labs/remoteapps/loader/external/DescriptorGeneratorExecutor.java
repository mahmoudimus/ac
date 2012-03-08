package com.atlassian.labs.remoteapps.loader.external;

/**
 * Executes the descriptor generator to initialize the remote app
 */
public class DescriptorGeneratorExecutor
{
    public DescriptorGeneratorExecutor(DescriptorGenerator descriptorGenerator) throws Exception
    {
        descriptorGenerator.init();
    }
}
