package com.atlassian.plugin.connect.plugin.service;

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
