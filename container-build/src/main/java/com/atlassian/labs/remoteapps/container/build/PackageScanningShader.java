package com.atlassian.labs.remoteapps.container.build;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.shade.DefaultShader;
import org.apache.maven.plugins.shade.Shader;
import org.apache.maven.plugins.shade.filter.Filter;
import org.apache.maven.plugins.shade.relocation.Relocator;
import org.apache.maven.plugins.shade.resource.ResourceTransformer;
import org.codehaus.plexus.component.annotations.Component;
import org.codehaus.plexus.logging.AbstractLogEnabled;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.twdata.pkgscanner.DefaultOsgiVersionConverter;
import org.twdata.pkgscanner.ExportPackage;
import org.twdata.pkgscanner.OsgiVersionConverter;
import org.twdata.pkgscanner.PackageScanner;
import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static org.twdata.pkgscanner.PackageScanner.*;

/**
 * A shader impl that will scan the result and add a package export report file to it
 */
@Component( role = Shader.class, hint = "pkgscanner" )
public class PackageScanningShader
        extends AbstractLogEnabled implements Shader
{
    private final DefaultShader delegate = new DefaultShader();
    private final DefaultOsgiVersionConverter versionConverter = new
            DefaultOsgiVersionConverter();

    @Override
    public void shade( Set<File> toShadeJars, File uberJar, List<Filter> filters, List<Relocator> relocators,
            List<ResourceTransformer> resourceTransformers) throws IOException,
            MojoExecutionException
    {
        delegate.enableLogging(getLogger());
        delegate.shade(toShadeJars, uberJar, filters, relocators, resourceTransformers);
        Yaml yaml = new Yaml();
        File yamlFile = new File(uberJar.getParentFile(), "classes/pkgscanner-config.yaml");
        Map<String, Object> config = (Map<String, Object>) yaml.load(
                FileUtils.readFileToString(yamlFile));

        Map<String,Collection<String>> packageIncludes = (Map<String, Collection<String>>) config.get("package_includes");
        Collection<String> includes = packageIncludes.get("include");
        Collection<String> excludes = packageIncludes.get("exclude");
        Map<String,String> versions = getScannedVersions(toShadeJars);
        for (Map<String,String> entry : (Collection<Map<String,String>>) config.get("package_versions"))
        {
            versions.put(entry.get("package"), versionConverter.getVersion(entry.get("version")));
        }
        Collection < ExportPackage > exports = new PackageScanner()
                .select(
                        jars(include("*.jar"), exclude()),
                        packages(
                                include(includes.toArray(new String[includes.size()])),
                                exclude(excludes.toArray(new String[excludes.size()]))))
                .withMappings(versions)
                .useClassLoader(new URLClassLoader(new URL[]{uberJar.toURI().toURL()},
                        null))
                .scan();
        Element root = DocumentFactory.getInstance().createDocument().addElement("exports");
        for (ExportPackage export : exports)
        {
            root.addElement("export")
                    .addAttribute("package", export.getPackageName())
                    .addAttribute("version", export.getVersion())
                    .addAttribute("location", export.getLocation().getName());
        }
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        new XMLWriter(bout, OutputFormat.createPrettyPrint()).write(root.getDocument());
        addFileToExistingZip(uberJar, "package-scanner-exports.xml",
                new ByteArrayInputStream(bout.toByteArray()));
    }

    private Map<String, String> getScannedVersions(Set<File> toShadeJars)
    {
        Map<String, String> versions = new HashMap<String, String>();
        for (File file : toShadeJars) {
            JarFile jarFile = null;
            try
            {
                jarFile = new JarFile(file);
                Manifest mf = jarFile.getManifest();
                if (mf != null && mf.getMainAttributes() != null)
                {
                    String version = mf.getMainAttributes().getValue("Bundle-Version");

                    if (version == null) {
                        version = mf.getMainAttributes().getValue("Specification-Version");
                    }

                    if (version == null) {
                        version = mf.getMainAttributes().getValue("Implementation-Version");
                    }

                    if (version == null) {
                        version = determineVersionFromMavenProperties(jarFile);
                    }

                    if (version != null)
                    {
                        String osgiVersion = versionConverter.getVersion(version);
                        for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
                            JarEntry entry = e.nextElement();
                            if (entry.isDirectory())
                            {
                                versions.put(entry.getName().substring(0, entry.getName().length() - 1).replace(
                                        '/', '.'), osgiVersion);
                            }
                        }
                    }
                }
            }
            catch (IOException e)
            {
                getLogger().error("Unable to scan shaded jar " + file.getName(), e);
            }
        }
        return versions;
    }

    private String determineVersionFromMavenProperties(JarFile jarFile)
    {
        for (Enumeration<JarEntry> e = jarFile.entries(); e.hasMoreElements(); ) {
            JarEntry entry = e.nextElement();
            if (entry.getName().endsWith("/pom.properties")) {
                InputStream in = null;
                try {
                    in = jarFile.getInputStream(entry);
                    Properties props = new Properties();
                    props.load(in);
                    return props.getProperty("version");
                }
                catch (IOException ex) {
                    getLogger().debug("Exception reading maven properties file", ex);
                }
                finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException ex) {
                            // ignore
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void addFileToExistingZip(File zipFile,
            String path, InputStream inputStream) throws IOException {
        // get a temp file
        File tempFile = File.createTempFile(zipFile.getName(), null);
        // delete it, otherwise you cannot rename your existing zip to it.
        tempFile.delete();

        boolean renameOk=zipFile.renameTo(tempFile);
        if (!renameOk)
        {
            throw new RuntimeException("could not rename the file "+zipFile.getAbsolutePath()+" to "+tempFile.getAbsolutePath());
        }
        byte[] buf = new byte[1024];

        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream out = null;
        try
        {
            out = new ZipOutputStream(new FileOutputStream(zipFile));
            ZipEntry entry = zin.getNextEntry();
            while (entry != null) {
                String name = entry.getName();
                if (path.equals(name)) {
                    break;
                }
                // Add ZIP entry to output stream.
                out.putNextEntry(new ZipEntry(name));
                // Transfer bytes from the ZIP file to the output file
                int len;
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                entry = zin.getNextEntry();
            }
            // Close the streams
            zin.close();
            // Add ZIP entry to output stream.
            out.putNextEntry(new ZipEntry(path));
            // Transfer bytes from the file to the ZIP file
            int len;
            while ((len = inputStream.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            // Complete the entry
            out.closeEntry();
        }
        finally
        {
            // Complete the ZIP file
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(inputStream);
            tempFile.delete();
        }
    }
}
