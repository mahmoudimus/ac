package com.atlassian.plugin.connect.jira.field.option;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.fugue.Either;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.connect.api.util.JsonCommon;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.codehaus.jackson.JsonNode;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.stream.Collectors.toList;

@JiraComponent
public class RemoteFieldOptionDao
{
    private final ActiveObjects ao;

    @Autowired
    public RemoteFieldOptionDao(final ActiveObjects activeObjects)
    {
        this.ao = activeObjects;
    }

    public Either<ErrorCollection, RemoteFieldOption> create(final String addonKey, final String fieldKey, final String value)
    {
        RemoteFieldOptionAO[] greatestOption = ao.find(RemoteFieldOptionAO.class, "OPTION_ID", Query.select().where("ADDON_KEY = ? AND FIELD_KEY = ?", addonKey, fieldKey).order("OPTION_ID DESC").limit(1));
        int id = greatestOption.length > 0 ? greatestOption[0].getOptionId() + 1 : 1;
        RemoteFieldOptionAO created = ao.create(RemoteFieldOptionAO.class,
                new DBParam("ADDON_KEY", addonKey),
                new DBParam("FIELD_KEY", fieldKey),
                new DBParam("VALUE", value),
                new DBParam("OPTION_ID", id));

        return Either.right(toAvailableOption(created).get());
    }

    public List<RemoteFieldOption> getAll(final String addonKey, final String fieldKey)
    {
        RemoteFieldOptionAO[] availableOptions = ao.find(RemoteFieldOptionAO.class, Query.select().where("ADDON_KEY = ? AND FIELD_KEY = ?", addonKey, fieldKey).order("OPTION_ID"));
        return Stream.of(availableOptions)
                .map(this::toAvailableOption)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    public Optional<RemoteFieldOption> get(final String addonKey, final String fieldKey, final Integer optionId)
    {
        return getAvailableOptionAO(addonKey, fieldKey, optionId).flatMap(this::toAvailableOption);
    }

    public boolean delete(final String addonKey, final String fieldKey, final Integer optionId)
    {
        Optional<RemoteFieldOptionAO> dbRow = getAvailableOptionAO(addonKey, fieldKey, optionId);
        dbRow.ifPresent(ao::delete);
        return dbRow.isPresent();
    }

    public Optional<RemoteFieldOption> update(final String addonKey, final String fieldKey, final Integer id, final JsonNode value)
    {
        Optional<RemoteFieldOptionAO> existingOption = getAvailableOptionAO(addonKey, fieldKey, id);
        existingOption.ifPresent(dbRow -> {
            dbRow.setValue(value.toString());
            dbRow.save();
        });
        return existingOption.flatMap(this::toAvailableOption);
    }

    private Optional<RemoteFieldOptionAO> getAvailableOptionAO(final String addonKey, final String fieldKey, final Integer optionId)
    {
        RemoteFieldOptionAO[] availableOptions = ao.find(RemoteFieldOptionAO.class, Query.select().where("ADDON_KEY = ? AND FIELD_KEY = ? AND OPTION_ID = ?", addonKey, fieldKey, optionId));
        return Stream.of(availableOptions).findFirst();
    }

    private Optional<RemoteFieldOption> toAvailableOption(final RemoteFieldOptionAO dbRow)
    {
        return JsonCommon.parseStringToJson(dbRow.getValue()).map(jsonValue -> RemoteFieldOption.option(dbRow.getOptionId(), jsonValue));
    }
}
