package it.matcher;

import cc.plural.jsonij.Value;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;

import java.util.List;

/**
 * Matchers for making assertions against cc.plural.jsonij.Value objects.
 */
public class ValueMatchers
{
    private static abstract class BaseValueMatcher extends TypeSafeDiagnosingMatcher<Value>
    {
        protected BaseValueMatcher()
        {
            super(Value.class);
        }
    }

    private static class ArrayValueMatcher extends BaseValueMatcher
    {
        private final Matcher<Iterable<? super Value>> matcher;

        protected ArrayValueMatcher(Matcher<Iterable<? super Value>> matcher)
        {
            this.matcher = matcher;
        }

        @Override
        protected final boolean matchesSafely(final Value value, final Description mismatchDescription)
        {
            if (value.getValueType() != Value.TYPE.ARRAY)
            {
                mismatchDescription.appendText("was of type " + value.getValueType());
                return false;
            }

            @SuppressWarnings ("unchecked") // this cast is safe - see the implementation fo value.getValueType()
                    List<Value> list = (List<Value>) value;
            if (!matcher.matches(list))
            {
                matcher.describeMismatch(list, mismatchDescription);
                return false;
            }

            return true;
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText(" of type " + Value.TYPE.ARRAY + " and ").appendDescriptionOf(matcher);
        }

    }

    private static class ValueWithStringPropertyMatcher extends BaseValueMatcher
    {
        private final String key;
        private final String value;

        public ValueWithStringPropertyMatcher(final String key, final String value)
        {
            this.key = key;
            this.value = value;
        }

        @Override
        protected boolean matchesSafely(final Value item, final Description mismatchDescription)
        {
            Value actualValue = item.get(key);
            if (actualValue == null)
            {
                mismatchDescription.appendText("no property with key '" + key + "'");
                return false;
            }
            if (actualValue.getValueType() != Value.TYPE.STRING)
            {
                mismatchDescription.appendText("property with key '" + key + "' is '" + actualValue.getValueType() +
                        "'");
                return false;
            }
            if (!value.equals(actualValue.getString()))
            {
                mismatchDescription.appendText("property with key '" + key + "' had value of '" +
                        actualValue.getString() + "'");
                return false;
            }
            return true;
        }

        @Override
        public void describeTo(final Description description)
        {
            description.appendText("value containing \"" + key + "\":\"" + value + "\"");
        }
    }

    public static Matcher<Value> isArrayMatching(Matcher<Iterable<? super Value>> matcher)
    {
        return new ArrayValueMatcher(matcher);
    }

    public static Matcher<Value> hasProperty(String key, String value)
    {
        return new ValueWithStringPropertyMatcher(key, value);
    }

}
