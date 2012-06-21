package com.atlassian.labs.remoteapps.util.beanfactory;

import com.google.common.collect.ImmutableMap;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.support.GenericApplicationContext;

import static org.junit.Assert.assertEquals;

public class TestSpecializedBeanFactory
{
    @Test
    public void testInject()
    {
        GenericApplicationContext parent = new GenericApplicationContext();
        parent.getBeanFactory().registerSingleton("foo", new Integer(10));

        SpecializedBeanFactory factory = new SpecializedBeanFactory(parent);
        Bean bean = factory.construct(Bean.class, "bob");
        assertEquals("bob", bean.name);
        assertEquals(10, (int)bean.dep);
    }

    @Test
    @Ignore("Doesn't work as qualifier annotations not found")
    public void testInjectTwoStrings()
    {
        GenericApplicationContext parent = new GenericApplicationContext();
        SpecializedBeanFactory factory = new SpecializedBeanFactory(parent);
        TwoStringsBean bean = factory.construct(TwoStringsBean.class, ImmutableMap.<String,Object>of(
                "name", "bob",
                "location", "heaven"));
        assertEquals("bob", bean.name);
        assertEquals("heaven", bean.location);
    }

    public static class Bean
    {
        final String name;
        final Integer dep;

        public Bean(String name, Integer dep)
        {
            this.name = name;
            this.dep = dep;
        }
    }

    public static class TwoStringsBean
    {
        final String name;
        final String location;

        public TwoStringsBean(@Qualifier("name") String name,
                @Qualifier("location") String location)
        {
            this.name = name;
            this.location = location;
        }
    }
}
