package com.atlassian.plugin.connect.plugin.property;

import com.atlassian.activeobjects.external.ActiveObjects;
import com.atlassian.plugin.connect.api.property.AddonProperty;
import com.atlassian.plugin.connect.api.property.AddonPropertyIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import net.java.ao.DBParam;
import net.java.ao.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * This class allows for persisting add-on properties.
 * @see com.atlassian.plugin.connect.plugin.property.AddonPropertyServiceImpl
 */
@Component
public class AddonPropertyStore {
    private final ActiveObjects ao;

    private static final String MAX_PROPERTIES_SYSTEM_PROPERTY = "com.atlassian.plugin.connect.add_on_properties.max_properties";
    private static final int MAX_PROPERTIES_DEFAULT = 100;
    public static final int MAX_PROPERTIES_PER_ADD_ON = Integer.getInteger(MAX_PROPERTIES_SYSTEM_PROPERTY,
            MAX_PROPERTIES_DEFAULT);

    @Autowired
    public AddonPropertyStore(final ActiveObjects ao) {
        this.ao = checkNotNull(ao);
    }

    public Optional<AddonProperty> getPropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey) {
        AddonPropertyAO[] properties = ao.find(AddonPropertyAO.class, Query.select().where("PLUGIN_KEY = ? AND PROPERTY_KEY = ?", addonKey, propertyKey));

        Optional<AddonPropertyAO> option = Optional.ofNullable(Iterables.getFirst(Arrays.asList(properties), null));

        return option.flatMap(addonPropertyAO -> Optional.of(AddonPropertyFactory.fromAO(addonPropertyAO)));
    }

    public PutResultWithOptionalProperty setPropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey, @Nonnull final String value) {
        checkNotNull(addonKey);
        checkNotNull(propertyKey);
        checkNotNull(value);
        if (hasReachedPropertyLimit(addonKey)) {
            return new PutResultWithOptionalProperty(PutResult.PROPERTY_LIMIT_EXCEEDED, Optional.<AddonProperty>empty());
        }
        if (existsProperty(addonKey, propertyKey)) {
            AddonPropertyAO previousPropertyAO = getAddonPropertyForKey(addonKey, propertyKey);
            ao.delete(previousPropertyAO); //delete and create to get a new auto-incremented version for property

            AddonPropertyAO newPropertyAO = createAddonProperty(addonKey, propertyKey, value);
            newPropertyAO.save();
            return new PutResultWithOptionalProperty(PutResult.PROPERTY_UPDATED, Optional.of(AddonPropertyFactory.fromAO(newPropertyAO)));
        } else {
            AddonPropertyAO property = createAddonProperty(addonKey, propertyKey, value);
            property.save();
            return new PutResultWithOptionalProperty(PutResult.PROPERTY_CREATED, Optional.of(AddonPropertyFactory.fromAO(property)));
        }
    }

    private AddonPropertyAO createAddonProperty(final String addonKey, final String propertyKey, final String value) {
        return ao.create(AddonPropertyAO.class,
                new DBParam("PLUGIN_KEY", addonKey),
                new DBParam("PROPERTY_KEY", propertyKey),
                new DBParam("VALUE", value),
                new DBParam("PRIMARY_KEY", getPrimaryKeyForProperty(addonKey, propertyKey)));
    }

    public void deletePropertyValue(@Nonnull final String addonKey, @Nonnull final String propertyKey) {
        checkNotNull(addonKey);
        checkNotNull(propertyKey);

        AddonPropertyAO propertyAO = getAddonPropertyForKey(addonKey, propertyKey);
        if (propertyAO != null) {
            ao.delete(propertyAO);
        }
    }

    public AddonPropertyIterable getAllPropertiesForAddonKey(@Nonnull final String addonKey) {
        return ao.executeInTransaction(() -> {
            ImmutableList<AddonPropertyAO> addonPropertyAOList = ImmutableList.<AddonPropertyAO>builder().add(getAddonPropertyAOArrayForAddonKey(addonKey)).build();
            return AddonPropertyFactory.fromAddonPropertyAOList(addonPropertyAOList);
        });
    }

    public <T> T executeInTransaction(@Nonnull final TransactionAction<T> function) {
        return ao.executeInTransaction(function::call);
    }

    private boolean existsProperty(@Nonnull final String addonKey, @Nonnull final String propertyKey) {
        return getAddonPropertyForKey(addonKey, propertyKey) != null;
    }

    private AddonPropertyAO[] getAddonPropertyAOArrayForAddonKey(final String addonKey) {
        return ao.find(AddonPropertyAO.class, Query.select().where("PLUGIN_KEY = ?", addonKey));
    }

    private String getPrimaryKeyForProperty(@Nonnull final String addonKey, @Nonnull final String propertyKey) {
        return String.format("%s:%s", addonKey, propertyKey);
    }

    private AddonPropertyAO getAddonPropertyForKey(@Nonnull final String addonKey, @Nonnull final String propertyKey) {
        AddonPropertyAO[] addonPropertyAOs = ao.find(AddonPropertyAO.class, Query.select().where("PRIMARY_KEY = ?", getPrimaryKeyForProperty(addonKey, propertyKey)));
        return Iterables.getFirst(Arrays.asList(addonPropertyAOs), null);
    }

    private boolean hasReachedPropertyLimit(@Nonnull final String addonKey) {
        return ao.count(AddonPropertyAO.class, Query.select().where("PLUGIN_KEY = ?", addonKey)) >= MAX_PROPERTIES_PER_ADD_ON;
    }

    public enum PutResult {
        PROPERTY_CREATED,
        PROPERTY_UPDATED,
        PROPERTY_LIMIT_EXCEEDED
    }

    public static class PutResultWithOptionalProperty {
        private final PutResult result;
        private final Optional<AddonProperty> property;

        public PutResultWithOptionalProperty(final PutResult result, final Optional<AddonProperty> property) {
            this.result = result;
            this.property = property;
        }

        public PutResult getResult() {
            return result;
        }

        public Optional<AddonProperty> getProperty() {
            return property;
        }
    }

    public interface TransactionAction<T> {
        T call();
    }
}

