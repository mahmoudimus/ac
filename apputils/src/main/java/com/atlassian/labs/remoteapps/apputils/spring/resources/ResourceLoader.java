package com.atlassian.labs.remoteapps.apputils.spring.resources;

import com.atlassian.fugue.Option;

import java.io.InputStream;

/**
 * A simple interface to load resources as {@link InputStream}.
 */
public interface ResourceLoader
{
    Option<InputStream> load(String resource);
}
