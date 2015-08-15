package com.atlassian.plugin.connect.plugin.redirect;

public interface RedirectRegistry
{
    void register(String addonKey, String moduleKey, RedirectData renderStrategy);

    RedirectData get(String addonKey, String moduleKey);

    final class RedirectData
    {
        private final String urlTemplate;

        public RedirectData(final String urlTemplate)
        {
            this.urlTemplate = urlTemplate;
        }

        public String getUrlTemplate()
        {
            return urlTemplate;
        }

    }
}



