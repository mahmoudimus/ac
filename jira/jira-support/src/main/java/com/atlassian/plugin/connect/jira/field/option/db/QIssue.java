package com.atlassian.plugin.connect.jira.field.option.db;

import com.atlassian.pocketknife.api.querydsl.schema.SchemaProvider;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;

import java.sql.Types;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

public class QIssue extends RelationalPathBase<QIssue> {
    public static final String TABLE_NAME = "jiraissue";

    public final NumberPath<Long> id;
    public final NumberPath<Long> project;

    QIssue(String variable, SchemaProvider schemaProvider) {
        super(QIssue.class,
                forVariable(variable),
                schemaProvider.getSchema(TABLE_NAME),
                schemaProvider.getTableName(TABLE_NAME));

        id = createNumber(schemaProvider.getColumnName(TABLE_NAME, "id"), Long.class);
        project = createNumber(schemaProvider.getColumnName(TABLE_NAME, "issue"), Long.class);

        addMetadata(schemaProvider);
    }

    private void addMetadata(SchemaProvider schemaProvider) {
        addMetadata(id, ColumnMetadata.named(schemaProvider.getColumnName(TABLE_NAME, "id")).withIndex(1).ofType(Types.NUMERIC).withSize(18));
        addMetadata(project, ColumnMetadata.named(schemaProvider.getColumnName(TABLE_NAME, "project")).withIndex(2).ofType(Types.NUMERIC).withSize(18));
    }
}

