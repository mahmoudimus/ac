package com.atlassian.plugin.remotable.plugin.module;

import java.util.Map;

public interface WebItemContext
{
    Map<String, String> getContextParams();

    int getPreferredWeight();

    String getPreferredSectionKey();
}
