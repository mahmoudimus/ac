package junit;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DebugInputStream extends InputStream
{
    InputStream is;
    ByteArrayOutputStream buffer;
    boolean debug;

    public DebugInputStream(InputStream in, boolean usedebug)
    {
        is = in;
        buffer = new ByteArrayOutputStream();
        debug = usedebug;
    }

    public int read() throws IOException
    {
        int input;

        input = is.read();
        if (debug)
        {
            buffer.write(input);
        }

        return input;
    }

    public int read(byte b[], int off, int len) throws IOException
    {
        int readb;

        readb = is.read(b, off, len);
        if (debug)
        {
            buffer.write(b, off, readb);
        }
        return readb;
    }

    public int available() throws IOException
    {
        return is.available();
    }

    public void close() throws IOException
    {
        buffer.close();
        is.close();
    }

    public void mark(int readlimit)
    {
        is.mark(readlimit);
    }

    public void reset() throws IOException
    {
        is.reset();
    }

    public boolean markSupported()
    {
        return is.markSupported();
    }


    public byte[] toByteArray()
    {
        return buffer.toByteArray();
    }

    public int size()
    {
        return buffer.size();
    }
}