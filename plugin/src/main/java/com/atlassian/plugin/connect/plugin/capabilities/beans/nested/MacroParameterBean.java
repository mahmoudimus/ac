package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.plugin.connect.plugin.capabilities.beans.BaseModuleBean;
import com.atlassian.plugin.connect.plugin.capabilities.beans.builder.MacroParameterBeanBuilder;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * Describes a parameter input field for a macro.
 *
 * Json Example:
 * @exampleJson {@see ConnectJsonExamples#DYNAMIC_MACRO_EXAMPLE}
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
    private String identifier;

    /**
     * The human readable name of the parameter which will be displayed in the UI.
     */
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
     * * __attachment__: displays an autocomplete field for search on attachment filenames.
     * * __boolean__: displays a check box.
     * * __confluence-content__: displays an autocomplete field for search on page and blog titles.
     * * __enum__: displays a select field.
     * * __spacekey__: displays an autocomplete field for search on space names.
     * * __string__: displays an input field (this is the default if unknown type).
     * * __username__: displays an autocomplete field for search on username and full name.
     *
     */
    @Required
    private String type;

    /**
     * Whether it is a required parameter, defaults to 'false'.
     */
    private Boolean required;

    /**
     * Whether it takes multiple values, defaults to 'false'.
     */
    private Boolean multiple;

    /**
     * The default value for the parameter.
     */
    private String defaultValue;

    /**
     * Describes the 'enum' values - only applicable for enum typed parameters.
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
        if (null == description)
        {
            description = I18nProperty.empty();
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
