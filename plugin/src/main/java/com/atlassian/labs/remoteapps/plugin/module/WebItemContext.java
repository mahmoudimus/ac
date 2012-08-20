package com.atlassian.labs.remoteapps.plugin.module;

import java.util.Map;

public interface WebItemContext
{
    Map<String, String> getContextParams();

    int getPreferredWeight();

    String getPreferredSectionKey();
}
