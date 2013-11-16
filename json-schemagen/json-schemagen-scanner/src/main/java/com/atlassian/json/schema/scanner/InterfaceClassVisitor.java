package com.atlassian.json.schema.scanner;

import java.io.InputStream;
import java.util.*;

import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.commons.EmptyVisitor;

public class InterfaceClassVisitor extends EmptyVisitor
{
    private final Map<String, Set<String>> ifaceToImpls;


    public InterfaceClassVisitor(Map<String, Set<String>> ifaceToImpls)
    {
        this.ifaceToImpls = ifaceToImpls;
    }

    @Override
    public void visit(final int version, final int access, final String name, final String signature, final String superName, final String[] interfaces)
    {
        Set<String> ifaces = getInterfaces(name, interfaces, superName);

        for (String iface : ifaces)
        {
            if (!ifaceToImpls.containsKey(iface))
            {
                Set<String> impls = new HashSet<String>();
                impls.add(normalize(name));
                ifaceToImpls.put(iface, impls);
            }
            else
            {
                ifaceToImpls.get(iface).add(normalize(name));
            }
        }

    }


    private Set<String> getInterfaces(String name, String[] interfaces, String superName)
    {
        Set<String> ifaces = new HashSet<String>();
        ifaces.addAll(normalize(interfaces));

        return addSuperInterfaces(superName, ifaces);
    }

    private Set<String> addSuperInterfaces(String superName, Set<String> ifaces)
    {
        if (normalize(superName).equals("java.lang.Object"))
        {
            return ifaces;
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = superName.replace('.', '/');

        InputStream is = null;
        try
        {
            is = classLoader.getResourceAsStream(path + ".class");
            if (null != is)
            {

                ClassReader classReader = new ClassReader(is);
                ifaces.addAll(normalize(classReader.getInterfaces()));

                return addSuperInterfaces(classReader.getSuperName(), ifaces);
            }
        }
        catch (Exception e)
        {
            //don't care
        }
        finally
        {
            IOUtils.closeQuietly(is);
        }

        return ifaces;
    }

    protected String normalize(String name)
    {
        if (name == null)
        {
            return null;
        }

        if (name.startsWith("L") && name.endsWith(";"))
        {
            name = name.substring(1, name.length() - 1);
        }

        if (name.endsWith(".class"))
        {
            name = name.substring(0, name.length() - ".class".length());
        }

        return name.replace('/', '.');
    }

    protected List<String> normalize(String[] names)
    {
        return Lists.transform(Arrays.asList(names), new Function<String, String>()
        {
            @Override
            public String apply(@Nullable String input)
            {
                return normalize(input);
            }
        });
    }
}
