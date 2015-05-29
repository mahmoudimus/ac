package com.atlassian.plugin.connect.core.util.security;

import java.security.PublicKey;

public class DummyPublicKey implements PublicKey
{
    public static PublicKey EMPTY = new DummyPublicKey();

    private DummyPublicKey() {}

    @Override
    public String getAlgorithm()
    {
        return "";
    }

    @Override
    public String getFormat()
    {
        return "";
    }

    @Override
    public byte[] getEncoded()
    {
        return new byte[0];
    }
}
