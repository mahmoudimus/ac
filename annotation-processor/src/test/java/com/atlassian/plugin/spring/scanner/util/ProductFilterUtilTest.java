package com.atlassian.plugin.spring.scanner.util;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.fail;

/**
 * Just a smoke test to catch if the classnames we use to determine which product we're in ever change
 */
@RunWith(MockitoJUnitRunner.class)
public class ProductFilterUtilTest
{

    @Test
    public void testJiraClassIsPresent()
    {
        assertClassIsPresentOnClasspath(ProductFilterUtil.CLASS_ON_JIRA_CLASSPATH, "JIRA");
    }

    @Test
    public void testConfluenceClassIsPresent()
    {
        assertClassIsPresentOnClasspath(ProductFilterUtil.CLASS_ON_CONFLUENCE_CLASSPATH, "Confluence");
    }

    private void assertClassIsPresentOnClasspath(String clazz, String product)
    {
        try {
            Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            fail(String.format(
                    "Class %s not found on classpath, is it no longer exported from %s? If so, %s will need to be " +
                            "updated to use an exported class from %s.",
                    clazz, product, ProductFilterUtil.class.getName(), product));
        }
    }

}
