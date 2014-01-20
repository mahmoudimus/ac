package com.atlassian.plugin.connect.test.pageobjects.confluence;

import com.atlassian.plugin.connect.test.pageobjects.AbstractConnectIFrameComponent;
import it.TestConstants;

public class RenderedMacro extends AbstractConnectIFrameComponent<RenderedMacro>
{
    private final String iframeId;

    public RenderedMacro(String macroKey, int count)
    {
        this.iframeId = TestConstants.IFRAME_ID_PREFIX + macroKey + "-" + count + TestConstants.IFRAME_ID_SUFFIX;
    }

    @Override
    protected String getFrameId()
    {
        return iframeId;
    }
}
