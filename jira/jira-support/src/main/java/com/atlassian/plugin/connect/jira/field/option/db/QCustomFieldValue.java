package com.atlassian.plugin.connect.jira.field.option.db;

import com.atlassian.pocketknife.api.querydsl.schema.SchemaProvider;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 * This is a table from JIRA
 */
public class QCustomFieldValue extends RelationalPathBase<QCustomFieldValue> {
    public static final String TABLE_NAME = "customfieldvalue";

    public final NumberPath<Long> id;
    public final NumberPath<Long> issue;
    public final NumberPath<Long> customfield;
    public final StringPath stringvalue;
    public final StringPath valuetype;

    QCustomFieldValue(String variable, SchemaProvider schemaProvider) {
        super(QCustomFieldValue.class,
                forVariable(variable),
                schemaProvider.getSchema(TABLE_NAME),
                schemaProvider.getTableName(TABLE_NAME));

        id = createNumber(schemaProvider.getColumnName(TABLE_NAME, "id"), Long.class);
        issue = createNumber(schemaProvider.getColumnName(TABLE_NAME, "issue"), Long.class);
        customfield = createNumber(schemaProvider.getColumnName(TABLE_NAME, "customfield"), Long.class);
        stringvalue = createString(schemaProvider.getColumnName(TABLE_NAME, "stringvalue"));
        valuetype = createString(schemaProvider.getColumnName(TABLE_NAME, "valuetype"));

        addMetadata(schemaProvider);
    }

    private void addMetadata(SchemaProvider schemaProvider) {
        addMetadata(id, ColumnMetadata.named(schemaProvider.getColumnName(TABLE_NAME, "id")).withIndex(1).ofType(Types.NUMERIC).withSize(18));
        addMetadata(issue, ColumnMetadata.named(schemaProvider.getColumnName(TABLE_NAME, "issue")).withIndex(2).ofType(Types.NUMERIC).withSize(18));
        addMetadata(customfield, ColumnMetadata.named(schemaProvider.getColumnName(TABLE_NAME, "customfield")).withIndex(3).ofType(Types.NUMERIC).withSize(18));
        addMetadata(stringvalue, ColumnMetadata.named(schemaProvider.getColumnName(TABLE_NAME, "stringvalue")).withIndex(5).ofType(Types.VARCHAR).withSize(255));
        addMetadata(valuetype, ColumnMetadata.named(schemaProvider.getColumnName(TABLE_NAME, "valuetype")).withIndex(9).ofType(Types.VARCHAR).withSize(255));
    }
}

