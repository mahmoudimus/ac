package com.atlassian.plugin.connect.jira.field;

import java.util.Objects;
import javax.annotation.Nonnull;

import com.atlassian.plugin.connect.api.util.ConnectPluginInfo;
import com.atlassian.plugin.connect.modules.util.ModuleKeyUtils;
import com.google.common.base.Preconditions;

public final class FieldId
{
    private final String addonKey;
    private final String fieldKey;

    public static FieldId of(@Nonnull String addonKey, @Nonnull String fieldKey)
    {
        return new FieldId(addonKey, fieldKey);
    }

    public String getCustomFieldTypeKey()
    {
        return ConnectPluginInfo.getPluginKey() + ":" + ModuleKeyUtils.addonAndModuleKey(addonKey, fieldKey);
    }

    private FieldId(String addonKey, String fieldKey)
    {
        this.addonKey = Preconditions.checkNotNull(addonKey);
        this.fieldKey = Preconditions.checkNotNull(fieldKey);
    }

    public String getAddonKey()
    {
        return addonKey;
    }

    public String getFieldKey()
    {
        return fieldKey;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        FieldId that = (FieldId) o;

        return Objects.equals(this.getAddonKey(), that.getAddonKey()) &&
                Objects.equals(this.getFieldKey(), that.getFieldKey());
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getAddonKey(), getFieldKey());
    }

    @Override
    public String toString()
    {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("addonKey", getAddonKey())
                .add("fieldKey", getFieldKey())
                .toString();
    }
}
