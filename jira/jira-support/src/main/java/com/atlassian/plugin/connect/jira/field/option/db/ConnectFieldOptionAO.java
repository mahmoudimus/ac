package com.atlassian.plugin.connect.jira.field.option.db;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import net.java.ao.Entity;
import net.java.ao.schema.Indexed;
import net.java.ao.schema.NotNull;
import net.java.ao.schema.StringLength;
import net.java.ao.schema.Table;

@Table("connect_field_option")
public interface ConnectFieldOptionAO extends Entity {
    @Indexed
    @NotNull
    int getOptionId();

    void setOptionId(long setOptionId);

    @Indexed
    @NotNull
    @StringLength(ConnectAddonBean.MAX_KEY_LENGTH)
    String getAddonKey();

    void setAddonKey(String key);

    @Indexed
    @NotNull
    @StringLength(ConnectAddonBean.MAX_KEY_LENGTH)
    String getFieldKey();

    void setFieldKey(String key);

    @NotNull
    @StringLength(StringLength.UNLIMITED)
    String getValue();

    void setValue(String value);

    @Indexed
    Long getProjectId();

    void setProjectId(Long projectId);
}
