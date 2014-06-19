package com.atlassian.connect.xmldescriptor;

import com.atlassian.plugin.connect.api.xmldescriptor.OAuth;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;

/**
 * Find source code annotated with {@link com.atlassian.plugin.connect.api.xmldescriptor.OAuth}.
 * Include this module as a compile-time dependency of another module to get automatic compile-time generation of
 * `target/classes/OAuth_annotations.txt`.
 */
@SupportedAnnotationTypes("com.atlassian.plugin.connect.api.xmldescriptor.OAuth")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class OAuthAnnotationProcessor extends CollectingAnnotationProcessor
{
    public OAuthAnnotationProcessor()
    {
        super(OAuth.class);
    }

    @Override
    protected String getExtraDetails(Element element)
    {
        return null;
    }
}
