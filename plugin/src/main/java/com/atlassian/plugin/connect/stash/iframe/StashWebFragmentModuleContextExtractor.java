package com.atlassian.plugin.connect.stash.iframe;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import com.atlassian.plugin.connect.api.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.spi.iframe.webpanel.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.connect.stash.iframe.context.StashModuleContextParameters;
import com.atlassian.plugin.connect.stash.iframe.context.StashModuleContextParametersImpl;
import com.atlassian.plugin.spring.scanner.annotation.component.StashComponent;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.atlassian.stash.project.Project;
import com.atlassian.stash.repository.Repository;

import com.google.common.collect.ImmutableList;

@StashComponent
public class StashWebFragmentModuleContextExtractor implements WebFragmentModuleContextExtractor
{
    private final Iterable<ParameterExtractor<?>> parameterExtractors;
    private final UserManager userManager;

    @Inject
    public StashWebFragmentModuleContextExtractor(UserManager userManager)
    {
        this.userManager = userManager;
        parameterExtractors = constructParameterExtractors();
    }

    @Override
    public ModuleContextParameters extractParameters(final Map<String, ? extends Object> webFragmentContext)
    {
        if (ModuleContextParameters.class.isAssignableFrom(webFragmentContext.getClass()))
        {
            return (ModuleContextParameters) webFragmentContext;
        }

        StashModuleContextParameters moduleContext = new StashModuleContextParametersImpl();

        for (ParameterExtractor extractor : parameterExtractors)
        {
            Object obj = webFragmentContext.get(extractor.getContextKey());
            if (obj != null)
            {
                Class<?> type = extractor.getExpectedType();
                if (type.isAssignableFrom(obj.getClass()))
                {
                    // the cast is safe due to the isAssignableFrom check above
                    extractor.addToContext(moduleContext, obj);
                }
                else
                {
                    throw new IllegalStateException(String.format(
                            "Web panel context object with key %s was of type %s (expected %s)",
                            extractor.getContextKey(), obj.getClass().getSimpleName(),
                            extractor.getExpectedType().getSimpleName()));
                }
            }
        }

        return moduleContext;
    }

    private static abstract class ParameterExtractor<T>
    {
        private final String contextKey;
        private final Class<T> expectedType;

        protected ParameterExtractor(String contextKey, Class<T> expectedType)
        {
            this.contextKey = contextKey;
            this.expectedType = expectedType;
        }

        public String getContextKey()
        {
            return contextKey;
        }

        Class<T> getExpectedType()
        {
            return expectedType;
        }

        abstract void addToContext(StashModuleContextParameters moduleContext, T value);
    }

    private Iterable<ParameterExtractor<?>> constructParameterExtractors()
    {
        return ImmutableList.of(
                new ParameterExtractor<Project>("project", Project.class)
                {
                    @Override
                    public void addToContext(final StashModuleContextParameters moduleContext, final Project value)
                    {
                        moduleContext.addProject(value);
                    }
                },
                new ParameterExtractor<Repository>("repository", Repository.class)
                {
                    @Override
                    public void addToContext(final StashModuleContextParameters moduleContext, final Repository value)
                    {
                        moduleContext.addRepository(value);
                    }
                },
                new ParameterExtractor<Principal>("userProfile", Principal.class)
                {
                    @Override
                    public void addToContext(final StashModuleContextParameters moduleContext, final Principal value)
                    {
                        UserProfile profile = userManager.getUserProfile(value.getName());
                        if (profile == null)
                        {
                            throw new IllegalStateException("Couldn't resolve profile for user in web panel context!");
                        }
                        moduleContext.addProfileUser(profile);
                    }
                },
                // it needs to be Map<Object, Object> because we cannot guarantee generic type expectations, we can receive any map and there is nothing we can do about it!
                new ParameterExtractor<Map>(WebFragmentModuleContextExtractor.MODULE_CONTEXT_KEY, Map.class)
                {
                    @Override
                    public void addToContext(final StashModuleContextParameters moduleContext, final Map value)
                    {
                        Set<Map.Entry> entries = value.entrySet();
                        for (Map.Entry entry : entries)
                        {
                            if (entry.getKey() instanceof String && entry.getValue() instanceof String)
                            {
                                moduleContext.put((String) entry.getKey(), (String) entry.getValue());
                            }
                        }
                    }
                }
        );
    }

}
