package com.atlassian.plugin.connect.plugin.rest.license;

import com.atlassian.upm.api.license.entity.PluginLicense;
import com.google.common.base.Predicate;
import org.joda.time.DateTime;
import org.joda.time.Interval;

/**
 * Utility methods for {@link com.atlassian.upm.api.license.entity.PluginLicense}s. Exists in this module instead of licensing-lib due to class dependencies
 * and to increase accessibility/reusability.
 *
 * fixme: this is copied from UPM master at 75cee855ebd6475a3e7d9b619694e613c8906f09
 *
 * Remove this once UPM supports this rest resource
 */
class PluginLicenses

{
    /**
     * Number of days before expiration when a license is considered to be "nearly expired".
     */
    public static final Integer NEARLY_EXPIRED_DAYS = 7;

    public static Predicate<PluginLicense> isNearlyExpired()
    {
        return license -> {
            for (DateTime expiryDate : license.getExpiryDate())
            {
                return new Interval(expiryDate.minusDays(NEARLY_EXPIRED_DAYS), expiryDate).contains(new DateTime());
            }
            return false; //no expiration date means it is never "nearly expired"
        };
    }
}
