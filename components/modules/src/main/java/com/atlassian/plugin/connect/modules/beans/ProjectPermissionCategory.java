package com.atlassian.plugin.connect.modules.beans;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;

import java.util.EnumSet;
import java.util.Map;

public enum ProjectPermissionCategory
{
    PROJECTS("projects"),
    ISSUES("issues"),
    VOTERS_AND_WATCHERS("voters.and.watchers"),
    COMMENTS("comments"),
    ATTACHMENTS("attachments"),
    TIME_TRACKING("time.tracking"),
    OTHER("other");

    private static final Map<String, ProjectPermissionCategory> BY_KEY = Maps.uniqueIndex(
            EnumSet.allOf(ProjectPermissionCategory.class),
            new Function<ProjectPermissionCategory, String>()
            {
                @Override
                public String apply(final ProjectPermissionCategory input)
                {
                    return input.getKey();
                }
            });

    private final String key;

    ProjectPermissionCategory(String key)
    {
        this.key = key;
    }

    public String getKey()
    {
        return key;
    }

    public static Optional<ProjectPermissionCategory> byKey(String key)
    {
        return Optional.fromNullable(BY_KEY.get(key));
    }
}

