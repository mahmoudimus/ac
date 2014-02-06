package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.CommonSchemaAttributes;
import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;
import com.atlassian.plugin.connect.modules.beans.builder.MacroParameterBeanBuilder;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Describes a parameter input field for a macro.
 * <p/>
 *#### Example
 *
 * @exampleJson {@see com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#DYNAMIC_MACRO_EXAMPLE}
 * @schemaTitle Macro Input Parameter
 * @since 1.0
 */
public class MacroParameterBean extends BaseModuleBean
{
    /**
     * A unique identifier for the parameter. It has to be all lowercase, and must not contain any spaces. This identifier will
     * be used in the query parameters of the add-on URL.
     */
    @Required
    @StringSchemaAttributes(pattern = "[-_a-z0-9\\.]+")
    private String identifier;

    /**
     * The human readable name of the parameter which will be displayed in the UI.
     */
    @Required
    private I18nProperty name;

    /**
     * The description of the parameter
     */
    private I18nProperty description;

    /**
     * The type of parameter.
     *
     * Currently the following parameter types are supported in the macro browser's UI:
     *
     * * `attachment`: displays an autocomplete field for search on attachment filenames.
     * * `boolean`: displays a check box.
     * * `confluence-content`: displays an autocomplete field for search on page and blog titles.
     * * `enum`: displays a select field.
     * * `spacekey`: displays an autocomplete field for search on space names.
     * * `string`: displays an input field (this is the default if unknown type).
     * * `username`: displays an autocomplete field for search on username and full name.
     */
    @Required
    private String type;

    /**
     * Whether it is a required parameter.
     */
    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean required;

    /**
     * Whether it takes multiple values.
     */
    @CommonSchemaAttributes(defaultValue = "false")
    private Boolean multiple;

    /**
     * The default value for the parameter.
     */
    private String defaultValue;

    /**
     * Describes the ``enum`` values - only applicable for enum typed parameters.
     */
    private List<String> values;

    /**
     * Aliases for the macro parameter.
     */
    private List<String> aliases;

    public MacroParameterBean()
    {
        init();
    }

    public MacroParameterBean(MacroParameterBeanBuilder builder)
    {
        super(builder);
        init();
    }

    private void init()
    {
        if (null == identifier)
        {
            identifier = "";
        }
        if (null == name)
        {
            name = I18nProperty.empty();
        }
        if (null == type)
        {
            type = "string";
        }
        if (null == required)
        {
            required = false;
        }
        if (null == multiple)
        {
            multiple = false;
        }
        if (null == defaultValue)
        {
            defaultValue = "";
        }
        if (null == values)
        {
            values = ImmutableList.of();
        }
        if (null == aliases)
        {
            aliases = ImmutableList.of();
        }
    }

    public String getIdentifier()
    {
        return identifier;
    }

    public I18nProperty getName()
    {
        return name;
    }

    public I18nProperty getDescription()
    {
        return description;
    }

    public String getType()
    {
        return type;
    }

    public Boolean isRequired()
    {
        return required;
    }

    public Boolean isMultiple()
    {
        return multiple;
    }

    public String getDefaultValue()
    {
        return defaultValue;
    }

    public boolean hasDefaultValue()
    {
        return !Strings.isNullOrEmpty(defaultValue);
    }

    public List<String> getValues()
    {
        return values;
    }

    public List<String> getAliases()
    {
        return aliases;
    }

    public static MacroParameterBeanBuilder newMacroParameterBean()
    {
        return new MacroParameterBeanBuilder();
    }

    public static MacroParameterBeanBuilder newMacroParameterBean(MacroParameterBean defaultBean)
    {
        return new MacroParameterBeanBuilder(defaultBean);
    }
}
