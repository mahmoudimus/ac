package com.atlassian.plugin.connect.plugin.capabilities.descriptor;

import com.atlassian.plugin.connect.plugin.module.webfragment.UrlVariableSubstitutor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class RelativeAddOnUrlConverterTest
{
    private static final String URL = "/irwi?issue_id=${issue.id}&project_key=${project.key}&pid=${project.id}";
    //    @Mock
//    UrlVariableSubstitutor urlVariableSubstitutor;

    @Test
    public void substitutesHost()
    {
        RelativeAddOnUrlConverter urlConverter = new RelativeAddOnUrlConverter(new UrlVariableSubstitutor());
        String localUrl = urlConverter.addOnUrlToLocalServletUrl("my-plugin", URL);
        assertThat(localUrl, is("/plugins/servlet/ac/my-plugin" + URL));
    }
}
