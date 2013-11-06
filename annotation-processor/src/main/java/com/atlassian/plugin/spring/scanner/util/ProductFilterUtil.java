package com.atlassian.plugin.spring.scanner.util;

import com.atlassian.plugin.spring.scanner.ProductFilter;

import com.google.common.annotations.VisibleForTesting;

/**
 * Utility to figure out what the currently running product is and give us it's name/filter in various ways
 */
public class ProductFilterUtil
{
    private static ProductFilterUtil INSTANCE;

    @VisibleForTesting
    static final String CLASS_ON_JIRA_CLASSPATH = "com.atlassian.jira.plugin.issuetabpanel.IssueTabPanelModuleDescriptor";
    @VisibleForTesting
    static final String CLASS_ON_CONFLUENCE_CLASSPATH = "com.atlassian.confluence.plugin.descriptor.MacroModuleDescriptor";

    private ProductFilter filterForProduct;

    /**
     * Yeah, it's somewhat sucky using Class.forName, but it's currently the most stable way to figure out which product we're in
     * since we can't have anything injected into us at this point in spring's lifecycle.
     * 
     * We could try to use system properties, but those are even more volatile than these ancient classes.
     */
    private  ProductFilterUtil()
    {
        try
        {
            Class.forName(CLASS_ON_JIRA_CLASSPATH);
            filterForProduct = ProductFilter.JIRA;
        }
        catch (ClassNotFoundException e)
        {
        }

        try
        {
            Class.forName(CLASS_ON_CONFLUENCE_CLASSPATH);
            filterForProduct = ProductFilter.CONFLUENCE;
        }
        catch (ClassNotFoundException e)
        {
        }
    }

    /**
     * Returns the ProductFilter instance that represents the currently running product.
     * 
     * @return
     */
    public static ProductFilter getFilterForCurrentProduct()
    {
        return getInstance().getFilterForProduct();
    }

    /**
     * returns the lower-case name of the currently running product
     * 
     * @return
     */
    public static String getLowerCurrentProductName()
    {
        ProductFilter filter = getFilterForCurrentProduct();

        if (null == filter)
        {
            return "";
        }
        else
        {
            return filter.name().toLowerCase();
        }
    }

    public ProductFilter getFilterForProduct()
    {
        return filterForProduct;
    }

    private static ProductFilterUtil getInstance()
    {
        if(null == INSTANCE)
        {
            INSTANCE = new ProductFilterUtil();
        }
        
        return INSTANCE;
    }
    
}
