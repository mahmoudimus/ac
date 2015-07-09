package com.atlassian.plugin.connect.confluence.capabilities.descriptor;

import com.atlassian.confluence.macro.Macro;
import com.atlassian.plugin.connect.modules.beans.nested.MacroBodyType;
import com.atlassian.plugin.connect.modules.beans.nested.MacroOutputType;

/**
 * Beans can't reference any product-specific classes, hence the mapping here
 */
public class MacroEnumMapper
{
    public static Macro.BodyType map(MacroBodyType bodyType)
    {
        switch (bodyType)
        {
            case RICH_TEXT:
                return Macro.BodyType.RICH_TEXT;
            case PLAIN_TEXT:
                return Macro.BodyType.PLAIN_TEXT;
            case NONE:
                return Macro.BodyType.NONE;
        }
        throw new IllegalStateException("Unexpected value: " + bodyType);
    }

    public static Macro.OutputType map(MacroOutputType outputType)
    {
        switch (outputType)
        {
            case BLOCK:
                return Macro.OutputType.BLOCK;
            case INLINE:
                return Macro.OutputType.INLINE;
        }
        throw new IllegalStateException("Unexpected value: " + outputType);
    }
}
