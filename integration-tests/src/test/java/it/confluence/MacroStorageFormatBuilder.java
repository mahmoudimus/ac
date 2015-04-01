package it.confluence;

import org.apache.commons.lang.StringEscapeUtils;

import java.util.HashMap;
import java.util.Map;

public class MacroStorageFormatBuilder
{
    private final String macroKey;

    private String richTextBody;

    private Map<String, String> parameters = new HashMap<>();

    public MacroStorageFormatBuilder(String macroKey)
    {
        this.macroKey = macroKey;
    }

    public MacroStorageFormatBuilder richTextBody(String body)
    {
        this.richTextBody = body;
        return this;
    }

    public String build()
    {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("<ac:structured-macro ac:name=\"%s\">", macroKey));
        for (Map.Entry<String, String> parameter : parameters.entrySet())
        {
            builder.append(String.format("<ac:parameter ac:name=\"%s\">", parameter.getKey()));
            builder.append(parameter.getValue());
            builder.append("</ac:parameter>");
        }
        if (richTextBody != null)
        {
            builder.append("<ac:rich-text-body>");
            builder.append(StringEscapeUtils.escapeXml(richTextBody));
            builder.append("</ac:rich-text-body>");
        }
        builder.append("</ac:structured-macro>");
        return builder.toString();
    }

    public MacroStorageFormatBuilder parameter(String name, String value)
    {
        parameters.put(name, value);
        return this;
    }
}
