package com.atlassian.plugin.connect.plugin.module.jira.context.serializer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.version.VersionService;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.user.util.UserManager;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.jira.bc.project.version.VersionService.VersionResult;

/**
 * Serializes Version objects.
 */
public class VersionSerializer extends AbstractJiraParameterSerializer<Version, VersionResult>
{

    public static final String VERSION_FIELD_NAME = "version";

    public VersionSerializer(final VersionService versionService, UserManager userManager)
    {
        super(userManager, VERSION_FIELD_NAME,
                new ParameterUnwrapper<VersionResult, Version>()
                {
                    @Override
                    public Version unwrap(VersionResult wrapped)
                    {
                        return wrapped.getVersion();
                    }
                },
                new AbstractIdParameterLookup<VersionResult>()
                {
                    @Override
                    public VersionResult lookup(User user, Long id)
                    {
                        return versionService.getVersionById(user, id);
                    }
                }
        );
    }

    @Override
    public Map<String, Object> serialize(final Version version)
    {
        return ImmutableMap.<String, Object>of(VERSION_FIELD_NAME, ImmutableMap.of(ID_FIELD_NAME, version.getId()));
    }

}
