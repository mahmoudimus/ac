package com.atlassian.labs.remoteapps.product;

import java.util.Map;

public interface EventMapper<T>
{
    public boolean handles(T e);

    public Map<String, Object> toMap(T e);
}
