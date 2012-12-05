package com.atlassian.plugin.remotable.container;

import java.io.File;

final class ContainerUtils
{
    private ContainerUtils()
    {
    }

    static File mkdirs(String path)
    {
        return mkdirs(new File(path));
    }

    static File mkdirs(File file)
    {
        if (file.exists())
        {
            if (file.isDirectory())
            {
                return file;
            }
            else
            {
                throw new IllegalStateException("Could not create directory '" + file.getAbsolutePath() + "'. " +
                        "This file already exists and is NOT a directory");
            }
        }
        else
        {
            if (file.mkdirs())
            {
                return file;
            }
            else
            {
                throw new IllegalStateException("Could not create directory '" + file.getAbsolutePath() + "'");
            }
        }
    }
}
