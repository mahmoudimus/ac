package com.atlassian.plugin.connect.confluence.macro;

import com.atlassian.confluence.macro.browser.beans.MacroIcon;
import com.atlassian.confluence.macro.browser.beans.MacroMetadata;

/**
 * Fixes metadata that incorrectly has an absolute icon url as relative.
 *
 * See https://jira.atlassian.com/browse/CONF-25394
 */
public class FixedMacroMetadata extends MacroMetadata
{
    public FixedMacroMetadata(MacroMetadata delegate)
    {
        super(
                delegate.getMacroName(),
                delegate.getPluginKey(),
                delegate.getTitle().getKey(),
                getFixedIcon(delegate.getIcon()),
                delegate.getDescription().getKey(),
                delegate.getAliases(),
                delegate.getCategories(),
                delegate.isBodyDeprecated(),
                delegate.isHidden(),
                delegate.getFormDetails(),
                delegate.getAlternateId(),
                delegate.getButtons()
        );
    }

    private static MacroIcon getFixedIcon(MacroIcon original)
    {
        if (original != null)
        {
            if (original.getLocation() != null && original.getLocation().startsWith("http") &&
                    original.isRelative())
            {
                return new MacroIcon(original.getLocation(), false, original.getHeight(), original.getWidth());
            }
        }
        return original;
    }
}
