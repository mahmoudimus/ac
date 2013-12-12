package com.atlassian.plugin.connect.plugin.service;

import org.springframework.stereotype.Component;

@Component
public class IsDevModeServiceImpl implements IsDevModeService
{
    private static final boolean IS_DEV_MODE = Boolean.parseBoolean(System.getProperty("atlassian.dev.mode","false"));

    @Override
    public boolean isDevMode()
    {
        return IS_DEV_MODE;
    }
}
