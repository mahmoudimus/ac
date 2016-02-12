package com.atlassian.plugin.connect.jira.field.option.db;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.atlassian.fugue.Either;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.connect.jira.field.option.Json;
import com.atlassian.plugin.connect.jira.field.option.ConnectFieldOption;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.jira.field.option.db.ConnectFieldTables.CONNECT_FIELD_OPTION;
import static com.querydsl.core.types.dsl.Expressions.constant;
import static com.querydsl.sql.SQLExpressions.select;
import static java.util.stream.Collectors.toList;

@JiraComponent
public class ConnectFieldOptionManager
{
    private final DatabaseAccessor databaseAccessor;

    @Autowired
    public ConnectFieldOptionManager(final DatabaseAccessor databaseAccessor)
    {
        this.databaseAccessor = databaseAccessor;
    }

    public Either<ErrorCollection, ConnectFieldOption> create(final String addonKey, final String fieldKey, final String value)
    {
        return databaseAccessor.run(databaseConnection -> {
            Integer createdOptionId = databaseConnection.insert(CONNECT_FIELD_OPTION)
                    .columns(CONNECT_FIELD_OPTION.OPTION_ID, CONNECT_FIELD_OPTION.ADDON_KEY, CONNECT_FIELD_OPTION.FIELD_KEY, CONNECT_FIELD_OPTION.VALUE)
                    .select(select(CONNECT_FIELD_OPTION.OPTION_ID.max().add(constant(1)).coalesce(1), constant(addonKey), constant(fieldKey), constant(value))
                            .from(CONNECT_FIELD_OPTION)
                            .where(predicate(addonKey, fieldKey)))
                    .executeWithKey(CONNECT_FIELD_OPTION.ID);

            databaseConnection.commit();

            return Either.right(toConnectFieldOption(
                    databaseConnection.select(CONNECT_FIELD_OPTION.OPTION_ID, CONNECT_FIELD_OPTION.VALUE)
                            .from(CONNECT_FIELD_OPTION)
                            .where(CONNECT_FIELD_OPTION.ID.eq(createdOptionId))
                            .fetchFirst()).get());
        });
    }

    public List<ConnectFieldOption> getAll(final String addonKey, final String fieldKey)
    {
        return databaseAccessor.run(connection -> {
            List<Tuple> tuples = connection
                    .select(CONNECT_FIELD_OPTION.OPTION_ID, CONNECT_FIELD_OPTION.VALUE)
                    .from(CONNECT_FIELD_OPTION)
                    .where(predicate(addonKey, fieldKey))
                    .orderBy(CONNECT_FIELD_OPTION.OPTION_ID.asc())
                    .fetch();

            return tuples.stream().map(this::toConnectFieldOption).filter(Optional::isPresent).map(Optional::get).collect(toList());
        });
    }

    public Optional<ConnectFieldOption> get(final String addonKey, final String fieldKey, final Integer optionId)
    {
        return databaseAccessor.run(databaseConnection -> toConnectFieldOption(databaseConnection
                .select(CONNECT_FIELD_OPTION.OPTION_ID, CONNECT_FIELD_OPTION.VALUE)
                .from(CONNECT_FIELD_OPTION)
                .where(predicate(addonKey, fieldKey, optionId))
                .fetchOne()));
    }

    public long delete(final String addonKey, final String fieldKey, final Integer optionId)
    {
        return databaseAccessor.run(connection -> {
            long deleted = connection.delete(CONNECT_FIELD_OPTION).where(predicate(addonKey, fieldKey, optionId)).execute();
            connection.commit();
            return deleted;
        });
    }

    public Optional<ConnectFieldOption> update(final String addonKey, final String fieldKey, final Integer optionId, final JsonNode value)
    {
        return databaseAccessor.run(connection -> {
            connection.update(CONNECT_FIELD_OPTION)
                    .where(predicate(addonKey, fieldKey, optionId))
                    .set(CONNECT_FIELD_OPTION.VALUE, value.toString())
                    .execute();

            connection.commit();

            return get(addonKey, fieldKey, optionId);
        });
    }

    private Optional<ConnectFieldOption> toConnectFieldOption(@Nullable Tuple tuple)
    {
        if (tuple == null)
        {
            return Optional.empty();
        }
        else
        {
            return Json.parse(tuple.get(CONNECT_FIELD_OPTION.VALUE)).map(json ->
                            ConnectFieldOption.of(tuple.get(CONNECT_FIELD_OPTION.OPTION_ID), json)
            );
        }
    }

    private Predicate predicate(final String addonKey, final String fieldKey, final Integer optionId)
    {
        return predicate(addonKey, fieldKey).and(CONNECT_FIELD_OPTION.OPTION_ID.eq(optionId));
    }

    private BooleanExpression predicate(final String addonKey, final String fieldKey)
    {
        return CONNECT_FIELD_OPTION.FIELD_KEY.eq(fieldKey).and(CONNECT_FIELD_OPTION.ADDON_KEY.eq(addonKey));
    }
}
