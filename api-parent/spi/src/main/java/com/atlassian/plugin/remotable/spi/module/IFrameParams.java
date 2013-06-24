package com.atlassian.plugin.remotable.spi.module;

import java.util.Map;

public interface IFrameParams
{
    Map<String, Object> getAsMap();

    void setParam(String key, String value);
}
