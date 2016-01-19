package com.atlassian.plugin.connect.plugin.web.context.condition;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.google.common.base.Splitter;
import org.springframework.stereotype.Component;

import static com.google.common.base.Strings.nullToEmpty;

@Component
public class InlineConditionParser
{
    private final Pattern regexp = Pattern.compile("condition\\.([\\w_]+)(?:\\((.*)\\))?");

    public Optional<InlineCondition> parse(String variable)
    {

        Matcher matcher = regexp.matcher(variable);
        if (matcher.find())
        {
            String conditionName = matcher.group(1);
            String parameters = matcher.group(2);
            return Optional.of(new InlineCondition(conditionName, parseParameters(nullToEmpty(parameters))));
        }
        else
        {
            return Optional.empty();
        }
    }

    private Map<String, String> parseParameters(@Nonnull final String parametersString)
    {
        return Splitter.on(",").trimResults().omitEmptyStrings().withKeyValueSeparator(Splitter.on("=").trimResults()).split(parametersString);
    }
}
