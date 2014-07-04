package it.com.atlassian.plugin.connect;

import com.google.common.base.Function;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypesException;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static com.google.common.collect.Collections2.transform;
import static java.util.Arrays.asList;

/**
 * Find {@link ParameterizedWiredTest}-annotated classes and translate them into test classes.
 */
@SupportedAnnotationTypes("it.com.atlassian.plugin.connect.ParameterizedWiredTest")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class ParameterizedWiredTestAnnotationProcessor extends AbstractProcessor
{
    private static final String GENERATED_CLASS_PREFIX = "Parameterized";
    private static final char NEWLINE = '\n';
    private static final String JAVA_LANG = "java.lang";

    private ProcessingEnvironment processingEnvironment;

    @Override
    public synchronized void init(final ProcessingEnvironment processingEnv)
    {
        super.init(processingEnv);
        this.processingEnvironment = processingEnv;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv)
    {
        try
        {
            for (Element element : roundEnv.getElementsAnnotatedWith(ParameterizedWiredTest.class))
            {
                try
                {
                    processingEnvironment.getMessager().printMessage(Diagnostic.Kind.NOTE, "Processing parameterized wired test class", element);
                    processClass((TypeElement)element);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }

            return true;
        }
        catch (MirroredTypesException e)
        {
            e.printStackTrace(System.out);
            throw e;
        }
    }

    private void processClass(TypeElement element) throws IOException
    {
        Collection<Element> testMethods = new ArrayList<Element>();
        Element parametersField = null;
        ParameterizedWiredTest.Parameters parametersFieldAnnotation = null;

        for (Element innerElement : element.getEnclosedElements())
        {
            collectTestMethods(testMethods, element, innerElement);

            Annotation fieldDataAnnotation = innerElement.getAnnotation(ParameterizedWiredTest.Parameters.class);

            if (null != fieldDataAnnotation)
            {
                if (parametersFieldAnnotation != null)
                {
                    throw new IllegalStateException(String.format("There can be only one %s-annotated field per %s-annotated class but %s has both %s and %s!",
                            ParameterizedWiredTest.Parameters.class.getSimpleName(), ParameterizedWiredTest.class.getSimpleName(), element.getSimpleName(), parametersField.getSimpleName(), innerElement.getSimpleName()));
                }

                TypeMirror fieldType = innerElement.asType();

                if (!(fieldType instanceof ArrayType))
                {
                    throw new IllegalStateException(String.format("%s-annotated fields must be of type Object[][] but %s is scalar!",
                            ParameterizedWiredTest.Parameters.class.getSimpleName(), innerElement.getSimpleName()));
                }

                ArrayType arrayFieldType = (ArrayType) fieldType;

                if (!(arrayFieldType.getComponentType() instanceof ArrayType))
                {
                    throw new IllegalStateException(String.format("%s-annotated fields must be of type Object[][] but %s is a one-dimensional array!",
                            ParameterizedWiredTest.Parameters.class.getSimpleName(), innerElement.getSimpleName()));
                }

                parametersField = innerElement;
                parametersFieldAnnotation = (ParameterizedWiredTest.Parameters) fieldDataAnnotation;

                if (parametersFieldAnnotation.length() <= 0)
                {
                    throw new IllegalStateException(String.format("%s-annotated fields must have length of greater than zero but %s has length %d!",
                            ParameterizedWiredTest.Parameters.class.getSimpleName(), innerElement.getSimpleName(), parametersFieldAnnotation.length()));
                }
            }
        }

        if (null == parametersFieldAnnotation)
        {
            throw new IllegalStateException(String.format("There must be a %s-annotated field per %s-annotated class but %s has none!",
                    ParameterizedWiredTest.Parameters.class.getSimpleName(), ParameterizedWiredTest.class.getSimpleName(), element.getSimpleName()));
        }

        if (testMethods.isEmpty())
        {
            throw new IllegalStateException(String.format("There must be at least one %s-annotated method per %s-annotated class but %s has none!",
                    ParameterizedWiredTest.Test.class.getSimpleName(), ParameterizedWiredTest.class.getSimpleName(), element.getSimpleName()));
        }

        generateTestClassSource(element, parametersFieldAnnotation);
    }

    private void collectTestMethods(Collection<Element> testMethods, TypeElement classElement, Element methodElement)
    {
        if (methodElement.getAnnotation(ParameterizedWiredTest.Test.class) != null)
        {
            testMethods.add(methodElement);
        }

        final TypeMirror superclassTypeMirror = classElement.getSuperclass();

        if (superclassTypeMirror instanceof DeclaredType)
        {
            collectTestMethods(testMethods, (TypeElement)((DeclaredType) superclassTypeMirror).asElement());
        }
    }

    private void collectTestMethods(Collection<Element> testMethods, TypeElement classElement)
    {
        collectTestMethods(testMethods, classElement, classElement.getEnclosedElements());
    }

    private void collectTestMethods(Collection<Element> testMethods, TypeElement classElement, Collection<? extends Element> classMembers)
    {
        for (Element classMember : classMembers)
        {
            collectTestMethods(testMethods, classElement, classMember);
        }
    }

    private void generateTestClassSource(TypeElement classElement, ParameterizedWiredTest.Parameters parametersFieldAnnotation) throws IOException
    {
        ClassName generatedClassName = constructClassName(classElement, GENERATED_CLASS_PREFIX);
        JavaFileObject generatedSourceFile = processingEnvironment.getFiler().createSourceFile(generatedClassName.getQualifiedName());
        final Writer writer = generatedSourceFile.openWriter();

        try
        {
            BufferedWriter bw = new BufferedWriter(writer);

            try
            {
                generateTestClassSourceUnsafe(classElement, parametersFieldAnnotation, generatedClassName, bw);
            }
            finally
            {
                bw.close();
            }
        }
        finally
        {
            writer.close();
        }
    }

    private void generateTestClassSourceUnsafe(Element element, ParameterizedWiredTest.Parameters parametersFieldAnnotation, ClassName generatedClassName, BufferedWriter bw) throws IOException
    {
        final Collection<ClassName> parameterClasses = getParameterClasses(parametersFieldAnnotation);
        writeClassHeader(bw, parameterClasses, generatedClassName.getPackageName());
        writeClassBody(element, parametersFieldAnnotation, generatedClassName, bw, parameterClasses);
    }

    private void writeClassBody(Element element, ParameterizedWiredTest.Parameters parametersFieldAnnotation, ClassName generatedClassName, BufferedWriter bw, Collection<ClassName> parameterClasses) throws IOException
    {
        bw.append("@RunWith(AtlassianPluginsTestRunner.class)").append(NEWLINE);
        bw.append("public class ").append(generatedClassName.getSimpleName()).append(" extends ").append(element.getSimpleName()).append(NEWLINE);
        bw.append("{").append(NEWLINE);
        bw.append("    public ").append(generatedClassName.getSimpleName()).append("(TestPluginInstaller testPluginInstaller, TestAuthenticator testAuthenticator,\n" +
                "                         JwtWriterFactory jwtWriterFactory,\n" +
                "                         ConnectAddonRegistry connectAddonRegistry,\n" +
                "                         ApplicationProperties applicationProperties)").append(NEWLINE); // TODO: find these args from the input class' constructor
        bw.append("    {").append(NEWLINE);
        bw.append("        super(testPluginInstaller, testAuthenticator, jwtWriterFactory, connectAddonRegistry, applicationProperties);").append(NEWLINE);
        bw.append("    }").append(NEWLINE);

        writeTests(parametersFieldAnnotation, bw, parameterClasses);

        bw.append("}").append(NEWLINE);
    }

    private void writeTests(ParameterizedWiredTest.Parameters parametersFieldAnnotation, BufferedWriter bw, Collection<ClassName> parameterClasses) throws IOException
    {
        for (int i = 0; i < parametersFieldAnnotation.length(); ++i)
        {
            bw.newLine();
            bw.append("    @Test").append(NEWLINE);
            bw.append("    public void ").append(constructTestMethodName(parametersFieldAnnotation.name(), i, parametersFieldAnnotation.length())).append("() throws Exception").append(NEWLINE);
            bw.append("    {").append(NEWLINE);

            bw.append("        super.test(");
            int paramIndex = 0;

            for (ClassName className : parameterClasses)
            {
                bw.append(String.format("(%s) super.data[%d][%d]%s", className.getSimpleName(), i, paramIndex, paramIndex < parameterClasses.size() - 1 ? ", " : ""));
                ++paramIndex;
            }

            bw.append(");").append(NEWLINE);
            bw.append("    }").append(NEWLINE);
        }
    }

    private void writeClassHeader(BufferedWriter bw, Collection<ClassName> parameterClasses, CharSequence packageName) throws IOException
    {
        bw.append("package ").append(packageName).append(";").append(NEWLINE);
        bw.newLine();
        bw.append("import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;").append(NEWLINE); // TODO: get from the input class' constructor
        bw.append("import com.atlassian.plugin.connect.testsupport.TestPluginInstaller;").append(NEWLINE);
        bw.append("import it.com.atlassian.plugin.connect.TestAuthenticator;").append(NEWLINE);
        bw.append("import com.atlassian.jwt.writer.JwtWriterFactory;").append(NEWLINE);
        bw.append("import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;").append(NEWLINE);
        bw.append("import com.atlassian.sal.api.ApplicationProperties;").append(NEWLINE);
        bw.append("import org.junit.Test;").append(NEWLINE);
        bw.append("import org.junit.runner.RunWith;").append(NEWLINE);

        for (ClassName className : parameterClasses)
        {
            if (!className.getPackageName().equals(JAVA_LANG))
            {
                bw.append("import ").append(className.getQualifiedName()).append(';').append(NEWLINE);
            }
        }

        bw.newLine();
    }

    private Collection<ClassName> getParameterClasses(ParameterizedWiredTest.Parameters parametersFieldAnnotation)
    {
        try
        {
            asList(parametersFieldAnnotation.classes()); // throws every time, because this API is wonderful
            throw new RuntimeException("I was expecting Parmeters.class() to throw a MirroredTypesException...");
        }
        catch (MirroredTypesException e)
        {
            return transform(e.getTypeMirrors(), new Function<TypeMirror, ClassName>()
            {
                @Override
                public ClassName apply(@Nullable TypeMirror typeMirror)
                {
                    return typeMirror instanceof DeclaredType ? constructClassName((TypeElement) (((DeclaredType) typeMirror).asElement())) : null;
                }
            });
        }
    }

    private CharSequence constructTestMethodName(String name, int i, int maxI)
    {
        return String.format("%s_%s", name, StringUtils.leftPad(String.valueOf(i), maxI / 10, '0')); // integer division
    }

    private static class ClassName
    {
        private final String packageName;
        private final String className;

        public ClassName(CharSequence packageName, CharSequence className)
        {
            this.packageName = packageName.toString();
            this.className = className.toString();
        }

        public String getQualifiedName()
        {
            return String.format("%s.%s", packageName, className);
        }

        public String getPackageName()
        {
            return packageName;
        }

        public String getSimpleName()
        {
            return className;
        }
    }

    private static ClassName constructClassName(TypeElement classElement)
    {
        return constructClassName(classElement, "");
    }

    private static ClassName constructClassName(TypeElement classElement, String classNamePrefix)
    {
        String simpleGeneratedClassName = String.format("%s%s", classNamePrefix, classElement.getSimpleName());
        PackageElement packageElement = (PackageElement) classElement.getEnclosingElement();
        return new ClassName(packageElement.getQualifiedName(), simpleGeneratedClassName);
    }
}
