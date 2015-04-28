package com.atlassian.plugin.connect.jira.iframe;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextParameters;
import com.atlassian.plugin.connect.jira.iframe.context.JiraModuleContextParametersImpl;
import com.atlassian.plugin.connect.plugin.iframe.webpanel.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import java.security.Principal;
import java.util.Map;

@JiraComponent
public class JiraWebFragmentModuleContextExtractor implements WebFragmentModuleContextExtractor
{
    private final Iterable<ParameterExtractor<?>> parameterExtractors;
    private final UserManager userManager;

    @Inject
    public JiraWebFragmentModuleContextExtractor(UserManager userManager)
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

        JiraModuleContextParameters moduleContext = new JiraModuleContextParametersImpl();

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

    private static interface ParameterExtractor<T>
    {
        String getContextKey();

        Class<T> getExpectedType();

        void addToContext(JiraModuleContextParameters moduleContext, T value);
    }

    private Iterable<ParameterExtractor<?>> constructParameterExtractors()
    {
        return ImmutableList.of(
                new ParameterExtractor<Issue>()
                {
                    @Override
                    public String getContextKey()
                    {
                        return "issue";
                    }

                    @Override
                    public Class<Issue> getExpectedType()
                    {
                        return Issue.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Issue value)
                    {
                        moduleContext.addIssue(value);
                    }
                },
                new ParameterExtractor<Project>()
                {
                    @Override
                    public String getContextKey()
                    {
                        return "project";
                    }

                    @Override
                    public Class<Project> getExpectedType()
                    {
                        return Project.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Project value)
                    {
                        moduleContext.addProject(value);
                    }
                },
                new ParameterExtractor<Principal>()
                {
                    @Override
                    public String getContextKey()
                    {
                        return "profileUser";
                    }

                    @Override
                    public Class<Principal> getExpectedType()
                    {
                        return Principal.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Principal value)
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
                new ParameterExtractor<Map<Object, Object>>()
                {
                    @Override
                    public String getContextKey()
                    {
                        return WebFragmentModuleContextExtractor.MODULE_CONTEXT_KEY;
                    }

                    @Override
                    public Class getExpectedType()
                    {
                        return Map.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Map<Object, Object> value)
                    {
                        for (Map.Entry<Object, Object> entry : value.entrySet())
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
