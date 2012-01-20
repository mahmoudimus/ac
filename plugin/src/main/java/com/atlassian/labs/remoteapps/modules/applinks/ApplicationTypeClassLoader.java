package com.atlassian.labs.remoteapps.modules.applinks;

import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.spi.application.NonAppLinksApplicationType;
import com.atlassian.util.concurrent.CopyOnWriteMap;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.collect.Maps.newHashMap;
import static org.objectweb.asm.Opcodes.*;

/**
* Generates and manages application type-related class instances
*/
@Component
public class ApplicationTypeClassLoader extends ClassLoader
{
    private final Pattern applicationTypePattern = Pattern.compile("generatedApplicationType/(.*)/_type");
    private final Map<String,Class> classes = newHashMap();

    private String appKeyToClassName(String key)
    {
        return "generatedApplicationType/" + key + "/_type";
    }

    private String typeIdToManifestProducerClassName(String typeId)
    {
        return "generatedManifestProducer/" + typeId + "/_manifest";
    }

    public ApplicationTypeClassLoader()
    {
        super(ApplicationTypeModuleGenerator.class.getClassLoader());
    }

    public Class<? extends RemoteAppApplicationType> getApplicationType(String key)
    {
        try
        {
            return (Class<? extends RemoteAppApplicationType>) loadClass(appKeyToClassName(key), true);
        } catch (ClassNotFoundException e)
        {
            // todo: handle better
            throw new RuntimeException(e);
        }
    }

    public Class<? extends RemoteManifestProducer> getManifestProducer(NonAppLinksApplicationType applicationType)
    {
        return generateManifestProducer(applicationType.getId().get(), applicationType.getI18nKey());
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException
    {
        Matcher appType = applicationTypePattern.matcher(name);
        if (appType.matches())
        {
            return generateApplicationType(appType.group(1));
        }

        throw new ClassNotFoundException(name);
    }

    private Class<? extends RemoteAppApplicationType> generateApplicationType(String key)
    {
        String genClassName = appKeyToClassName(key);
        if (classes.containsKey(genClassName))
        {
            return classes.get(genClassName);
        }
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;
        cw.visit(V1_6, ACC_PUBLIC + ACC_SUPER, genClassName, null,
                "com/atlassian/labs/remoteapps/modules/applinks/RemoteAppApplicationType", null);

        mv = cw.visitMethod(ACC_PUBLIC, "<init>",
                "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/String;Ljava/net/URI;Lcom/atlassian/applinks/spi/link/ApplicationLinkDetails;)V",
                null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitVarInsn(ALOAD, 2);
        mv.visitVarInsn(ALOAD, 3);
        mv.visitVarInsn(ALOAD, 4);
        mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/labs/remoteapps/modules/applinks/RemoteAppApplicationType",
                "<init>",
                "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/String;Ljava/net/URI;Lcom/atlassian/applinks/spi/link/ApplicationLinkDetails;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 5);
        mv.visitEnd();

        cw.visitEnd();
        byte[] b = cw.toByteArray();
        Class<? extends RemoteAppApplicationType> clazz = (Class<? extends RemoteAppApplicationType>) defineClass(
                genClassName.replace("/", "."), b, 0,
                b.length);
        classes.put(genClassName, clazz);
        return clazz;
    }

    private Class<? extends RemoteManifestProducer> generateManifestProducer(String typeId, String name)
    {
        String genClassName = typeIdToManifestProducerClassName(typeId);
        if (classes.containsKey(genClassName))
        {
            return classes.get(genClassName);
        }
        ClassWriter cw = new ClassWriter(0);
        MethodVisitor mv;

        cw.visit(V1_5, ACC_PUBLIC + ACC_SUPER, genClassName, null,
                "com/atlassian/labs/remoteapps/modules/applinks/RemoteManifestProducer", null);

        // constructor that encodes the parameters in the constructor super call
        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitTypeInsn(NEW, "com/atlassian/applinks/spi/application/TypeId");
        mv.visitInsn(DUP);
        mv.visitLdcInsn(typeId);
        mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/applinks/spi/application/TypeId", "<init>",
                "(Ljava/lang/String;)V");
        mv.visitLdcInsn(name);
        mv.visitMethodInsn(INVOKESPECIAL, "com/atlassian/labs/remoteapps/modules/applinks/RemoteManifestProducer",
                "<init>", "(Lcom/atlassian/applinks/spi/application/TypeId;Ljava/lang/String;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(4, 1);
        mv.visitEnd();

        cw.visitEnd();
        byte[] b = cw.toByteArray();
        Class<? extends RemoteManifestProducer> clazz = (Class<? extends RemoteManifestProducer>) defineClass(
                genClassName.replace("/", "."), b, 0,
                b.length);
        classes.put(genClassName, clazz);
        return clazz;
    }
}
