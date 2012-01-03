package com.atlassian.labs.remoteapps.modules;

import java.util.Map;

public interface WebItemContext
{
    Map<String, String> getContextParams();

    int getPreferredWeight();

    String getPreferredSectionKey();
}
