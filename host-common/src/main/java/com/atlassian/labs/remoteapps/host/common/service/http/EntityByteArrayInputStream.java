package com.atlassian.labs.remoteapps.host.common.service.http;

import java.io.ByteArrayInputStream;

public class EntityByteArrayInputStream extends ByteArrayInputStream
{
    private byte[] bytes;

    public EntityByteArrayInputStream(byte[] bytes)
    {
        super(bytes);
        this.bytes = bytes;
    }

    public byte[] getBytes()
    {
        return bytes;
    }
}
