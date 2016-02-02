package com.atlassian.plugin.connect.plugin.web.dialog;

import com.atlassian.plugin.connect.modules.beans.ConnectModuleMeta;
import com.atlassian.plugin.connect.modules.beans.ConnectModuleValidationException;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.dialog.DialogOptions;
import com.google.common.annotations.VisibleForTesting;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * Validates DialogOptions fields that can't be tested against the schema.
 */
public class DialogOptionsValidator
{
    private static final Pattern DIM_PATTERN = Pattern.compile("(\\d+)(px|%)?");

    private final ShallowConnectAddonBean descriptor;
    private final ConnectModuleMeta meta;

    public DialogOptionsValidator(ShallowConnectAddonBean descriptor, ConnectModuleMeta meta)
    {
        this.descriptor = descriptor;
        this.meta = meta;
    }

    public void validate(DialogOptions options)
            throws ConnectModuleValidationException
    {
        String height = options.getHeight();
        String width = options.getWidth();
        validateDimension(height);
        validateDimension(width);

        // Height and Width are ignored when size is specified, but if it isn't they must be provided together
        // or not at all.
        if (options.getSize() == null && (isNotBlank(height) != isNotBlank(width)))
        {
            throw new ConnectModuleValidationException(descriptor, meta,
                        "Both height and width must be specified",
                        "connect.install.error.invalid.dialogoptions.dimensions");
        }
    }

    @VisibleForTesting
    void validateDimension(String dimension)
            throws ConnectModuleValidationException
    {
        if (isBlank(dimension))
            return;

        Matcher matcher = DIM_PATTERN.matcher(dimension);
        if (!matcher.matches())
            throwDimensionalError(dimension);

        int number = 0;
        try
        {
            number = Integer.parseInt(matcher.group(1));
        }
        catch (NumberFormatException e)
        {
            // Although we can probably assume that anything over a few thousand isn't legit...
            throwDimensionalError(dimension);
        }

        if (number == 0)
            throwDimensionalError(dimension);

        String suffix = matcher.group(2);
        if ("%".equals(suffix) && number > 100)
            // 110%? That's impossible. No one can give more than 100%.
            throwDimensionalError(dimension);
    }

    private void throwDimensionalError(String dimension)
            throws ConnectModuleValidationException
    {
        throw new ConnectModuleValidationException(descriptor, meta,
                String.format("Invalid DialogOptions dimension string: %s", dimension),
                "connect.install.error.invalid.dialogoptions.dimension", dimension);
    }
}
