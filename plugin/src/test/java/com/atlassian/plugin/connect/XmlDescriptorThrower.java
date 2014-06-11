package com.atlassian.plugin.connect;

import java.util.Set;

public interface XmlDescriptorThrower
{
    public Set<Class> runAndGetProxyFailures();
}
