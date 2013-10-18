package com.atlassian.plugin.connect.plugin.capabilities.util;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.Constants;

import aQute.lib.osgi.Builder;
import aQute.lib.osgi.Jar;

/**
 * @since version
 */
public class ConnectAddOnBundleBuilder
{
    public static final String CONNECT_FILE_SUFFIX = "_-atlassian-connect-_";
    private static final DateFormat BUILD_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final String ATLASSIAN_BUILD_DATE = "Atlassian-Build-Date";
    
    private final Map<String, byte[]> jarContents;
    private Map<String, String> manifestMap;

    public ConnectAddOnBundleBuilder()
    {
        this.jarContents = new HashMap<String, byte[]>();
    }

    public ConnectAddOnBundleBuilder manifest(Map<String, String> manifest)
    {
        this.manifestMap = manifest;
        return this;
    }

    /**
     * Adds a resource in the jar from a string
     *
     * @param path     The path for the jar entry
     * @param contents The contents of the file to create
     * @return
     */
    public ConnectAddOnBundleBuilder addResource(String path, String contents)
    {
        jarContents.put(path, contents.getBytes());
        return this;
    }

    /**
     * Builds a jar file from the provided information.  The file name is not guaranteed to match the jar name, as it is
     * created as a temporary file.
     * @param fileNamePrefix the filename to use minus the extension
     * @return The created jar plugin
     * @throws java.io.IOException
     */
    public File build(String fileNamePrefix)
    {
        File newJar = null;
        try
        {
            addManifest();
    
            File tempJarFile = createExtractableTempFile("delete-me-" + fileNamePrefix,".jar");
            
            ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(tempJarFile));
            addJarContents(jarContents,zout);
    
            zout.close();

            newJar = updateManifestInJar(fileNamePrefix, tempJarFile, manifestMap);
    
            FileUtils.forceDelete(tempJarFile);
        }
        catch (FileNotFoundException e)
        {
            throw new RuntimeException(e);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }


        return newJar;
    }

    private void addJarContents(Map<String, byte[]> contents, ZipOutputStream zout) throws IOException
    {
        for (Iterator i = contents.entrySet().iterator(); i.hasNext(); )
        {
            Map.Entry entry = (Map.Entry) i.next();
            zout.putNextEntry(new ZipEntry((String) entry.getKey()));
            zout.write((byte[]) entry.getValue());
            zout.closeEntry();
        }
    }

    private File updateManifestInJar(String fileNamePrefix, File oldJar, Map<String, String> manifest) throws Exception
    {
        //convert to OSGi bundle
        Builder bnd = new Builder();
        bnd.setJar(oldJar);
        bnd.calcManifest();

        if (null != manifest)
        {
            Manifest mf = new Manifest();
            for (Map.Entry<String, String> entry : manifest.entrySet())
            {
                mf.getMainAttributes().putValue(entry.getKey(), entry.getValue());
            }

            bnd.mergeManifest(mf);
        }

        Jar bundle = bnd.build();
        Manifest mergedManifest = bundle.getManifest();

                
        if(!mergedManifest.getMainAttributes().containsKey(Constants.BUNDLE_CLASSPATH))
        {
            mergedManifest.getMainAttributes().putValue(Constants.BUNDLE_CLASSPATH, ".");
        }

        
        File newJar = createExtractableTempFile(fileNamePrefix,".jar");
        ZipFile jarZip = new ZipFile(oldJar);
        ZipOutputStream append = new ZipOutputStream(new FileOutputStream(newJar));

        Enumeration<? extends ZipEntry> entries = jarZip.entries();
        while (entries.hasMoreElements())
        {
            ZipEntry e = entries.nextElement();

            if (e.isDirectory())
            {
                append.putNextEntry(e);
            }
            else if (!e.isDirectory() && !e.getName().equals("META-INF/MANIFEST.MF"))
            {
                append.putNextEntry(e);
                IOUtils.copy(jarZip.getInputStream(e), append);
            }
            else if (e.getName().equals("META-INF/MANIFEST.MF"))
            {
                ZipEntry mfe = new ZipEntry("META-INF/MANIFEST.MF");
                append.putNextEntry(mfe);
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                mergedManifest.write(bos);
                bos.close();

                IOUtils.copy(new ByteArrayInputStream(bos.toByteArray()), append);
            }
            
            append.closeEntry();
        }

        // close
        jarZip.close();
        append.close();
        
        return newJar;
    }
    
    private void addManifest() throws IOException
    {
        if(null == manifestMap)
        {
            manifestMap = new HashMap<String, String>();
        }

        final String buildDateStr = String.valueOf(BUILD_DATE_FORMAT.format(new Date()));
        manifestMap.put(ATLASSIAN_BUILD_DATE,buildDateStr);

        Manifest mf = new Manifest();
        for (Map.Entry<String, String> entry : manifestMap.entrySet())
        {
            mf.getMainAttributes().putValue(entry.getKey(),entry.getValue());
        }

        ByteArrayOutputStream bos = null;
        try
        {
             bos = new ByteArrayOutputStream();
            mf.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION,"1.0");
            mf.write(bos);
            String entries = bos.toString();
            
            addResource("META-INF/MANIFEST.MF", entries);
        }
        finally
        {
            IOUtils.closeQuietly(bos);
        }
        
    }
    
    public File createExtractableTempFile(String key, String suffix) throws IOException
    {
        return File.createTempFile(key + CONNECT_FILE_SUFFIX, suffix);
    }
}
