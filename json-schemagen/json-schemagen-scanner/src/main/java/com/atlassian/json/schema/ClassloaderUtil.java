package com.atlassian.json.schema;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClassloaderUtil
{
    public static ClassLoader getClassloader(String classpath)
    {
        List<String> pathList = Arrays.asList(classpath.split(File.pathSeparator));

        List<URL> urls = new ArrayList<URL>( pathList.size() );
        for ( String filename : pathList )
        {
            try
            {
                urls.add( new File( filename ).toURL() );
            }
            catch ( MalformedURLException e )
            {
                //ignore
            }
        }

        return new URLClassLoader((URL[]) urls.toArray(new URL[urls.size()]), Thread.currentThread().getContextClassLoader());
    }
}
