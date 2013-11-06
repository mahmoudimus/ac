package com.atlassian.plugin.spring.scanner;

/**
 * An enum representing the products that we can filter beans by.
 * This enum is used not only within this library, but may also be used by
 * any other library/plugin that needs a list of products to filter by.
 * 
 * Example: Atlassian Connect uses this enum to filter out product-specific
 * connect modules. Moral of the story: Don't remove this.
 */
public enum ProductFilter
{
    ALL,JIRA,CONFLUENCE,BAMBOO,STASH,CROWD,FECRU;
    
    public static boolean hasProduct(String productName)
    {
        try
        {
            ProductFilter filter = valueOf(productName);
            return (null != filter);
        }
        catch (IllegalArgumentException e)
        {
            return false;
        }
    }
}
