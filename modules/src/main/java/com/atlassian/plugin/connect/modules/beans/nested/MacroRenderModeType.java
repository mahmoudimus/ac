package com.atlassian.plugin.connect.modules.beans.nested;

/**
 * Target format for macro render
 *
 * @schemaTitle Macro Render Mode Type
 */
public enum MacroRenderModeType {
    /**
     * Indicates your macro should output for mobile rendering.
     *
     * Note: Currently disabled, see AC-1210 and ACDEV-1400
     */
//    mobile,

    /**
     * Indicates your macro should out for desktop rendering.
     *
     * This is the default if a render mode is not specified.
     */
    desktop;

    @Override
    public String toString()
    {
        return super.toString().toLowerCase();
    }

}
