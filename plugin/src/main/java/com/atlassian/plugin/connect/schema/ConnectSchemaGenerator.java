package com.atlassian.plugin.connect.schema;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.atlassian.json.schema.DefaultJsonSchemaGenerator;
import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.model.JsonSchema;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.atlassian.plugin.connect.plugin.capabilities.annotation.CapabilityModuleProvider;
import com.atlassian.plugin.spring.scanner.ProductFilter;

public class ConnectSchemaGenerator extends DefaultJsonSchemaGenerator
{
    private final ProductFilter product;
    
    public ConnectSchemaGenerator(boolean lowercaseEnums, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, ProductFilter product)
    {
        super(lowercaseEnums, interfaceList, schemaDocs);
        this.product = product;
    }

    @Override
    protected JsonSchema generateSchemaForField(Class<?> owner, Field field, Class<?>[] ifaces)
    {
        if(field.isAnnotationPresent(CapabilityModuleProvider.class))
        {
            CapabilityModuleProvider anno = field.getAnnotation(CapabilityModuleProvider.class);
            List<ProductFilter> filters = Arrays.asList(anno.products());
            if(!filters.contains(product) && !filters.contains(ProductFilter.ALL))
            {
                return null;
            }
        }
        
        return super.generateSchemaForField(owner,field,ifaces);
    }
}
