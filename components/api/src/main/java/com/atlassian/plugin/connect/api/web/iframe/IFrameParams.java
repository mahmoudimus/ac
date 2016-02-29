package com.atlassian.plugin.connect.api.web.iframe;

import java.util.Map;

public interface IFrameParams {
    Map<String, Object> getAsMap();

    void setParam(String key, String value);
}
