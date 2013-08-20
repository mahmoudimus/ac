package com.atlassian.plugin.connect.plugin.descriptor;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import com.google.common.base.Preconditions;
import com.google.common.io.InputSupplier;

import org.w3c.dom.ls.LSInput;

final class InputStreamSupplierLSInput implements LSInput
{
    private final String systemId;
    private final String publicId;
    private final InputSupplier<InputStream> inputSupplier;

    InputStreamSupplierLSInput(String systemId, String publicId, InputSupplier<InputStream> inputSupplier)
    {
        this.systemId = systemId;
        this.publicId = publicId;
        this.inputSupplier = Preconditions.checkNotNull(inputSupplier);
    }

    @Override
    public Reader getCharacterStream()
    {
        return null;
    }

    @Override
    public void setCharacterStream(Reader characterStream)
    {
    }

    @Override
    public InputStream getByteStream()
    {
        try
        {
            return inputSupplier.getInput();
        }
        catch (IOException e)
        {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void setByteStream(InputStream byteStream)
    {
    }

    @Override
    public String getStringData()
    {
        return null;
    }

    @Override
    public void setStringData(String stringData)
    {
    }

    @Override
    public String getSystemId()
    {
        return systemId;
    }

    @Override
    public void setSystemId(String systemId)
    {
    }

    @Override
    public String getPublicId()
    {
        return publicId;
    }

    @Override
    public void setPublicId(String publicId)
    {
    }

    @Override
    public String getBaseURI()
    {
        return null;
    }

    @Override
    public void setBaseURI(String baseURI)
    {
    }

    @Override
    public String getEncoding()
    {
        return null;
    }

    @Override
    public void setEncoding(String encoding)
    {
    }

    @Override
    public boolean getCertifiedText()
    {
        return false;
    }

    @Override
    public void setCertifiedText(boolean certifiedText)
    {
    }
}
