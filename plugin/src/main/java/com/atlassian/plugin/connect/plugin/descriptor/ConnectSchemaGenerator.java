package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.json.schema.DefaultJsonSchemaGenerator;
import com.atlassian.json.schema.EnumCase;
import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.model.JsonSchema;
import com.atlassian.json.schema.scanner.model.InterfaceList;
import com.atlassian.plugin.connect.modules.annotation.ConnectModule;
import com.atlassian.plugin.connect.modules.util.ProductFilter;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class ConnectSchemaGenerator extends DefaultJsonSchemaGenerator
{
    private final ProductFilter product;
    
    public ConnectSchemaGenerator(EnumCase enumCase, InterfaceList interfaceList, JsonSchemaDocs schemaDocs, String ignoreFilter, ProductFilter product)
    {
        super(enumCase, interfaceList, schemaDocs, ignoreFilter);
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

    @Override
    protected String getFieldName(Field field)
    {
        String name = field.getName();
        
        if(field.isAnnotationPresent(SerializedName.class))
        {
            name = field.getAnnotation(SerializedName.class).value();
        }
        
        return name;
    }
}
