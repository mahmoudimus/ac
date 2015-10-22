package com.atlassian.plugin.connect.jira.capabilities.descriptor.report;

import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.servlet.DownloadableResource;
import com.atlassian.plugin.webresource.transformer.TransformableResource;
import com.atlassian.plugin.webresource.transformer.TransformerUrlBuilder;
import com.atlassian.plugin.webresource.transformer.UrlReadingWebResourceTransformer;
import com.atlassian.plugin.webresource.url.UrlBuilder;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

@RunWith (MockitoJUnitRunner.class)
public class ReportModuleCssTransformerTest
{

    public static final String MD1_KEY = "test_addon1_test-report1";
    public static final String MD2_KEY = "test_addon1_test-report2";
    public static final String MD3_KEY = "test_addon2_test-report1";
    public static final String MD1_THUMBNAIL = "/thumbnail.png";
    public static final String MD2_THUMBNAIL = "/second-thumbnail.png";

    @Mock
    private PluginAccessor pluginAccessor;

    private ReportModuleCssTransformer reportModuleCssTransformer;

    @Before
    public void setUp() throws Exception
    {
        final List<ConnectReportModuleDescriptor.ModuleDescriptorImpl> moduleDescriptors = ImmutableList.of(
                createModuleDescriptor(MD1_KEY, MD1_THUMBNAIL),
                createModuleDescriptor(MD2_KEY, MD2_THUMBNAIL),
                createModuleDescriptor(MD3_KEY, null)
        );

        Mockito.when(pluginAccessor.getEnabledModuleDescriptorsByClass(ConnectReportModuleDescriptor.ModuleDescriptorImpl.class))
                .thenReturn(moduleDescriptors);

        reportModuleCssTransformer = new ReportModuleCssTransformer(pluginAccessor);
    }

    @Test
    public void testMakeUrlBuilder() throws Exception
    {
        final TransformerUrlBuilder transformerUrlBuilder = reportModuleCssTransformer.makeUrlBuilder(null);

        MatcherAssert.assertThat(transformerUrlBuilder, Matchers.instanceOf(ReportModuleCssTransformer.ReportModulesUriBuilder.class));
    }

    @Test
    public void testUrlBuilder() throws Exception
    {
        final UrlBuilder urlBuilder = Mockito.mock(UrlBuilder.class);
        final TransformerUrlBuilder transformerUrlBuilder = new ReportModuleCssTransformer.ReportModulesUriBuilder(pluginAccessor);
        transformerUrlBuilder.addToUrl(urlBuilder);

        final Object expectedHash = new HashCodeBuilder()
                .append(MD1_KEY)
                .append(MD2_KEY)
                .append(MD3_KEY)
                .build();

        Mockito.verify(urlBuilder).addToHash(org.mockito.Matchers.any(String.class), org.mockito.Matchers.eq(expectedHash));
    }

    @Test
    public void testMakeResourceTransformer() throws Exception
    {
        final DownloadableResource downloadableResource = Mockito.mock(DownloadableResource.class);
        final TransformableResource transformableResource = Mockito.mock(TransformableResource.class);
        Mockito.doReturn(downloadableResource).when(transformableResource).nextResource();

        final UrlReadingWebResourceTransformer urlReadingWebResourceTransformer = reportModuleCssTransformer.makeResourceTransformer(null);
        final DownloadableResource transformer = urlReadingWebResourceTransformer.transform(transformableResource, null);

        MatcherAssert.assertThat(transformer, Matchers.instanceOf(ReportModuleCssTransformer.ThumbnailCssClassesGenerator.class));
    }

    @Test
    public void testResourceTransformerThatGenerateCssClassForFirstPlugin() throws Exception
    {
        final String generatedCss = generateCss();

        MatcherAssert.assertThat(generatedCss, Matchers.containsString(MD1_KEY));
        MatcherAssert.assertThat(generatedCss, Matchers.containsString(MD1_THUMBNAIL));
    }

    @Test
    public void testResourceTransformerThatGenerateCssClassForSecondPlugin() throws Exception
    {
        final String generatedCss = generateCss();

        MatcherAssert.assertThat(generatedCss, Matchers.containsString(MD2_KEY));
        MatcherAssert.assertThat(generatedCss, Matchers.containsString(MD2_THUMBNAIL));
    }

    @Test
    public void testResourceTransformerThatNotGenerateCssClassForPluginWithEmptyThumbnailUrl() throws Exception
    {
        final String generatedCss = generateCss();

        MatcherAssert.assertThat(generatedCss, Matchers.not(Matchers.containsString(MD3_KEY)));
    }

    private String generateCss() {
        final ReportModuleCssTransformer.ThumbnailCssClassesGenerator thumbnailCssClassesGenerator = new ReportModuleCssTransformer.ThumbnailCssClassesGenerator(null, pluginAccessor);
        return thumbnailCssClassesGenerator.transform("").toString();
    }


    private ConnectReportModuleDescriptor.ModuleDescriptorImpl createModuleDescriptor(final String descriptorKey, final String thumbnailUrl)
    {
        final ConnectReportModuleDescriptor.ModuleDescriptorImpl moduleDescriptor = Mockito.mock(ConnectReportModuleDescriptor.ModuleDescriptorImpl.class);
        Mockito.doReturn(descriptorKey).when(moduleDescriptor).getKey();
        Mockito.doReturn(thumbnailUrl).when(moduleDescriptor).getThumbnailUrl();
        return moduleDescriptor;
    }
}