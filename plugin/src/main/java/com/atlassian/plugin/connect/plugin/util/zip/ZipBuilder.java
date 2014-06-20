package com.atlassian.plugin.connect.plugin.util.zip;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;
import com.atlassian.plugin.connect.plugin.xmldescriptor.XmlDescriptorExploder;
import org.apache.commons.io.IOUtils;
import org.dom4j.Document;
import org.dom4j.io.XMLWriter;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
*
*/
@XmlDescriptor
public class ZipBuilder
{
    public static final String KEY_SEPARATOR = "----remotableplugins-";
    private final ZipOutputStream zout;

    public ZipBuilder(ZipOutputStream zout)
    {
        this.zout = zout;
    }

    public static File buildZip(String identifier, ZipHandler handler)
    {
        XmlDescriptorExploder.notifyAndExplode(identifier);

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
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finally
        {
            IOUtils.closeQuietly(zout);
        }
        return tmpFile;
    }


    public void addFile(String path, InputStream contents) throws IOException
    {
        XmlDescriptorExploder.notifyAndExplode(null);

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
        XmlDescriptorExploder.notifyAndExplode(null);

        ZipEntry entry = new ZipEntry(path);
        zout.putNextEntry(entry);
        IOUtils.copy(new StringReader(contents), zout);
    }

    public static File createExtractableTempFile(String key, String suffix) throws IOException
    {
        XmlDescriptorExploder.notifyAndExplode(key);

        return File.createTempFile(key + KEY_SEPARATOR, suffix);
    }

    public void addFile(String path, Document document) throws IOException
    {
        XmlDescriptorExploder.notifyAndExplode(null == document ? null : document.getRootElement().attributeValue("key"));

        StringWriter out = new StringWriter();
        new XMLWriter(out).write(document);
        addFile(path, out.toString());
    }
}
