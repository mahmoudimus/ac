package com.atlassian.labs.remoteapps;

import com.atlassian.labs.remoteapps.modules.RemoteModule;
import com.atlassian.labs.remoteapps.modules.RemoteModuleGenerator;
import com.atlassian.labs.remoteapps.modules.applinks.ApplicationTypeModuleGenerator;
import org.dom4j.Element;
import org.netbeans.lib.cvsclient.commandLine.command.log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import sun.security.pkcs11.Secmod;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import static com.google.common.collect.Maps.newHashMap;
import static com.google.common.collect.Sets.newHashSet;
import static org.apache.commons.lang.Validate.notNull;

/**
 *
 */
@Component
public class ModuleGeneratorManager
{
    private final static Logger log = LoggerFactory.getLogger(ModuleGeneratorManager.class);
    private final Map<String,RemoteModuleGenerator> childGenerators;
    private final Set<RemoteModuleGenerator> allGenerators;
    private final ApplicationTypeModuleGenerator applicationTypeModuleGenerator;

    @Autowired
    public ModuleGeneratorManager(ApplicationContext applicationContext)
    {
        Map<String,RemoteModuleGenerator> map = newHashMap();
        ApplicationTypeModuleGenerator appTypeGen = null;
        Set<RemoteModuleGenerator> set = newHashSet();
        for (RemoteModuleGenerator generator : (Collection<RemoteModuleGenerator>) applicationContext.getBeansOfType(RemoteModuleGenerator.class).values())
        {
            set.add(generator);
            if (generator instanceof ApplicationTypeModuleGenerator)
            {
                appTypeGen = (ApplicationTypeModuleGenerator) generator;
            }
            else
            {
                map.put(generator.getType(), generator);
            }
        }

        childGenerators = Collections.unmodifiableMap(map);
        notNull(appTypeGen);
        applicationTypeModuleGenerator = appTypeGen;
        allGenerators = Collections.unmodifiableSet(set);
    }

    public void processDescriptor(Element root, ModuleHandler handler)
    {
        for (Element e : ((Collection<Element>)root.elements()))
        {
            String type = e.getName();
            if (childGenerators.containsKey(type))
            {
                RemoteModuleGenerator generator = childGenerators.get(type);
                handler.handle(e, generator);
            }
            else
            {
                log.warn("Unknown module: " + type);
            }
        }
    }

    public Map<String, RemoteModuleGenerator> getChildGenerators()
    {
        return childGenerators;
    }

    public Set<RemoteModuleGenerator> getAllGenerators()
    {
        return allGenerators;
    }

    public ApplicationTypeModuleGenerator getApplicationTypeModuleGenerator()
    {
        return applicationTypeModuleGenerator;
    }

    public static interface ModuleHandler
    {
        public void handle(Element element, RemoteModuleGenerator generator);
    }
}
