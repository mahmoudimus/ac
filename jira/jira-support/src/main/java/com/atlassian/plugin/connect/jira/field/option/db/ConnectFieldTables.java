package com.atlassian.plugin.connect.jira.field.option.db;

import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.pocketknife.api.querydsl.schema.SchemaProvider;
import org.springframework.beans.factory.annotation.Autowired;

@JiraComponent
public class ConnectFieldTables {
    public static final QConnectFieldOption CONNECT_FIELD_OPTION = connectFieldOption("CFO");

    private final SchemaProvider schemaProvider;

    @Autowired
    public ConnectFieldTables(final SchemaProvider schemaProvider) {
        this.schemaProvider = schemaProvider;
    }

    public static QConnectFieldOption connectFieldOption(String alias) {
        return new QConnectFieldOption(alias, "", QConnectFieldOption.AO_TABLE_NAME);
    }

    public QCustomFieldValue customFieldValue(String alias) {
        return new QCustomFieldValue(alias, schemaProvider);
    }
}
