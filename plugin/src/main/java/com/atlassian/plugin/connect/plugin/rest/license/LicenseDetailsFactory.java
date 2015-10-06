package com.atlassian.plugin.connect.plugin.rest.license;

import com.atlassian.upm.api.license.entity.Contact;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.google.common.base.Function;
import org.joda.time.DateTime;

import java.util.Date;

import static com.google.common.collect.Iterables.transform;

/**
 */
public class LicenseDetailsFactory
{
    public static LicenseDetailsRepresentation createRemotablePluginLicense(PluginLicense pluginLicense)
    {
        return new LicenseDetailsRepresentation(pluginLicense.isValid(),
                pluginLicense.isEvaluation(),
                PluginLicenses.isNearlyExpired().apply(pluginLicense),
                pluginLicense.getEdition().getOrElse((Integer) null),
                pluginLicense.getMaintenanceExpiryDate().map(toDate)
                        .getOrElse((Date) null),
                pluginLicense.getLicenseType().name(),
                pluginLicense.getCreationDate().toString(),
                pluginLicense.isEvaluation() ? pluginLicense.getExpiryDate()
                        .map(toDate).getOrElse((Date) null) : null,
                pluginLicense.getMaintenanceExpiryDate()
                        .map(formatDate()).getOrElse((String) null),
                pluginLicense.getSupportEntitlementNumber().getOrElse((String) null),
                pluginLicense.getOrganization().getName(),
                getContactsEmail(pluginLicense.getContacts()),
                pluginLicense.isEnterprise());
    }

    private static final Function<DateTime, Date> toDate = new Function<DateTime, Date>()
    {
        @Override
        public Date apply(DateTime dateTime)
        {
            return dateTime.toDate();
        }
    };


    private static Function<DateTime, String> formatDate()
    {
        return new Function<DateTime, String>()
        {
            @Override
            public String apply(DateTime dateTime)
            {
                return dateTime.toString();           }
        };
    }

    private static String getContactsEmail(Iterable<Contact> contacts)
    {
        Iterable<String> emails = transform(contacts, new Function<Contact, String>()
        {
            @Override
            public String apply(Contact contact)
            {
                return contact.getEmail();
            }
        });

        return Strings.getFirstNonEmpty(emails).getOrElse((String) null);
    }
}
