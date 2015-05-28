package com.atlassian.plugin.connect.core.rest.license;

import com.atlassian.upm.api.util.Option;

import org.apache.commons.lang.StringUtils;

import static com.atlassian.upm.api.util.Option.none;
import static com.atlassian.upm.api.util.Option.some;

/**
 * fixme: this is copied from UPM master at 75cee855ebd6475a3e7d9b619694e613c8906f09
 *
 * Remove this once UPM supports this rest resource
 */
public class Strings
{
    public static Option<String> getFirstNonEmpty(Iterable<String> vals)
    {
        for (String val : vals)
        {
            if (!StringUtils.isEmpty(val))
            {
                return Option.some(val);
            }
        }
        return Option.none(String.class);
    }
}
