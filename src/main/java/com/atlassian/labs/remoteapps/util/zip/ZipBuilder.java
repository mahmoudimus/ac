package com.atlassian.labs.remoteapps.util.zip;

import org.apache.commons.io.IOUtils;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
*
*/
public class ZipBuilder
{
    public static final String KEY_SEPARATOR = "----remoteapps-";
    private final ZipOutputStream zout;

    public ZipBuilder(ZipOutputStream zout)
    {
        this.zout = zout;
    }

    public static File buildZip(String identifier, ZipHandler handler)
    {
        ZipOutputStream zout = null;
        File tmpFile = null;
        try
        {
            tmpFile = createExtractableTempFile(identifier, ".jar");
            zout = new ZipOutputStream(new FileOutputStream(tmpFile));
            ZipBuilder builder = new ZipBuilder(zout);
            handler.build(builder);
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        catch (IOException e)
        {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        finally
        {
            IOUtils.closeQuietly(zout);
        }
        return tmpFile;
    }


    public void addFile(String path, InputStream contents) throws IOException
    {
        try
        {
            ZipEntry entry = new ZipEntry(path);
            zout.putNextEntry(entry);
            IOUtils.copy(contents, zout);
        }
        finally
        {
            IOUtils.closeQuietly(contents);
        }
    }

    public void addFile(String path, String contents) throws IOException
    {
        ZipEntry entry = new ZipEntry(path);
        zout.putNextEntry(entry);
        IOUtils.copy(new StringReader(contents), zout);
    }

    public static File createExtractableTempFile(String key, String suffix) throws IOException
    {
        return File.createTempFile(key + KEY_SEPARATOR, suffix);
    }

}
