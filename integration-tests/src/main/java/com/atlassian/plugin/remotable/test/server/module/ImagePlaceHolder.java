package com.atlassian.plugin.remotable.test.server.module;

import org.dom4j.Element;

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

    @Override
    protected void addToElement(Element el)
    {
        addAttribute(el, "url", path);
    }
}
