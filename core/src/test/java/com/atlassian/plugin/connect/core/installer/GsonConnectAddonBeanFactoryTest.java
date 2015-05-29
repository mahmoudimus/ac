package com.atlassian.plugin.connect.core.installer;

import com.atlassian.plugin.connect.modules.schema.DescriptorValidationResult;
import com.atlassian.plugin.connect.modules.schema.JsonDescriptorValidator;
import com.atlassian.plugin.connect.core.capabilities.schema.ConnectSchemaLocator;
import com.atlassian.plugin.connect.core.capabilities.validate.AddOnBeanValidatorService;
import com.atlassian.plugin.connect.core.descriptor.InvalidDescriptorException;
import com.atlassian.plugin.connect.api.service.IsDevModeService;
import com.atlassian.sal.api.ApplicationProperties;
import com.atlassian.sal.api.message.I18nResolver;
import com.github.fge.jsonschema.core.exceptions.ProcessingException;
import com.github.fge.jsonschema.core.report.ListProcessingReport;
import com.github.fge.jsonschema.core.report.ProcessingMessage;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.Serializable;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class GsonConnectAddonBeanFactoryTest
{

    @InjectMocks
    private GsonConnectAddonBeanFactory addonBeanFactory;

    @Mock
    private JsonDescriptorValidator jsonDescriptorValidator;

    @Mock
    private AddOnBeanValidatorService addOnBeanValidatorService;

    @Mock
    private ConnectSchemaLocator connectSchemaLocator;

    @Mock
    private ApplicationProperties applicationProperties;

    @Mock
    private I18nResolver i18nResolver;

    @Mock
    private IsDevModeService isDevModeService;

    @Before
    public void setUp() throws IOException
    {
        when(connectSchemaLocator.getSchemaForCurrentProduct()).thenReturn("fake-schema");
    }

    @Test(expected = InvalidDescriptorException.class)
    public void shouldRejectMalformedDescriptor()
    {
        when(jsonDescriptorValidator.validate(anyString(), anyString())).thenReturn(new DescriptorValidationResult(false, false, null, null));
        assertFromJsonReturnsErrorMessage("connect.invalid.descriptor.malformed.json");
    }

    @Test(expected = InvalidDescriptorException.class)
    public void shouldRejectInvalidDescriptor()
    {
        String applicationName = "My App";
        when(applicationProperties.getDisplayName()).thenReturn(applicationName);
        when(jsonDescriptorValidator.validate(anyString(), anyString())).thenReturn(new DescriptorValidationResult(true, false, null, null));
        assertFromJsonReturnsErrorMessage("connect.install.error.remote.descriptor.validation", applicationName);
    }

    @Test(expected = InvalidDescriptorException.class)
    public void shouldRejectInvalidDescriptorWithReportInDevMode() throws ProcessingException
    {
        ListProcessingReport report = createProcessingReportWithErrors(ImmutableSet.of("An error", "Another error"));

        when(isDevModeService.isDevMode()).thenReturn(true);
        when(jsonDescriptorValidator.validate(anyString(), anyString())).thenReturn(new DescriptorValidationResult(report));
        assertFromJsonReturnsErrorMessage("connect.install.error.remote.descriptor.validation.dev", "<ul><li>An error<li>Another error</ul>");
    }

    private ListProcessingReport createProcessingReportWithErrors(Iterable<String> errors) throws ProcessingException
    {
        ListProcessingReport report = new ListProcessingReport();
        for (String error : errors)
        {
            ProcessingMessage message = new ProcessingMessage();
            message.setMessage(error);
            report.error(message);
        }
        return report;
    }

    private void assertFromJsonReturnsErrorMessage(String i18nKey, Serializable... params)
    {
        try
        {
            addonBeanFactory.fromJson("fake-descriptor", Maps.<String, String>newHashMap());
        }
        catch (InvalidDescriptorException e)
        {
            assertThat(e.getI18nMessageProperties().get().first(), equalTo(i18nKey));
            assertThat(e.getI18nMessageProperties().get().second(), equalTo(params));
            throw e;
        }
    }
}
