package com.atlassian.plugin.connect.jira.field.option;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.fugue.Either;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;

import static java.util.stream.Collectors.toList;

@JiraComponent
public class AvailableOptionDao
{
    private final ActiveObjects ao;

    @Autowired
    public AvailableOptionDao(final ActiveObjects activeObjects)
    {
        this.ao = activeObjects;
    }

    public Either<ErrorCollection, AvailableOption> create(final String addonKey, final String fieldKey, final String value)
    {
        AvailableOptionAO[] greatestOption = ao.find(AvailableOptionAO.class, "OPTION_ID", Query.select().where("ADDON_KEY = ? AND FIELD_KEY = ?", addonKey, fieldKey).order("OPTION_ID DESC").limit(1));
        int id = greatestOption.length > 0 ? greatestOption[0].getOptionId() + 1 : 1;
        AvailableOptionAO created = ao.create(AvailableOptionAO.class,
                new DBParam("ADDON_KEY", addonKey),
                new DBParam("FIELD_KEY", fieldKey),
                new DBParam("VALUE", value),
                new DBParam("OPTION_ID", id));

        return Either.right(toAvailableOption(created).get());
    }

    public List<AvailableOption> getAll(final String addonKey, final String fieldKey)
    {
        AvailableOptionAO[] availableOptions = ao.find(AvailableOptionAO.class, Query.select().where("ADDON_KEY = ? AND FIELD_KEY = ?", addonKey, fieldKey).order("OPTION_ID"));
        return Stream.of(availableOptions)
                .map(this::toAvailableOption)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    public Optional<AvailableOption> get(final String addonKey, final String fieldKey, final Integer optionId)
    {
        return getAvailableOptionAO(addonKey, fieldKey, optionId).flatMap(this::toAvailableOption);
    }

    public boolean delete(final String addonKey, final String fieldKey, final Integer optionId)
    {
        Optional<AvailableOptionAO> dbRow = getAvailableOptionAO(addonKey, fieldKey, optionId);
        dbRow.ifPresent(ao::delete);
        return dbRow.isPresent();
    }

    public Optional<AvailableOption> update(final String addonKey, final String fieldKey, final Integer id, final JsonValue value)
    {
        Optional<AvailableOptionAO> existingOption = getAvailableOptionAO(addonKey, fieldKey, id);
        existingOption.ifPresent(dbRow -> {
            dbRow.setValue(value.toJson());
            dbRow.save();
        });
        return existingOption.flatMap(this::toAvailableOption);
    }

    private Optional<AvailableOptionAO> getAvailableOptionAO(final String addonKey, final String fieldKey, final Integer optionId) {
        AvailableOptionAO[] availableOptions = ao.find(AvailableOptionAO.class, Query.select().where("ADDON_KEY = ? AND FIELD_KEY = ? AND OPTION_ID = ?", addonKey, fieldKey, optionId));
        return Stream.of(availableOptions).findFirst();
    }

    private Optional<AvailableOption> toAvailableOption(final AvailableOptionAO dbRow)
    {
        return JsonValue.parse(dbRow.getValue()).map(jsonValue -> AvailableOption.option(dbRow.getOptionId(), jsonValue));
    }
}
