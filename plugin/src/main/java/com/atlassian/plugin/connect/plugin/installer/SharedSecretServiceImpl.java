package com.atlassian.plugin.connect.plugin.installer;

import java.util.UUID;

public class SharedSecretServiceImpl implements SharedSecretService
{
    @Override
    public String next()
    {
        return UUID.randomUUID().toString();
    }
}
