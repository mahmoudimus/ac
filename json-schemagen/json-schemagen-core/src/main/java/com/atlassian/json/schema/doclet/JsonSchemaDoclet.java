package com.atlassian.json.schema.doclet;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.atlassian.json.schema.doclet.model.JsonSchemaDocs;
import com.atlassian.json.schema.doclet.model.SchemaClassDoc;
import com.atlassian.json.schema.doclet.model.SchemaFieldDoc;

import com.google.common.base.CaseFormat;
import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import com.google.gson.Gson;
import com.sun.javadoc.*;

public class JsonSchemaDoclet
{
    private static final Logger log = Logger.getLogger(JsonSchemaDoclet.class.getName());
    
    private static final String OPTION_OUTPUT = "-output";
    private static final String TITLE_TAG = "schemaTitle";
    private static final String EXAMPLE_TAG = "exampleJson";
    private static final String SEE_TAG = "@see";

    public static boolean start(RootDoc rootDoc)
    {
        final String output = getOptionArg(rootDoc.options(), OPTION_OUTPUT);

        JsonSchemaDocs rootSchemaDocs = new JsonSchemaDocs();
        List<SchemaClassDoc> schemaClassDocs = new ArrayList<SchemaClassDoc>();

        for (ClassDoc classDoc : rootDoc.classes())
        {
            SchemaClassDoc schemaClassDoc = new SchemaClassDoc();
            schemaClassDoc.setClassName(classDoc.typeName());
            schemaClassDoc.setClassDoc(getDocWithExample(classDoc));
            schemaClassDoc.setClassTitle(getTitle(classDoc));

            List<SchemaFieldDoc> schemaFieldDocs = new ArrayList<SchemaFieldDoc>();

            for (FieldDoc fieldDoc : classDoc.fields())
            {
                if (!fieldDoc.isTransient() && !fieldDoc.isStatic())
                {
                    SchemaFieldDoc schemaFieldDoc = new SchemaFieldDoc();
                    schemaFieldDoc.setFieldName(fieldDoc.name());
                    schemaFieldDoc.setFieldTitle(getTitle(fieldDoc));

                    if (Strings.isNullOrEmpty(fieldDoc.commentText()))
                    {
                        MethodDoc accessor = findFieldAccessor(classDoc, fieldDoc);
                        if (null != accessor && !Strings.isNullOrEmpty(accessor.commentText()))
                        {
                            schemaFieldDoc.setFieldDocs(accessor.commentText());
                        }
                    }
                    else
                    {
                        schemaFieldDoc.setFieldDocs(fieldDoc.commentText());
                    }

                    schemaFieldDocs.add(schemaFieldDoc);
                }
            }

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

    private static String getDocWithExample(Doc doc)
    {
        StringBuilder sb = new StringBuilder();

        if (!Strings.isNullOrEmpty(doc.commentText()))
        {
            sb.append(doc.commentText()).append("\n");
        }

        String example = getExample(doc);

        if (!Strings.isNullOrEmpty(example))
        {
            sb.append(example);
        }

        return sb.toString();
    }

    private static String getExample(Doc doc)
    {
        Tag exampleTag = getSingleTagOrNull(doc, EXAMPLE_TAG);

        if (null != exampleTag)
        {
            final Tag[] inlineTags = exampleTag.inlineTags();

            if (null != inlineTags && inlineTags.length > 0)
            {
                for (Tag inlineTag : inlineTags)
                {
                    if (SEE_TAG.equals(inlineTag.name()))
                    {
                        final SeeTag linkTag = (SeeTag) inlineTag;
                        return getExampleFromLink(linkTag);
                    }
                    else if (!Strings.isNullOrEmpty(inlineTag.text()))
                    {
                        return inlineTag.text();
                    }
                }
            }
            else
            {
                if (!Strings.isNullOrEmpty(exampleTag.text()))
                {
                    return exampleTag.text();
                }
            }
        }

        return "";
    }

    private static String getExampleFromLink(SeeTag linkTag)
    {
        final MemberDoc fieldDoc = linkTag.referencedMember();

        if (null == fieldDoc || !fieldDoc.isStatic())
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
        }
        else
        {
            accessorName = "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, fieldDoc.name());
        }

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
}
