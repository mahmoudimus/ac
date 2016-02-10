package com.atlassian.plugin.connect.jira.field.option.db;

import java.sql.Types;

import com.atlassian.pocketknife.api.querydsl.schema.SchemaProvider;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;

import static com.querydsl.core.types.PathMetadataFactory.forVariable;

/**
 *
 * Generated by https://bitbucket.org/atlassian/querydsl-ao-code-gen
 */
public class QRemoteFieldOption extends RelationalPathBase<QRemoteFieldOption>
{

    private static final long serialVersionUID = -1183967618L;

    public static final String AO_TABLE_NAME = "AO_F4ED3A_REMOTE_FIELD_OPTION";

    public static final QRemoteFieldOption withSchema(SchemaProvider schemaProvider)
    {
        String schema = schemaProvider.getSchema(AO_TABLE_NAME);
        return new QRemoteFieldOption("REMOTE_FIELD_OPTION", schema, AO_TABLE_NAME);
    }

    /**
     * Database Columns
     */
    public final StringPath ADDON_KEY = createString("ADDON_KEY");

    public final StringPath FIELD_KEY = createString("FIELD_KEY");

    public final NumberPath<Integer> ID = createNumber("ID", Integer.class);

    public final NumberPath<Integer> OPTION_ID = createNumber("OPTION_ID", Integer.class);

    public final StringPath VALUE = createString("VALUE");

    public final com.querydsl.sql.PrimaryKey<QRemoteFieldOption> REMOTE_FIELD_OPTION_PK = createPrimaryKey(ID);

    public QRemoteFieldOption(String variable, String schema, String table)
    {
        super(QRemoteFieldOption.class, forVariable(variable), schema, table);
        addMetadata();
    }

    private void addMetadata()
    {
        /**
         * Database Metadata is not yet used by QueryDSL but it might one day.
         */
        addMetadata(ADDON_KEY, ColumnMetadata.named("ADDON_KEY").ofType(Types.VARCHAR)); // .withSize(80).withNotNull()); // until detect primitive types, int ..
        addMetadata(FIELD_KEY, ColumnMetadata.named("FIELD_KEY").ofType(Types.VARCHAR)); // .withSize(80).withNotNull()); // until detect primitive types, int ..
        addMetadata(ID, ColumnMetadata.named("ID").ofType(Types.INTEGER)); // .withSize(0).withNotNull()); // until detect primitive types, int ..
        addMetadata(OPTION_ID, ColumnMetadata.named("OPTION_ID").ofType(Types.INTEGER)); // .withSize(0).withNotNull()); // until detect primitive types, int ..
        addMetadata(VALUE, ColumnMetadata.named("VALUE").ofType(Types.VARCHAR)); // .withSize(2147483647).withNotNull()); // until detect primitive types, int ..
    }
}