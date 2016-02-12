package com.atlassian.plugin.connect.jira.field.option.db;

import com.atlassian.pocketknife.api.querydsl.schema.SchemaProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ConnectFieldTables
{
    public static final QRemoteFieldOption REMOTE_FIELD_OPTION = remoteFieldOption("RFO");

    private final SchemaProvider schemaProvider;

    @Autowired
    public ConnectFieldTables(final SchemaProvider schemaProvider)
    {
        this.schemaProvider = schemaProvider;
    }

    public static QRemoteFieldOption remoteFieldOption(String alias) {
        return new QRemoteFieldOption(alias, "", QRemoteFieldOption.AO_TABLE_NAME);
    }

    public QCustomFieldValue customFieldValue(String alias) {
        return new QCustomFieldValue(alias, schemaProvider);
    }
}
