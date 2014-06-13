package com.atlassian.plugin.connect.xmldescriptor;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;

@SupportedAnnotationTypes("com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor")
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class XmlDescriptorAnnotationProcessor extends AbstractProcessor
{
    private Filer filer;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        writeOutputToFile(generateOutput(roundEnv));

        return true;
    }

    private void writeOutputToFile(String outputString)
    {
        try
        {
            FileObject outfile = filer.createResource(StandardLocation.CLASS_OUTPUT, "", XmlDescriptor.class.getSimpleName() + "_annotations.txt");
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

        for (Element element : roundEnv.getElementsAnnotatedWith(XmlDescriptor.class))
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
