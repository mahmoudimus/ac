package com.atlassian.plugin.connect.test.modules;

import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.beans.ModuleList;
import org.junit.Test;

import java.lang.reflect.Field;

/**
 * This class is here simply to make sure we don't typo a classname when we use @ConnectModule annotations
 */
public class ModuleProviderAnnotationCompileTimeTest
{
    /**
     * I realize that asserting true and just letting exceptions happen in a loop is not how we "normally" write tests
     * However, doing it this way ensures that if we ever add any new modules to the ModuleList class, they will automatically
     * be picked up here
     */
    @Test
    public void verifyClassesLoad() throws Exception
    {
        for(Field field : ModuleList.class.getDeclaredFields())
        {
            if(field.isAnnotationPresent(ConnectModule.class))
            {
                ConnectModule anno = field.getAnnotation(ConnectModule.class);
                Class.forName(anno.value());
            }
        }
        
        assert(true);
    }
}
