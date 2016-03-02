package com.atlassian.plugin.connect.modules.beans.nested.contenttype;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.SchemaDefinition;

@SchemaDefinition("icons")
public class IconsBean {
    @CommonSchemaAttributes(defaultValue = "")
    private String item;

    @CommonSchemaAttributes(defaultValue = "")
    private String container;

    @CommonSchemaAttributes(defaultValue = "")
    private String create;

    public IconsBean() {
    }

    public IconsBean(String item, String container, String create) {
        this.item = item;
        this.container = container;
        this.create = create;
    }

    public String getItem() {
        return item;
    }

    public String getContainer() {
        return container;
    }

    public String getCreate() {
        return create;
    }
}
