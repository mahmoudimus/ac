package com.atlassian.maven.plugins.json.schemagen;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.BuildPluginManager;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.PluginManager;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.*;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.sonatype.aether.RepositorySystemSession;
import org.sonatype.aether.graph.Dependency;
import org.twdata.maven.mojoexecutor.MojoExecutor;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.twdata.maven.mojoexecutor.MojoExecutor.configuration;

public abstract class AbstractSchemaGenMojo extends AbstractMojo
{
    @Component
    protected MavenProject project;

    @Component
    private BuildPluginManager buildPluginManager;

    @Component
    private PluginManager pluginManager;
    
    @Component
    private MavenSession session;
    
    @Component
    private ProjectDependenciesResolver resolver;
    
    @Parameter(defaultValue = "${repositorySystemSession}")
    private RepositorySystemSession reposession;

    @Parameter(defaultValue = "false")
    protected Boolean debug = false;

    public MojoExecutor.ExecutionEnvironment executionEnvironment()
    {
        if (buildPluginManager != null)
        {
            /* Maven 3 */
            return MojoExecutor.executionEnvironment(project, session, buildPluginManager);
        }
        else
        {
            /* Maven 2 */
            return MojoExecutor.executionEnvironment(project, session, pluginManager);
        }
    }

    protected static Xpp3Dom configurationWithoutNullElements(MojoExecutor.Element... elements)
    {
        List<MojoExecutor.Element> nonNullElements = new ArrayList<MojoExecutor.Element>();
        for (MojoExecutor.Element e : elements)
        {
            if (e != null)
            {
                nonNullElements.add(e);
            }
        }

        return configuration(nonNullElements.toArray(new MojoExecutor.Element[nonNullElements.size()]));
    }
    
    protected String getDefaultDocsFile()
    {
        StringBuilder outBuilder = new StringBuilder(project.getBuild().getDirectory());
        outBuilder.append(File.separator).append("jsonSchemaDocs.json");
        
        return outBuilder.toString();
    }

    protected String getDefaultInterfacesFile()
    {
        StringBuilder outBuilder = new StringBuilder(project.getBuild().getDirectory());
        outBuilder.append(File.separator).append("jsonSchemaInterfaces.json");
        
        return outBuilder.toString();
    }

    protected String getClasspath() throws MojoExecutionException
    {
        Set<String> docletPaths = new HashSet<String>();
        StringBuffer docletPath = new StringBuffer(File.pathSeparator + project.getBuild().getOutputDirectory());

        try
        {
            docletPaths.addAll(project.getCompileClasspathElements());
            docletPaths.addAll(project.getRuntimeClasspathElements());
            docletPaths.addAll(project.getSystemClasspathElements());

            DependencyResolutionResult result = resolver.resolve(new DefaultDependencyResolutionRequest(project, reposession));
            
            for(Dependency dep : result.getDependencies())
            {
                docletPaths.add(dep.getArtifact().getFile().getPath());
            }
            
            URL[] pluginUrls = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
            for(URL pluginUrl : pluginUrls)
            {
                docletPaths.add(new File(pluginUrl.getFile()).getPath());
            }

            for(String path : docletPaths) {
                docletPath.append(File.pathSeparator);
                docletPath.append(path);
            }

            return docletPath.toString();
        }
        catch (DependencyResolutionRequiredException e)
        {
            throw new MojoExecutionException("Dependencies must be resolved", e);
        }
        catch (DependencyResolutionException e)
        {
            throw new MojoExecutionException("Dependencies must be resolved", e);
        }

    }
}
