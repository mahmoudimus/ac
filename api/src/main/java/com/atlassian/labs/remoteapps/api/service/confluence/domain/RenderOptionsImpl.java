package com.atlassian.labs.remoteapps.api.service.confluence.domain;

/**
 */
class RenderOptionsImpl implements RenderOptions
{
    private RenderStyle style;

    public RenderStyle getStyle()
    {
        return style;
    }

    @Override
    public void setStyle(RenderStyle style)
    {
        this.style = style;
    }
}
