package com.atlassian.json.schema.doclet;

import com.atlassian.json.schema.annotation.SchemaIgnore;
import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.doclet.model.SchemaClassDoc;
import com.atlassian.json.schema.doclet.model.SchemaFieldDoc;
import com.atlassian.json.schema.util.StringUtil;
import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.sun.javadoc.*;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class JsonSchemaDoclet
{
    private static final Logger log = Logger.getLogger(JsonSchemaDoclet.class.getName());
    
    private static final String LS = System.getProperty("line.separator");
    private static final String P = LS + LS;
    private static final String OPTION_OUTPUT = "-output";
    private static final String TITLE_TAG = "schemaTitle";
    private static final String EXAMPLE_TAG = "exampleJson";
    private static final String SEE_TAG = "@see";
    private static final String TEXT_TAG = "Text";

    public static boolean start(RootDoc rootDoc)
    {
        final String output = getOptionArg(rootDoc.options(), OPTION_OUTPUT);

        JsonSchemaDocs rootSchemaDocs = new JsonSchemaDocs();
        List<SchemaClassDoc> schemaClassDocs = new ArrayList<SchemaClassDoc>();

        for (ClassDoc classDoc : rootDoc.classes())
        {
            SchemaClassDoc schemaClassDoc = new SchemaClassDoc();
            schemaClassDoc.setClassName(classDoc.qualifiedTypeName());
            schemaClassDoc.setClassDoc(getDocWithIncludes(classDoc));
            schemaClassDoc.setClassTitle(getTitle(classDoc));

            List<SchemaFieldDoc> schemaFieldDocs = new ArrayList<SchemaFieldDoc>();
            
            addFieldDocs(classDoc,schemaFieldDocs);
            

            schemaClassDoc.setFieldDocs(schemaFieldDocs);

            schemaClassDocs.add(schemaClassDoc);
        }

        rootSchemaDocs.setClassDocs(schemaClassDocs);

        Gson gson = new Gson();

        try
        {
            Files.write(gson.toJson(rootSchemaDocs), new File(output), Charsets.UTF_8);
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void addFieldDocs(ClassDoc classDoc, List<SchemaFieldDoc> schemaFieldDocs)
    {
        if(null == classDoc || Object.class.getName().equals(classDoc.qualifiedName()))
        {
            return;
        }
        
        for (FieldDoc fieldDoc : classDoc.fields())
        {
            if (!fieldDoc.isTransient() && !fieldDoc.isStatic() && !hasAnnotation(fieldDoc, SchemaIgnore.class))
            {
                SchemaFieldDoc schemaFieldDoc = new SchemaFieldDoc();
                schemaFieldDoc.setFieldName(fieldDoc.name());
                schemaFieldDoc.setFieldTitle(getTitle(fieldDoc));
                
                Doc docForField = fieldDoc;
                
                if (Strings.isNullOrEmpty(fieldDoc.commentText()))
                {
                    MethodDoc accessor = findFieldAccessor(classDoc, fieldDoc);
                    if (null != accessor && !Strings.isNullOrEmpty(accessor.commentText()))
                    {
                        docForField = accessor;
                    }
                }

                schemaFieldDoc.setFieldDocs(getDocWithIncludes(docForField));

                schemaFieldDocs.add(schemaFieldDoc);
            }
        }
        
        addFieldDocs(classDoc.superclass(),schemaFieldDocs);
    }

    private static boolean hasAnnotation(FieldDoc fieldDoc, Class<?> annoClass)
    {
        boolean foundAnnotation = false;
        
        for(AnnotationDesc annoDesc : fieldDoc.annotations())
        {
            if(annoDesc.annotationType().typeName().equals(annoClass.getName()) || annoDesc.annotationType().simpleTypeName().equals(annoClass.getSimpleName()))
            {
                foundAnnotation = true;
                break;
            }
        }
        
        return foundAnnotation;
    }

    private static String getDocWithIncludes(Doc doc)
    {
        StringBuilder sb = new StringBuilder();
        
        if (!Strings.isNullOrEmpty(doc.commentText()))
        {
            for(Tag tag : doc.inlineTags())
            {
                if(tag.kind().equals(TEXT_TAG))
                {
                    sb.append(P).append(tag.text());
                }
                else if(tag.kind().equals(SEE_TAG))
                {
                    sb.append(getIncludeFromLink((SeeTag) tag));
                }
            }
            //sb.append(doc.commentText()).append(LS).append(LS);
        }

        String example = getExamples(doc);
        if (!Strings.isNullOrEmpty(example))
        {
            sb.append(example);
        }

        return sb.toString();
    }

    private static String getExamples(Doc doc)
    {
        Tag[] exampleTags = getTagsOrNull(doc, EXAMPLE_TAG);
        StringBuilder sb = new StringBuilder(P);
        
        if (null != exampleTags)
        {
            for(Tag exampleTag : exampleTags)
            {
                final Tag[] inlineTags = exampleTag.inlineTags();

                if (null != inlineTags && inlineTags.length > 0)
                {
                    for (Tag inlineTag : inlineTags)
                    {
                        if (SEE_TAG.equals(inlineTag.name()))
                        {
                            final SeeTag linkTag = (SeeTag) inlineTag;
                            sb.append(getExampleFromLink(linkTag));
                        }
                        else if (!Strings.isNullOrEmpty(inlineTag.text()))
                        {
                            sb.append(inlineTag.text());
                        }
                        sb.append(P);
                    }
                }
            }
        }

        return sb.toString();
    }

    private static String getIncludeFromLink(SeeTag linkTag)
    {
        final MemberDoc fieldDoc = linkTag.referencedMember();

        if (null == fieldDoc || !fieldDoc.isStatic() || !fieldDoc.isField())
        {
            return "";
        }

        ClassDoc owner = fieldDoc.containingClass();

        try
        {
            Field declaredField = Class.forName(owner.qualifiedName(), false, Thread.currentThread().getContextClassLoader()).getDeclaredField(fieldDoc.name());

            if (!String.class.equals(declaredField.getType()))
            {
                return "";
            }

            if (fieldDoc.isFinal() || fieldDoc.isPrivate() || fieldDoc.isProtected())
            {
                declaredField.setAccessible(true);
            }

            return (String) declaredField.get(null);

        }
        catch (Exception e)
        {
            e.printStackTrace();
            return "";
        }
    }
    
    private static String getExampleFromLink(SeeTag linkTag)
    {
        String example = getIncludeFromLink(linkTag);
        return P + StringUtil.indent(example, 4);
    }

    private static String getTitle(Doc taggedDoc)
    {
        Tag titleTag = getSingleTagOrNull(taggedDoc, TITLE_TAG);

        if (null != titleTag && null != titleTag.text())
        {
            return titleTag.text();
        }

        return "";
    }

    private static MethodDoc findFieldAccessor(ClassDoc classDoc, FieldDoc fieldDoc)
    {
        String accessorName;

        if (fieldDoc.type().simpleTypeName().equals("boolean") || fieldDoc.type().simpleTypeName().equals("Boolean"))
        {
            accessorName = "is" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldDoc.name());
            
            //try to find method starting with "is". If not found, we'll fall through to finding the getter
            for (MethodDoc methodDoc : classDoc.methods())
            {
                if (methodDoc.name().equals(accessorName))
                {
                    return methodDoc;
                }
            }
        }
        
        accessorName = "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldDoc.name());
        
        for (MethodDoc methodDoc : classDoc.methods())
        {
            if (methodDoc.name().equals(accessorName))
            {
                return methodDoc;
            }
        }

        return null;
    }

    public static int optionLength(String option)
    {
        if (OPTION_OUTPUT.equals(option))
        {
            return 2;
        }

        return 0;
    }

    public static boolean validOptions(String[][] options, DocErrorReporter reporter)
    {
        return validOption(OPTION_OUTPUT, "<path-to-file>", options, reporter);
    }

    private static boolean validOption(String optionName, String reportOptionName, String[][] options, DocErrorReporter reporter)
    {
        final String option = getOptionArg(options, optionName);

        final boolean foundOption = (option != null && option.trim().length() > 0);
        if (!foundOption)
        {
            reporter.printError(optionName + " " + reportOptionName + " must be specified.");
        }
        return foundOption;
    }

    private static String getOptionArg(String[][] options, String option)
    {

        for (int i = 0; i < options.length; i++)
        {
            String[] opt = options[i];

            if (opt[0].equals(option))
            {
                return opt[1];
            }
        }

        return null;
    }

    private static Tag getSingleTagOrNull(Doc taggedDoc, String tagName)
    {
        final Tag[] tags = taggedDoc.tags(tagName);

        if (tags != null && tags.length > 0)
        {
            return tags[0];
        }

        return null;
    }

    private static Tag[] getTagsOrNull(Doc taggedDoc, String tagName)
    {
        final Tag[] tags = taggedDoc.tags(tagName);

        if (tags != null && tags.length > 0)
        {
            return tags;
        }

        return null;
    }
}
