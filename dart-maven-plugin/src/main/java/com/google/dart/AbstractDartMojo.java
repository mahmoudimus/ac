package com.google.dart;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class AbstractDartMojo extends AbstractMojo {

    /**
     * The directory to run the compiler from if fork is true.
     *
     * @since 1.0
     */
    @Parameter(defaultValue = "${basedir}", required = true, readonly = true)
    private File basedir;

    /**
     * @since 2.0.5
     */
    @Parameter(defaultValue = "src/main/dart", required = true)
    protected File sourceDirectory;

    /**
     * provide a dart-sdk
     *
     * @since 2.0.0
     */
    @Parameter(required = true, defaultValue = "${env.DART_SDK}")
    private File dartSdk;

    /**
     * The source directories containing the dart sources to be compiled.
     * <p/>
     * If not specified the default is 'sourceDirectory'.
     *
     * @since 1.0
     */
    @Parameter
    private List<File> compileSourceRoots = new ArrayList<File>();

    // ----------------------------------------------------------------------
    // Read-only parameters
    // ----------------------------------------------------------------------

    protected void checkDartSdk() {

        if (getLog().isDebugEnabled()) {
            getLog().debug("Check for DART_SDK.");
        }

        if (dartSdk == null) {
            throw new NullPointerException("Dart-sdk required. Configuration error for dartSdk?");
        }
        if (!dartSdk.isDirectory()) {
            throw new IllegalArgumentException("Dart-sdk required. Configuration error for dartSdk? dartSdk="
                + dartSdk.getAbsolutePath());
        }
        getLog().info("Dart-sdk configured to " + dartSdk);
        getLog().info("Version: " + readDartVersion());

    }

    protected File getDartSdk() {
        return dartSdk;
    }

    public File getBasedir() {
        return basedir;
    }

    protected List<File> getCompileSourceRoots() {
        if (compileSourceRoots.isEmpty()) {
            return Collections.singletonList(sourceDirectory);
        }
        return compileSourceRoots;
    }

    private String readDartVersion() {
        File dartVersionFile = new File(dartSdk, "version");
        if (!dartVersionFile.isFile()) {
            throw new IllegalArgumentException("Dart version file missing. Configuration error for dartSdk? dartSdk="
                + dartSdk.getAbsolutePath());
        }

        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader(dartVersionFile));

            final String dartVersion = in.readLine();
            if (StringUtils.isEmpty(dartVersion)) {
                throw new NullPointerException("Unable to read dart version. Configuration error for dartSdk?");
            }
            return dartVersion;
        } catch (IOException e) {
            throw new IllegalStateException("Unable to read dart version. Configuration error for dartSdk?", e);
        }
    }

    protected Set<File> findDartPackageRoots() throws MojoExecutionException {
        final Set<File> dartPackageRoots = new HashSet<File>();
        for (final File compileSourceRoot : getCompileSourceRoots()) {
            if (!compileSourceRoot.exists()) {
                throw new MojoExecutionException("Compiler-source-root '" + compileSourceRoot + "'  does not exist.");
            }
            if (!compileSourceRoot.isDirectory()) {
                throw new MojoExecutionException(
                    "Compiler-source-root '" + compileSourceRoot + "'  must be a directory.");
            }
            if (!compileSourceRoot.canRead()) {
                throw new MojoExecutionException("Compiler-source-root '" + compileSourceRoot + "'  must be readable.");
            }
            if (!compileSourceRoot.canWrite()) {
                throw new MojoExecutionException("Compiler-source-root '" + compileSourceRoot + "'  must be writable.");
            }

            if (getLog().isDebugEnabled()) {
                getLog().debug("Check compile-source-root '" + compileSourceRoot + "' for dart packages.");
            }

            final List<File> pubSpecs = new ArrayList<File>(
                FileUtils.listFiles(compileSourceRoot, new NameFileFilter("pubspec.yaml"),
                    DirectoryFileFilter.DIRECTORY));
            Collections.sort(pubSpecs, new Comparator<File>() {
                @Override
                public int compare(File f1, File f2) {
                    return f1.getAbsolutePath().compareTo(f2.getAbsolutePath());
                }
            });

            if (getLog().isDebugEnabled()) {
                getLog().debug("");
                final StringBuilder builder = new StringBuilder();
                builder.append("Found pubspec.yaml in ");
                builder.append(compileSourceRoot);
                builder.append(":\n");
                for (final File pubSpec : pubSpecs) {
                    builder.append("\t");
                    builder.append(pubSpec.getAbsolutePath().replace(compileSourceRoot + "/", ""));
                    builder.append("\n");
                }
                getLog().debug(builder.toString());
                getLog().debug("");
            }

            for (final File pubSpec : pubSpecs) {
                final File dartPackageRoot = pubSpec.getParentFile();
                if (isSubdirectory(dartPackageRoots, dartPackageRoot)) {
                    getLog().error("Recursive package root found " + dartPackageRoot.getAbsolutePath());
                    throw new MojoExecutionException("Found dart package root which is a subdirectory of another package.");
                }
                dartPackageRoots.add(dartPackageRoot);
            }
        }
        logDartPackageRoots(dartPackageRoots);
        return dartPackageRoots;
    }

    private boolean isSubdirectory(Set<File> dartPackageRoots, File dartPackageRootToCheck) {
        final String path = dartPackageRootToCheck.getAbsolutePath();
        for (final File root : dartPackageRoots) {
            if (path.startsWith(root.getAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    protected String relativePath(final File absolutePath) {
        return absolutePath.getAbsolutePath().replace(getBasedir() + "/", "");
    }

    private void logDartPackageRoots(final Set<File> dartPackageRoots) {
        getLog().info("");
        final StringBuilder builder = new StringBuilder();
        builder.append("Found package roots:\n");
        for (final File dartPackageRoot : dartPackageRoots) {
            builder.append("\t");
            builder.append(relativePath(dartPackageRoot));
            builder.append("\n");
        }
        getLog().info(builder.toString());
        getLog().info("");
    }

}
