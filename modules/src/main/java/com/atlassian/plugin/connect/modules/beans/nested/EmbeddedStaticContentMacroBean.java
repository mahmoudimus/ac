package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.SchemaDefinition;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.EmbeddedStaticContentMacroBeanBuilder;

@SchemaDefinition("embeddedStaticContentMacroBean")
public class EmbeddedStaticContentMacroBean extends BaseModuleBean
{
    @Required
    private String url;

    public EmbeddedStaticContentMacroBean(EmbeddedStaticContentMacroBeanBuilder builder)
    {
        super(builder);
    }

    public String getUrl()
    {
        return url;
    }

    public static EmbeddedStaticContentMacroBeanBuilder newEmbeddedStaticContentMacroModuleBean()
    {
        return new EmbeddedStaticContentMacroBeanBuilder();
    }
}
