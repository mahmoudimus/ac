package com.atlassian.plugin.connect.plugin.rest.data;

import com.atlassian.plugin.connect.plugin.license.LicenseStatus;
import com.atlassian.upm.Strings;
import com.atlassian.upm.api.license.entity.Contact;
import com.atlassian.upm.api.license.entity.PluginLicense;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;

import static com.google.common.collect.Iterables.transform;

/**
 * A mapper from license objects to REST representations.
 */
public class RestAddonLicenseMapper
{
    public RestAddonLicense getRestAddonLicense(PluginLicense license)
    {
        return new RestAddonLicense(
                LicenseStatus.fromBoolean(license.isActive()),
                license.getLicenseType(),
                license.isEvaluation(),
                getFirstContactEmail(license.getContacts()),
                license.getSupportEntitlementNumber().getOrElse((String) null));
    }

    @VisibleForTesting
    protected String getFirstContactEmail(Iterable<Contact> contacts)
    {
        Iterable<String> emails = transform(contacts, new Function<Contact, String>()
        {
            @Override
            public String apply(Contact contact)
            {
                String email = contact.getEmail();
                if (email != null)
                {
                    String name = contact.getName();
                    if (name != null)
                    {
                        email = String.format("\"%s\" <%s>", name, email);
                    }
                }
                return email;
            }
        });

        return Strings.getFirstNonEmpty(emails).getOrElse((String) null);
    }
}
