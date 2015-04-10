package com.atlassian.plugin.connect.modules.util;

/**
 * NOTE: This is a copy of the ProductFilter class in atlassian-spring-scanner
 * We have copied it here so we can use product filtering without having to include all of spring dependencies.
 * This is to support non-plugin projects consuming this library without a ton of dependencies
 * <br><br>
 * An enum representing the products that we can filter beans by.
 * This enum is used not only within this library, but may also be used by
 * any other library/plugin that needs a list of products to filter by.
 * <br><br>
 * Example: Atlassian Connect uses this enum to filter out product-specific
 * connect modules. Moral of the story: Don't remove this.
 */
public enum ProductFilter
{
    ALL, JIRA, CONFLUENCE, BAMBOO, STASH, CROWD, FECRU;

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
