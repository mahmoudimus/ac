package com.atlassian.plugin.connect.plugin.util;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class KeysFromPathMatcher
{
    // Matches addOnKey and moduleKey from url: /addon.key/module.key
    private static final Pattern PATH_PATTERN = Pattern.compile("^/([^/]+)/([^/]+)");

    public Optional<AddOnKeyAndModuleKey> getAddOnKeyAndModuleKey(String path)
    {
        Matcher matcher = PATH_PATTERN.matcher(path);
        if (!matcher.find())
        {
            return Optional.empty();
        }
        String addOnKey = matcher.group(1);
        String moduleKey = matcher.group(2);

        return Optional.of(new AddOnKeyAndModuleKey(addOnKey, moduleKey));
    }

    public static final class AddOnKeyAndModuleKey
    {
        private final String addOnKey;
        private final String moduleKey;

        private AddOnKeyAndModuleKey(final String addOnKey, final String moduleKey) {
            this.addOnKey = addOnKey;
            this.moduleKey = moduleKey;
        }

        public String getAddOnKey()
        {
            return addOnKey;
        }

        public String getModuleKey()
        {
            return moduleKey;
        }
    }
}
