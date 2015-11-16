package com.atlassian.plugin.connect.plugin.auth;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SharedSecretServiceImpl implements SharedSecretService
{
    @Override
    public String next()
    {
        return UUID.randomUUID().toString();
    }
}
