package com.atlassian.plugin.connect.processor;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.springframework.stereotype.Component;

public class ComponentAnnotationProcessor extends AbstractProcessor
{
    public static final String ANNOTATED_INDEX_PREFIX = "META-INF/annotations/";
    private Set<TypeElement> annotatedTypes = new HashSet<TypeElement>();
    private Set<String> indexedAnnotations;
    private Filer filer;

    public ComponentAnnotationProcessor()
    {
    }

    @Override
    public SourceVersion getSupportedSourceVersion()
    {
        return SourceVersion.latest();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes()
    {
        return Sets.newHashSet("*");
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        try
        {
            for (Element element : roundEnv.getRootElements())
            {
                if (!(element instanceof TypeElement))
                {
                    continue;
                }

                TypeElement typeElement = (TypeElement) element;

                for (AnnotationMirror mirror : element.getAnnotationMirrors())
                {
                    TypeElement annotationElement = (TypeElement) mirror.getAnnotationType().asElement();

                    if (annotationElement.getQualifiedName().toString().equals(Component.class.getName()) || annotationElement.getAnnotation(Component.class) != null)
                    {
                        annotatedTypes.add(typeElement);
                    }
                }
            }

            if (!roundEnv.processingOver())
            {
                return false;
            }

            writeIndexFile(annotatedTypes, ANNOTATED_INDEX_PREFIX + Component.class.getName());

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }

        return false;
    }

    private void readOldIndexFile(Set<String> entries, String resourceName) throws IOException
    {
        BufferedReader reader = null;
        try
        {
            FileObject resource = filer.getResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
            Reader resourceReader = resource.openReader(true);
            reader = new BufferedReader(resourceReader);

            String line = reader.readLine();
            while (line != null)
            {
                entries.add(line);
                line = reader.readLine();
            }
        }
        catch (FileNotFoundException e)
        {
        }
        catch (IOException e)
        {
            // Thrown by Eclipse JDT when not found
        }
        catch (UnsupportedOperationException e)
        {
            // Java6 does not support reading old index files
        }
        finally
        {
            if (reader != null)
            {
                reader.close();
            }
        }
    }

    private void writeIndexFile(Iterable<TypeElement> elementList, String resourceName)
            throws IOException
    {
        Set<String> entries = new HashSet<String>();
        for (TypeElement element : elementList)
        {
            entries.add(element.getQualifiedName().toString());
        }

        readOldIndexFile(entries, resourceName);

        FileObject file = filer.createResource(StandardLocation.CLASS_OUTPUT, "", resourceName);
        Writer writer = file.openWriter();
        for (String entry : entries)
        {
            writer.write(entry);
            writer.write("\n");
        }
        writer.close();
    }
}
