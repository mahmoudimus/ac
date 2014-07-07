package it.com.atlassian.plugin.connect;

import com.google.common.base.Function;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;

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
    private static final Function<TypeMirror, ClassName> TYPE_MIRROR_TO_CLASS_NAME = new Function<TypeMirror, ClassName>()
    {
        @Override
        public ClassName apply(@Nullable TypeMirror typeMirror)
        {
            return typeMirror instanceof DeclaredType ? constructClassName((TypeElement) (((DeclaredType) typeMirror).asElement())) : null;
        }
    };

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
        catch (RuntimeException e)
        {
            e.printStackTrace(System.out);
            throw e;
        }
    }

    private void processClass(TypeElement inputClassElement) throws IOException
    {
        final Collection<ExecutableElement> testMethods = collectTestMethods(inputClassElement);

        if (testMethods.isEmpty())
        {
            throw new IllegalStateException(String.format("There must be at least one %s-annotated method per %s-annotated class but %s has none!",
                    ParameterizedWiredTest.Test.class.getSimpleName(), ParameterizedWiredTest.class.getSimpleName(), inputClassElement.getSimpleName()));
        }

        Element parametersField = null;
        ParameterizedWiredTest.Parameters parametersFieldAnnotation = null;

        for (Element innerElement : inputClassElement.getEnclosedElements())
        {
            Annotation fieldDataAnnotation = innerElement.getAnnotation(ParameterizedWiredTest.Parameters.class);

            if (null != fieldDataAnnotation)
            {
                if (parametersFieldAnnotation != null)
                {
                    throw new IllegalStateException(String.format("There can be only one %s-annotated field per %s-annotated class but %s has both %s and %s!",
                            ParameterizedWiredTest.Parameters.class.getSimpleName(), ParameterizedWiredTest.class.getSimpleName(), inputClassElement.getSimpleName(), parametersField.getSimpleName(), innerElement.getSimpleName()));
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
                    ParameterizedWiredTest.Parameters.class.getSimpleName(), ParameterizedWiredTest.class.getSimpleName(), inputClassElement.getSimpleName()));
        }

        generateTestClassSource(inputClassElement, parametersFieldAnnotation, testMethods);
    }

    private static Collection<ExecutableElement> collectTestMethods(TypeElement classElement)
    {
        Collection<ExecutableElement> testMethods = collectTestMethods(classElement.getEnclosedElements());

        final TypeMirror superclassTypeMirror = classElement.getSuperclass();

        if (superclassTypeMirror instanceof DeclaredType)
        {
            testMethods.addAll(collectTestMethods((TypeElement) ((DeclaredType) superclassTypeMirror).asElement()));
        }

        return testMethods;
    }

    private static Collection<ExecutableElement> collectTestMethods(Collection<? extends Element> classMembers)
    {
        Collection<ExecutableElement> testMethods = new ArrayList<ExecutableElement>();

        for (Element classMember : classMembers)
        {
            if (classMember instanceof ExecutableElement && classMember.getAnnotation(ParameterizedWiredTest.Test.class) != null)
            {
                testMethods.add((ExecutableElement)classMember);
            }
        }

        return testMethods;
    }

    private void generateTestClassSource(TypeElement inputClassElement, ParameterizedWiredTest.Parameters parametersFieldAnnotation, Collection<ExecutableElement> testMethods) throws IOException
    {
        ClassName generatedClassName = constructClassName(inputClassElement, GENERATED_CLASS_PREFIX);
        JavaFileObject generatedSourceFile = processingEnvironment.getFiler().createSourceFile(generatedClassName.getQualifiedName());
        final Writer writer = generatedSourceFile.openWriter();

        try
        {
            BufferedWriter bw = new BufferedWriter(writer);

            try
            {
                generateTestClassSourceUnsafe(inputClassElement, parametersFieldAnnotation, generatedClassName, bw, testMethods);
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

    private void generateTestClassSourceUnsafe(TypeElement inputClassElement, ParameterizedWiredTest.Parameters parametersFieldAnnotation, ClassName generatedClassName, BufferedWriter bw, Collection<ExecutableElement> testMethods) throws IOException
    {
        final Iterable<ClassName> constructorClassNames = getConstructorClassNames(inputClassElement);
        writeClassHeader(bw, testMethods, generatedClassName.getPackageName(), constructorClassNames);
        writeClassBody(inputClassElement, parametersFieldAnnotation, generatedClassName, bw, constructorClassNames, testMethods);
    }

    private void writeClassBody(TypeElement inputClassElement,
                                ParameterizedWiredTest.Parameters parametersFieldAnnotation,
                                ClassName generatedClassName,
                                BufferedWriter bw,
                                Iterable<ClassName> constructorClassNames,
                                Collection<ExecutableElement> testMethods) throws IOException
    {
        bw.append("@RunWith(AtlassianPluginsTestRunner.class)").append(NEWLINE);
        bw.append("public class ").append(generatedClassName.getSimpleName()).append(" extends ").append(inputClassElement.getSimpleName()).append(NEWLINE);
        bw.append("{").append(NEWLINE);
        bw.append("    public ").append(generatedClassName.getSimpleName()).append("(").append(classNamesToArgList(constructorClassNames)).append(")").append(NEWLINE);
        bw.append("    {").append(NEWLINE);
        bw.append("        super(").append(classNamesToVariableNames(constructorClassNames)).append(");").append(NEWLINE);
        bw.append("    }").append(NEWLINE);

        writeTests(parametersFieldAnnotation, bw, testMethods);

        bw.append("}").append(NEWLINE);
    }

    private static String classNamesToVariableNames(Iterable<ClassName> constructorClassNames)
    {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for (ClassName className : constructorClassNames)
        {
            if (!isFirst)
            {
                sb.append(", ");
            }

            isFirst = false;
            sb.append(classNameToVariableName(className));
        }

        return sb.toString();
    }

    private static String classNamesToArgList(Iterable<ClassName> constructorClassNames)
    {
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;

        for (ClassName className : constructorClassNames)
        {
            if (!isFirst)
            {
                sb.append(", ");
            }

            isFirst = false;
            sb.append(className.getSimpleName()).append(' ').append(classNameToVariableName(className));
        }

        return sb.toString();
    }

    private static String classNameToVariableName(ClassName className)
    {
        return className.getSimpleName().substring(0,1).toLowerCase() + className.getSimpleName().substring(1);
    }

    private static Iterable<ClassName> getConstructorClassNames(TypeElement inputClassElement)
    {
        Collection<ClassName> classNames = null;
        boolean foundConstructor = false;

        for (Element innerElement : inputClassElement.getEnclosedElements())
        {
            if (innerElement.getKind().equals(ElementKind.CONSTRUCTOR))
            {
                if (foundConstructor)
                {
                    throw new IllegalStateException(String.format("%s-annotated classes must have a single constructor but %s has at least two!", ParameterizedWiredTest.class.getSimpleName(), innerElement.getSimpleName()));
                }

                foundConstructor = true;
                ExecutableElement constructor = (ExecutableElement) innerElement;
                classNames = getMethodParameterClassNames(constructor);
            }
        }

        if (!foundConstructor)
        {
            classNames = Collections.emptyList();
        }

        return classNames;
    }

    private static Collection<ClassName> getMethodParameterClassNames(ExecutableElement method)
    {
        Collection<ClassName> classNames;
        classNames = new ArrayList<ClassName>();

        for (VariableElement param : method.getParameters())
        {
            if (param.asType().getKind().isPrimitive())
            {
                final String enumName = param.asType().getKind().toString(); // e.g. "BOOLEAN"
                classNames.add(new ClassName(null, enumName.substring(0,1).toUpperCase() + enumName.substring(1).toLowerCase())); // e.g. "Boolean"
            }
            else
            {
                classNames.add(constructClassName((TypeElement) ((DeclaredType) param.asType()).asElement()));
            }
        }

        return classNames;
    }

    private static Iterable<? extends ClassName> getMethodParameterClassNames(Collection<ExecutableElement> testMethods)
    {
        Set<ClassName> classNames = new HashSet<ClassName>(testMethods.size() * 2); // assume that every test method has at least one input and one expected output

        for (ExecutableElement testMethod : testMethods)
        {
            classNames.addAll(getMethodParameterClassNames(testMethod));
        }

        return classNames;
    }

    private void writeTests(ParameterizedWiredTest.Parameters parametersFieldAnnotation, BufferedWriter bw, Collection<ExecutableElement> testMethods) throws IOException
    {
        for (ExecutableElement testMethod : testMethods)
        {
            final Collection<ClassName> methodFormalParams = getMethodParameterClassNames(testMethod);

            for (int i = 0; i < parametersFieldAnnotation.length(); ++i)
            {
                bw.newLine();
                bw.append("    @Test").append(NEWLINE);
                bw.append("    public void ").append(constructTestMethodName(parametersFieldAnnotation.name(), i, parametersFieldAnnotation.length())).append("() throws Exception").append(NEWLINE);
                bw.append("    {").append(NEWLINE);

                bw.append("        super.").append(testMethod.getSimpleName()).append("(");
                int paramIndex = 0;

                for (ClassName className : methodFormalParams)
                {
                    bw.append(String.format("(%s) super.data[%d][%d]%s", className.getSimpleName(), i, paramIndex, paramIndex < methodFormalParams.size() - 1 ? ", " : ""));
                    ++paramIndex;
                }

                bw.append(");").append(NEWLINE);
                bw.append("    }").append(NEWLINE);
            }
        }
    }

    private void writeClassHeader(BufferedWriter bw, Collection<ExecutableElement> testMethods, CharSequence packageName, Iterable<ClassName> constructorClassNames) throws IOException
    {
        bw.append("package ").append(packageName).append(";").append(NEWLINE);
        bw.newLine();

        for (ClassName constructorArgClassName : constructorClassNames)
        {
            bw.append("import ").append(constructorArgClassName.getQualifiedName()).append(';').append(NEWLINE);
        }

        bw.append("import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;").append(NEWLINE);
        bw.append("import org.junit.Test;").append(NEWLINE);
        bw.append("import org.junit.runner.RunWith;").append(NEWLINE);

        for (ClassName className : getMethodParameterClassNames(testMethods))
        {
            if (null != className.getPackageName() && !className.getPackageName().equals(JAVA_LANG))
            {
                bw.append("import ").append(className.getQualifiedName()).append(';').append(NEWLINE);
            }
        }

        bw.newLine();
    }

    private CharSequence constructTestMethodName(String name, int i, int maxI)
    {
        return String.format("%s_%s", name, StringUtils.leftPad(String.valueOf(i), maxI / 10, '0')); // integer division
    }

    private static class ClassName
    {
        @Nullable private final String packageName;
        private final String className;

        public ClassName(CharSequence packageName, CharSequence className)
        {
            this.packageName = null == packageName ? null : packageName.toString();
            this.className = className.toString();
        }

        public String getQualifiedName()
        {
            return null == packageName ? className : String.format("%s.%s", packageName, className);
        }

        public String getPackageName()
        {
            return packageName;
        }

        public String getSimpleName()
        {
            return className;
        }

        @Override
        public boolean equals(Object rhsObject)
        {
            if (this == rhsObject)
            {
                return true;
            }

            if (!(rhsObject instanceof ClassName))
            {
                return false;
            }

            ClassName rhs = (ClassName) rhsObject;
            return className.equals(rhs.className) && (null == packageName ? null == rhs.packageName : packageName.equals(rhs.packageName));

        }

        @Override
        public int hashCode()
        {
            return 31 * (null == packageName ? 0 : packageName.hashCode()) + 3 * className.hashCode();
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
