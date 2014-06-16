package com.atlassian.connect.xmldescriptor;

import com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor;

import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;

/**
 * Find source code annotated with {@link com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor}.
 * Include this module as a compile-time dependency of another module to get automatic compile-time generation of
 * `target/classes/XmlDescriptor_annotations.txt`.
 */
@SupportedAnnotationTypes("com.atlassian.plugin.connect.api.xmldescriptor.XmlDescriptor")
@SupportedSourceVersion(SourceVersion.RELEASE_6)
public class XmlDescriptorAnnotationProcessor extends CollectingAnnotationProcessor
{
    public XmlDescriptorAnnotationProcessor()
    {
        super(XmlDescriptor.class);
    }
}
