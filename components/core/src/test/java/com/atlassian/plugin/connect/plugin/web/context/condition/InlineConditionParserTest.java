package com.atlassian.plugin.connect.plugin.web.context.condition;

import java.util.Map;
import java.util.Optional;

import com.google.common.collect.Maps;
import org.junit.Test;

import static java.util.Optional.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

public class InlineConditionParserTest
{
    private final InlineConditionParser parser = new InlineConditionParser();

    @Test
    public void variablesWithoutConditionPrefixAreNotParsed() {
        assertThat(parser.parse("has_project_permission(permission=browse)"), equalTo(empty()));
    }
    @Test
    public void prefixIsCaseSensistiveAndEndsWithADot() {
        assertThat(parser.parse("conditionhas_project_permission(permission=browse)"), equalTo(empty()));
        assertThat(parser.parse("ccondition.has_project_permission(permission=browse)"), equalTo(empty()));
        assertThat(parser.parse("Condition.has_project_permission(permission=browse)"), equalTo(empty()));
        assertThat(parser.parse("CONDITION.has_project_permission(permission=browse)"), equalTo(empty()));
    }

    @Test
    public void spaceIsNotAllowedAfterThePrefix() {
        assertThat(parser.parse("condition. has_project_permission(permission=browse)"), equalTo(empty()));
    }

    @Test
    public void parenthesisAreNotRequired() {
        assertThat(parser.parse("condition.is_admin"), equalTo(condition("is_admin").build()));
    }

    @Test
    public void parenthesisWithoutAnyParametersAreAllowed() {
        assertThat(parser.parse("condition.is_admin()"), equalTo(condition("is_admin").build()));
    }

    @Test
    public void extraSpacesAreIgnoredInParametersList() {
        assertThat(parser.parse("condition.has_project_permission  (  permission  =   browse  )  "),
                equalTo(condition("has_project_permission").withParam("permission", "browse").build()));
    }

    @Test
    public void moreThanOneParameterCanBeSupplied() {
        assertThat(parser.parse("condition.has_project_permission( permission=browse ,  otherParam = val)"),
                equalTo(condition("has_project_permission").withParam("permission", "browse").withParam("otherParam", "val").build()));
    }

    @Test
    public void allCharactersAllowedInCustomPermissionKeysAreSupported() {
        assertThat(parser.parse("condition.myAddOnKey__az-AZ-023-Az42"), equalTo(condition("myAddOnKey__az-AZ-023-Az42").build()));
    }

    @Test
    public void invalidCharactersDoNotBreakTheParser() {
        assertThat(parser.parse("condition.$%^*&*.,;"), equalTo(empty()));
    }

    @Test
    public void quotingParametersDoesNotWorkAndItsAFeature() {
        assertThat(parser.parse("condition.has_project_permission( permission=\"browse, heh\")"), equalTo(empty()));
    }


    private static InlineConditionBuilder condition(String name) {
        return new InlineConditionBuilder(name);
    }

    private  static final class InlineConditionBuilder
    {
        private final String conditionName;
        private final Map<String, String> params = Maps.newHashMap();

        private InlineConditionBuilder(String conditionName) {
            this.conditionName = conditionName;
        }

        public InlineConditionBuilder withParam(String key, String value)
        {
            this.params.put(key, value);
            return this;
        }

        public Optional<InlineCondition> build()
        {
            return Optional.of(new InlineCondition(conditionName, params));
        }
    }
}
