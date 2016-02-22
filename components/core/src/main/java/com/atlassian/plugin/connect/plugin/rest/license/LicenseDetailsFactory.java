package com.atlassian.plugin.connect.plugin.rest.license;

import com.atlassian.upm.api.license.entity.Contact;
import com.atlassian.upm.api.license.entity.PluginLicense;

import org.joda.time.DateTime;

import java.util.Date;

import static com.google.common.collect.Iterables.transform;

/**
 */
public class LicenseDetailsFactory {
    public static LicenseDetailsRepresentation createRemotablePluginLicense(PluginLicense pluginLicense) {
        return new LicenseDetailsRepresentation(pluginLicense.isValid(),
                pluginLicense.isEvaluation(),
                PluginLicenses.isNearlyExpired().apply(pluginLicense),
                pluginLicense.getEdition().getOrElse((Integer) null),
                pluginLicense.getMaintenanceExpiryDate().map(DateTime::toDate)
                        .getOrElse((Date) null),
                pluginLicense.getLicenseType().name(),
                pluginLicense.getCreationDate().toString(),
                pluginLicense.isEvaluation() ? pluginLicense.getExpiryDate()
                        .map(DateTime::toDate).getOrElse((Date) null) : null,
                pluginLicense.getMaintenanceExpiryDate()
                        .map(DateTime::toString).getOrElse((String) null),
                pluginLicense.getSupportEntitlementNumber().getOrElse((String) null),
                pluginLicense.getOrganization().getName(),
                getContactsEmail(pluginLicense.getContacts()),
                pluginLicense.isEnterprise());
    }

    private static String getContactsEmail(Iterable<Contact> contacts) {
        Iterable<String> emails = transform(contacts, Contact::getEmail);

        return Strings.getFirstNonEmpty(emails).getOrElse((String) null);
    }
}
