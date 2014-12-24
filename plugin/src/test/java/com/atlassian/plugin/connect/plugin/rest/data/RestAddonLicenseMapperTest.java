package com.atlassian.plugin.connect.plugin.rest.data;

import com.atlassian.upm.api.license.entity.Contact;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class RestAddonLicenseMapperTest
{

    private RestAddonLicenseMapper mapper;

    @Before
    public void setUp()
    {
        mapper = new RestAddonLicenseMapper();
    }

    @Test
    public void shouldSerializeFirstNamedContact() throws Exception
    {
        Contact contactMock = mock(Contact.class);
        Iterable<Contact> contacts = Lists.newArrayList(contactMock);

        when(contactMock.getName()).thenReturn("Charlie");
        when(contactMock.getEmail()).thenReturn("charlie@atlassian.com");

        assertThat(mapper.getFirstContactEmail(contacts), equalTo("\"Charlie\" <charlie@atlassian.com>"));
    }

    @Test
    public void shouldReturnFirstNonEmptyContact() {
        String email = "charlie@atlassian.com";
        Contact contactMock1 = mock(Contact.class);
        Contact contactMock2 = mock(Contact.class);
        Iterable<Contact> contacts = Lists.newArrayList(contactMock1, contactMock2);

        when(contactMock2.getEmail()).thenReturn(email);

        assertThat(mapper.getFirstContactEmail(contacts), equalTo(email));
    }
}