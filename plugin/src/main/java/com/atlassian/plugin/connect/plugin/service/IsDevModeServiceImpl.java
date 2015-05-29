package com.atlassian.plugin.connect.plugin.service;

import com.atlassian.plugin.connect.api.service.IsDevModeService;
import org.springframework.stereotype.Component;

@Component
public class IsDevModeServiceImpl implements IsDevModeService
{
    @Override
    public boolean isDevMode()
    {
        return Boolean.parseBoolean(System.getProperty("atlassian.dev.mode", "false"));
    }
}
