package com.atlassian.plugin.connect.schema;

import com.atlassian.json.schema.DefaultJsonSchemaGenerator;
import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.model.JsonSchema;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.atlassian.plugin.connect.plugin.capabilities.annotation.ConnectModule;
import com.atlassian.plugin.spring.scanner.ProductFilter;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class ConnectSchemaGenerator extends DefaultJsonSchemaGenerator
{
    private final ProductFilter product;
    
    public ConnectSchemaGenerator(boolean lowercaseEnums, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, ProductFilter product)
    {
        super(lowercaseEnums, interfaceList, schemaDocs);
        this.product = product;
    }

    @Override
    protected JsonSchema generateSchemaForField(Class<?> owner, Field field, Class<?>[] ifaces, String defaultArrayTitle)
    {
        if(field.isAnnotationPresent(ConnectModule.class))
        {
            ConnectModule anno = field.getAnnotation(ConnectModule.class);
            List<ProductFilter> filters = Arrays.asList(anno.products());
            if(!filters.contains(product) && !filters.contains(ProductFilter.ALL))
            {
                return null;
            }
        }
        
        return super.generateSchemaForField(owner,field,ifaces, defaultArrayTitle);
    }
}
