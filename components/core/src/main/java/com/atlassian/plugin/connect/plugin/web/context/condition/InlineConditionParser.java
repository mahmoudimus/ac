package com.atlassian.plugin.connect.plugin.web.context.condition;

import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.Nonnull;

import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.google.common.base.Strings.nullToEmpty;

@Component
public class InlineConditionParser
{
    private static final Logger log = LoggerFactory.getLogger(InlineConditionParser.class);

    private static final Pattern CONDITION_PARSER = Pattern.compile("^condition\\.([a-zA-Z0-9\\-_]+)\\s*(?:\\((.*)\\))?$");
    private static final Splitter.MapSplitter PARAMETERS_PARSER = Splitter.on(",").trimResults().omitEmptyStrings().withKeyValueSeparator(Splitter.on("=").trimResults());

    public Optional<InlineCondition> parse(String variable)
    {

        Matcher matcher = CONDITION_PARSER.matcher(variable.trim());
        if (matcher.find())
        {
            String conditionName = matcher.group(1);
            String parameters = matcher.group(2);
            return parseParameters(nullToEmpty(parameters)).map(params -> new InlineCondition(conditionName, params));
        }
        else
        {
            return Optional.empty();
        }
    }

    private Optional<Map<String, String>> parseParameters(@Nonnull final String parametersString)
    {
        try
        {
            return Optional.of(PARAMETERS_PARSER.split(parametersString));
        }
        catch (IllegalArgumentException iae) {
            log.info("invalid syntax for parameters list: '" + parametersString + "'");
            return Optional.empty();
        }
    }
}
