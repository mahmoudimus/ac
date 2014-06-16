package com.atlassian.connect.xmldescriptor;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.Set;

/**
 * Base class for finding annotated code and listing it in a text file in `target/classes/`.
 */
public abstract class CollectingAnnotationProcessor extends AbstractProcessor
{
    private Filer filer;
    private StringBuilder stringBuilder = new StringBuilder();
    private final Class<? extends Annotation> annotationType;

    public CollectingAnnotationProcessor(Class<? extends Annotation> annotationType)
    {
        this.annotationType = annotationType;
    }

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        stringBuilder.append(generateOutput(roundEnv));

        if (roundEnv.processingOver() && !roundEnv.errorRaised())
        {
            writeOutputToFile(stringBuilder.toString());
            stringBuilder = new StringBuilder();
        }

        return true;
    }

    private void writeOutputToFile(String outputString)
    {
        try
        {
            FileObject outfile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", annotationType.getSimpleName() + "_annotations.txt");
            Writer writer = outfile.openWriter();

            try
            {
                writer.write(outputString);
            }
            finally
            {
                writer.close();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private String generateOutput(RoundEnvironment roundEnv)
    {
        StringBuilder sb = new StringBuilder();

        for (Element element : roundEnv.getElementsAnnotatedWith(annotationType))
        {
            sb.append(String.format("%11s %s\n", element.getKind(), getName(element)));
        }

        return sb.toString();
    }

    private String getName(Element element)
    {
        switch (element.getKind())
        {
            case METHOD:
                return getEnclosingClassName(element) + '.' + element.getSimpleName() + "()";
            case FIELD:
                return getEnclosingClassName(element) + '.' + element.getSimpleName();
            case CONSTRUCTOR:
                return getEnclosingClassName(element) + '.' + getEnclosingClassName(element) + "()";
            default:
                return element.getSimpleName().toString();
        }
    }

    private String getEnclosingClassName(Element element)
    {
        return element.getEnclosingElement().getSimpleName().toString();
    }
}
