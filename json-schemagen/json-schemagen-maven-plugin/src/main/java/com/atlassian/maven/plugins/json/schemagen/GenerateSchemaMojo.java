package com.atlassian.maven.plugins.json.schemagen;

import com.atlassian.json.schema.DefaultJsonSchemaGeneratorProvider;
import com.atlassian.json.schema.JsonSchemaGenerator;
import com.atlassian.json.schema.JsonSchemaGeneratorProvider;
import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.model.JsonSchema;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.FieldNamingStrategy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import static com.atlassian.json.schema.ClassloaderUtil.getClassloader;

@Mojo(name = "generate-schema")
public class GenerateSchemaMojo extends AbstractSchemaGenMojo
{
    public static final String DEFAULT_PROVIDER = "com.atlassian.json.schema.DefaultJsonSchemaGeneratorProvider";
    
    @Parameter
    private String rawOutput = "";

    @Parameter
    private String prettyOutput = "";

    @Parameter(required = true)
    private String rootClassName;

    @Parameter(defaultValue = "com.atlassian.json.schema.DefaultJsonSchemaGeneratorProvider")
    private String generatorProvider = DEFAULT_PROVIDER;

    @Parameter(defaultValue = "true")
    private Boolean lowercaseEnums = true;

    @Parameter
    private String ignoreFilter = "";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        //we have to check/set these here because project hasn't been injected yet up above
        if(Strings.isNullOrEmpty(rawOutput))
        {
            rawOutput = project.getBuild().getDirectory() + File.separator + "schema.json";
        }

        if(Strings.isNullOrEmpty(prettyOutput))
        {
            prettyOutput = project.getBuild().getDirectory() + File.separator + "schema-pretty.json";
        }
        
        ClassLoader oldClassloader = Thread.currentThread().getContextClassLoader();
        
        Thread.currentThread().setContextClassLoader(getClassloader(getClasspath()));
        try
        {
            Gson gson = new GsonBuilder().setFieldNamingStrategy(new SchemaFieldNamingStrategy()).create();
            Gson prettyGson = new GsonBuilder().setPrettyPrinting().setFieldNamingStrategy(new SchemaFieldNamingStrategy()).create();
            
            JsonSchemaGeneratorProvider provider = getProvider();
            JsonSchemaDocs schemaDocs = new JsonSchemaDocs();
            InterfaceList interfaceList = new InterfaceList();
            
            File docsFile = new File(getDefaultDocsFile());
            File interfacesFile = new File(getDefaultInterfacesFile());

            if (docsFile.exists() && docsFile.canRead())
            {
                String docsJson = Files.toString(docsFile, Charsets.UTF_8);
                schemaDocs = gson.fromJson(docsJson,JsonSchemaDocs.class);
            }

            if (interfacesFile.exists() && interfacesFile.canRead())
            {
                String ifaceJson = Files.toString(interfacesFile, Charsets.UTF_8);
                interfaceList = gson.fromJson(ifaceJson,InterfaceList.class);
            }

            JsonSchemaGenerator generator = provider.provide(lowercaseEnums,interfaceList,schemaDocs,ignoreFilter);
            JsonSchema schema = generator.generateSchema(getRootClass());
            
            File rawFile = new File(rawOutput);
            File prettyFile = new File(prettyOutput);
            
            Files.createParentDirs(rawFile);
            Files.createParentDirs(prettyFile);
            
            Files.write(gson.toJson(schema),new File(rawOutput),Charsets.UTF_8);
            Files.write(prettyGson.toJson(schema),new File(prettyOutput),Charsets.UTF_8);
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Unable to generate schema", e);
        }
        finally
        {
            Thread.currentThread().setContextClassLoader(oldClassloader);
        }
    }

    private Class<?> getRootClass() throws ClassNotFoundException
    {
        return Thread.currentThread().getContextClassLoader().loadClass(rootClassName);
    }

    private JsonSchemaGeneratorProvider getProvider() throws Exception
    {
        if (DefaultJsonSchemaGeneratorProvider.class.getName().equals(generatorProvider))
        {
            return new DefaultJsonSchemaGeneratorProvider();
        }
        
        Class<?> providerClass = Thread.currentThread().getContextClassLoader().loadClass(generatorProvider);
        Constructor ctr = providerClass.getConstructor();
        
        return (JsonSchemaGeneratorProvider) ctr.newInstance();
    }

    private class SchemaFieldNamingStrategy implements FieldNamingStrategy
    {
        @Override
        public String translateName(Field field)
        {
            if("enumList".equals(field.getName()))
            {
                return "enum";
            }
            else
            {
                return FieldNamingPolicy.IDENTITY.translateName(field);
            }
        }
    }
}
