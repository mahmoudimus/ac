package com.atlassian.plugin.remotable.plugin.util.zip;

import java.io.IOException;

/**
*
*/
public interface ZipHandler
{
    void build(ZipBuilder builder) throws IOException;
}
