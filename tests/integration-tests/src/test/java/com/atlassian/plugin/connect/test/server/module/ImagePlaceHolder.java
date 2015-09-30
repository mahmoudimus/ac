package com.atlassian.plugin.connect.test.server.module;

import static com.atlassian.fugue.Option.some;
import static com.google.common.base.Preconditions.checkNotNull;

public final class ImagePlaceHolder extends AbstractModuleWithResource<ImagePlaceHolder>
{
    private ImagePlaceHolder(String path)
    {
        super("image-placeholder");
        this.path = some(checkNotNull(path));
    }

    public static ImagePlaceHolder at(String path)
    {
        return new ImagePlaceHolder(path);
    }
}
