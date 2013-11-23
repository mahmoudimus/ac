package com.atlassian.maven.plugins.json.schemagen;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

import com.atlassian.json.schema.doclet.JsonSchemaDoclet;
import com.atlassian.json.schema.scanner.InterfaceImplementationScanner;

import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import static org.twdata.maven.mojoexecutor.MojoExecutor.*;
import static org.twdata.maven.mojoexecutor.MojoExecutor.element;
import static org.twdata.maven.mojoexecutor.MojoExecutor.name;

@Mojo(name = "generate-support-docs")
public class GenerateSupportDocsMojo extends AbstractSchemaGenMojo
{
    @Parameter
    private String basePackage = "";
    
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        runJavadoc();
        runInterfaceScanner();
    }

    private void runInterfaceScanner() throws MojoExecutionException
    {
        InterfaceImplementationScanner scanner = new InterfaceImplementationScanner();
        try
        {
            scanner.scan(basePackage,getClasspath(),new File(getDefaultInterfacesFile()));
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Error running interface scanner", e);
        }
    }

    private void runJavadoc() throws MojoExecutionException
    {
        Set<String> docletPaths = new HashSet<String>();
        StringBuffer docletPath = new StringBuffer(File.pathSeparator + project.getBuild().getOutputDirectory());

        try
        {
            docletPaths.addAll(project.getCompileClasspathElements());
            docletPaths.addAll(project.getRuntimeClasspathElements());
            docletPaths.addAll(project.getSystemClasspathElements());

            URL[] pluginUrls = ((URLClassLoader)Thread.currentThread().getContextClassLoader()).getURLs();
            for(URL pluginUrl : pluginUrls)
            {
                docletPaths.add(new File(pluginUrl.getFile()).getPath());
            }
            
            for(String path : docletPaths) {
                docletPath.append(File.pathSeparator);
                docletPath.append(path);
            }
        }
        catch (DependencyResolutionRequiredException e)
        {
            throw new MojoExecutionException("Dependencies must be resolved", e);
        }
        
        
        
        String resourcedocPath = getDefaultDocsFile();
        
        String packagePath = "**" + File.separator + basePackage.replaceAll("\\.", Matcher.quoteReplacement(File.separator)) + File.separator + "**" + File.separator + "*.java";
        executeMojo(
                plugin(
                        groupId("org.apache.maven.plugins"),
                        artifactId("maven-javadoc-plugin"),
                        version("2.9.1")
                ),
                goal("javadoc"),
                configuration(
                        element(name("maxmemory"),"1024m"),
                        element(name("doclet"), JsonSchemaDoclet.class.getName()),
                        element(name("docletPath"), getClasspath()),
                        element(name("additionalparam"),"-output \"" + resourcedocPath + "\""),
                        element(name("sourceFileIncludes"),element(name("sourceFileInclude"),packagePath)),
                        element(name("quiet"),"true"),
                        element(name("show"),"private"),
                        element(name("useStandardDocletOptions"),"false")
                        
                ),
                executionEnvironment()
        );
    }

}
