package it.com.atlassian.plugin.connect.plugin.web.condition;

import com.atlassian.plugin.Plugin;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.atlassian.plugin.web.descriptors.WebItemModuleDescriptor;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

class WebTestMatchers {
    private WebTestMatchers() {
    }

    static Matcher<WebItemModuleDescriptor> webItemWithKey(Plugin plugin, String key) {
        return new TypeSafeMatcher<WebItemModuleDescriptor>() {

            @Override
            protected boolean matchesSafely(WebItemModuleDescriptor item) {
                return getWebItemKey(item).equals(getWebItemModuleKey());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("web item with key ");
                description.appendValue(getWebItemModuleKey());
            }

            @Override
            protected void describeMismatchSafely(WebItemModuleDescriptor item, Description mismatchDescription) {
                mismatchDescription.appendText("web item with key ");
                mismatchDescription.appendValue(getWebItemKey(item));
            }

            private String getWebItemKey(WebItemModuleDescriptor item) {
                return item.getKey();
            }

            private String getWebItemModuleKey() {
                return ModuleKeyUtils.addonAndModuleKey(plugin.getKey(), key);
            }
        };
    }
}
