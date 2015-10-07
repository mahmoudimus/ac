package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.api.service.IsDevModeService;
import com.atlassian.plugin.util.PluginUtils;
import org.springframework.stereotype.Component;

@Component
public class IsDevModeServiceImpl implements IsDevModeService
{
    @Override
    public boolean isDevMode()
    {
        return Boolean.getBoolean(PluginUtils.ATLASSIAN_DEV_MODE);
    }
}
