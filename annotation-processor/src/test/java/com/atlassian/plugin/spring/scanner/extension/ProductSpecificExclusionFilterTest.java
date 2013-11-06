package com.atlassian.plugin.spring.scanner.extension;

import com.atlassian.plugin.spring.scanner.extension.ProductSpecificExclusionFilter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.fail;

@RunWith(MockitoJUnitRunner.class)
public class ProductSpecificExclusionFilterTest
{

    @Test
    public void testJiraClassIsPresent()
    {
        assertClassIsPresentOnClasspath(ProductSpecificExclusionFilter.CLASS_ON_JIRA_CLASSPATH, "JIRA");
    }

    @Test
    public void testConfluenceClassIsPresent()
    {
        assertClassIsPresentOnClasspath(ProductSpecificExclusionFilter.CLASS_ON_CONFLUENCE_CLASSPATH, "Confluence");
    }

    private void assertClassIsPresentOnClasspath(String clazz, String product)
    {
        try {
            Class.forName(clazz);
        } catch (ClassNotFoundException e) {
            fail(String.format(
                    "Class %s not found on classpath, is it no longer exported from %s? If so, %s will need to be " +
                            "updated to use an exported class from %s.",
                    clazz, product, ProductSpecificExclusionFilter.class.getName(), product));
        }
    }

}
