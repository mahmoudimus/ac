package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrl;
import com.atlassian.plugin.connect.plugin.capabilities.descriptor.url.RelativeAddOnUrlConverter;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RelativeAddOnUrlConverterTest
{
    private static final String URL = "/irwi?issue_id={issue.id}&project_key={project.key}&pid={project.id}";

    @Test
    public void prependsACContextToPath()
    {
        RelativeAddOnUrlConverter urlConverter = new RelativeAddOnUrlConverter();
        RelativeAddOnUrl localUrl = urlConverter.addOnUrlToLocalServletUrl("my-plugin", URL);
        assertThat(localUrl.getRelativeUri(), is("/plugins/servlet/ac/my-plugin" + URL));
    }
}
