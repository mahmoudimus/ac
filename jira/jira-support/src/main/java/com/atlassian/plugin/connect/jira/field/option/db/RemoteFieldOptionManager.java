package com.atlassian.plugin.connect.jira.field.option.db;

import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;

import com.atlassian.fugue.Either;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.connect.api.util.JsonCommon;
import com.atlassian.plugin.connect.jira.field.option.RemoteFieldOption;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.pocketknife.api.querydsl.DatabaseAccessor;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import static com.atlassian.plugin.connect.jira.field.option.db.Tables.REMOTE_FIELD_OPTION;
import static com.querydsl.core.types.dsl.Expressions.constant;
import static com.querydsl.sql.SQLExpressions.select;
import static java.util.stream.Collectors.toList;

@JiraComponent
public class RemoteFieldOptionManager
{
    private final DatabaseAccessor databaseAccessor;

    @Autowired
    public RemoteFieldOptionManager(final DatabaseAccessor databaseAccessor)
    {
        this.databaseAccessor = databaseAccessor;
    }

    public Either<ErrorCollection, RemoteFieldOption> create(final String addonKey, final String fieldKey, final String value)
    {
        return databaseAccessor.run(databaseConnection -> {
            Integer createdOptionId = databaseConnection.insert(REMOTE_FIELD_OPTION)
                    .columns(REMOTE_FIELD_OPTION.OPTION_ID, REMOTE_FIELD_OPTION.ADDON_KEY, REMOTE_FIELD_OPTION.FIELD_KEY, REMOTE_FIELD_OPTION.VALUE)
                    .select(select(REMOTE_FIELD_OPTION.OPTION_ID.max().add(constant(1)).coalesce(1), constant(addonKey), constant(fieldKey), constant(value))
                            .from(REMOTE_FIELD_OPTION)
                            .where(predicate(addonKey, fieldKey)))
                    .executeWithKey(REMOTE_FIELD_OPTION.ID);

            databaseConnection.commit();

            return Either.right(toRemoteFieldOption(
                    databaseConnection.select(REMOTE_FIELD_OPTION.OPTION_ID, REMOTE_FIELD_OPTION.VALUE)
                            .from(REMOTE_FIELD_OPTION)
                            .where(REMOTE_FIELD_OPTION.ID.eq(createdOptionId))
                            .fetchFirst()).get());
        });
    }

    public List<RemoteFieldOption> getAll(final String addonKey, final String fieldKey)
    {
        return databaseAccessor.run(connection -> {
            List<Tuple> tuples = connection
                    .select(REMOTE_FIELD_OPTION.OPTION_ID, REMOTE_FIELD_OPTION.VALUE)
                    .from(REMOTE_FIELD_OPTION)
                    .where(predicate(addonKey, fieldKey))
                    .orderBy(REMOTE_FIELD_OPTION.OPTION_ID.asc())
                    .fetch();

            return tuples.stream().map(this::toRemoteFieldOption).filter(Optional::isPresent).map(Optional::get).collect(toList());
        });
    }

    public Optional<RemoteFieldOption> get(final String addonKey, final String fieldKey, final Integer optionId)
    {
        return databaseAccessor.run(databaseConnection -> toRemoteFieldOption(databaseConnection
                .select(REMOTE_FIELD_OPTION.OPTION_ID, REMOTE_FIELD_OPTION.VALUE)
                .from(REMOTE_FIELD_OPTION)
                .where(predicate(addonKey, fieldKey, optionId))
                .fetchOne()));
    }

    public long delete(final String addonKey, final String fieldKey, final Integer optionId)
    {
        return databaseAccessor.run(connection -> {
            long deleted = connection.delete(REMOTE_FIELD_OPTION).where(predicate(addonKey, fieldKey, optionId)).execute();
            connection.commit();
            return deleted;
        });
    }

    public Optional<RemoteFieldOption> update(final String addonKey, final String fieldKey, final Integer optionId, final JsonNode value)
    {
        return databaseAccessor.run(connection -> {
            connection.update(REMOTE_FIELD_OPTION)
                    .where(predicate(addonKey, fieldKey, optionId))
                    .set(REMOTE_FIELD_OPTION.VALUE, value.toString())
                    .execute();

            connection.commit();

            return get(addonKey, fieldKey, optionId);
        });
    }

    private Optional<RemoteFieldOption> toRemoteFieldOption(@Nullable Tuple tuple)
    {
        if (tuple == null)
        {
            return Optional.empty();
        }
        else
        {
            return JsonCommon.parseStringToJson(tuple.get(REMOTE_FIELD_OPTION.VALUE)).map(json ->
                            RemoteFieldOption.option(tuple.get(REMOTE_FIELD_OPTION.OPTION_ID), json)
            );
        }
    }

    private Predicate predicate(final String addonKey, final String fieldKey, final Integer optionId)
    {
        return predicate(addonKey, fieldKey).and(REMOTE_FIELD_OPTION.OPTION_ID.eq(optionId));
    }

    private BooleanExpression predicate(final String addonKey, final String fieldKey)
    {
        return REMOTE_FIELD_OPTION.FIELD_KEY.eq(fieldKey).and(REMOTE_FIELD_OPTION.ADDON_KEY.eq(addonKey));
    }
}
