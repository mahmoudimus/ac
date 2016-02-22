package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.spi.web.context.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.spring.scanner.annotation.component.JiraComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableList;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@JiraComponent
@ExportAsDevService
public class JiraWebFragmentModuleContextExtractor implements WebFragmentModuleContextExtractor {
    private final List<ParameterExtractor<?>> parameterExtractors;
    private final UserManager userManager;
    private final IssueManager issueManager;
    private final ProjectManager projectManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;

    @Inject
    public JiraWebFragmentModuleContextExtractor(UserManager userManager, IssueManager issueManager, ProjectManager projectManager, JiraAuthenticationContext jiraAuthenticationContext) {
        this.userManager = userManager;
        this.issueManager = issueManager;
        this.projectManager = projectManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.parameterExtractors = constructParameterExtractors();
    }

    @Override
    public ModuleContextParameters extractParameters(final Map<String, ? extends Object> webFragmentContext) {
        if (ModuleContextParameters.class.isAssignableFrom(webFragmentContext.getClass())) {
            return (ModuleContextParameters) webFragmentContext;
        }

        JiraModuleContextParameters moduleContext = new JiraModuleContextParametersImpl(webFragmentContext);

        for (ParameterExtractor extractor : parameterExtractors) {
            Object obj = webFragmentContext.get(extractor.getContextKey());
            if (obj != null) {
                Class<?> type = extractor.getExpectedType();
                if (type.isAssignableFrom(obj.getClass())) {
                    // the cast is safe due to the isAssignableFrom check above
                    extractor.addToContext(moduleContext, obj);
                } else {
                    throw new IllegalStateException(String.format(
                            "Web panel context object with key %s was of type %s (expected %s)",
                            extractor.getContextKey(), obj.getClass().getSimpleName(),
                            extractor.getExpectedType().getSimpleName()));
                }
            }
        }

        return moduleContext;
    }

    @Override
    public Map<String, Object> reverseExtraction(HttpServletRequest request, final Map<String, String> queryParams) {
        Optional<Issue> issue = mapParam(queryParams, "issue.id", id -> issueManager.getIssueObject(Long.valueOf(id)));
        Optional<Project> project = mapParam(queryParams, "project.id", id -> projectManager.getProjectObj(Long.valueOf(id)));

        Map<String, Object> context = new HashMap<>();

        context.put("user", jiraAuthenticationContext.getLoggedInUser());
        issue.ifPresent(value -> context.put("issue", value));
        project.ifPresent(value -> context.put("project", value));

        JiraHelper jiraHelper = new JiraHelper(request, project.orElse(null), context);

        context.put("helper", jiraHelper);

        return context;
    }

    private <T> Optional<T> mapParam(Map<String, String> queryParams, String paramName, Function<String, T> valueMapping) {
        return Optional.ofNullable(queryParams.get(paramName))
                .flatMap(valueMapping.andThen(Optional::ofNullable));
    }

    private interface ParameterExtractor<T> {
        String getContextKey();

        Class<T> getExpectedType();

        void addToContext(JiraModuleContextParameters moduleContext, T value);
    }

    private List<ParameterExtractor<?>> constructParameterExtractors() {
        return ImmutableList.of(
                new ParameterExtractor<Issue>() {
                    @Override
                    public String getContextKey() {
                        return "issue";
                    }

                    @Override
                    public Class<Issue> getExpectedType() {
                        return Issue.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Issue value) {
                        moduleContext.addIssue(value);
                    }
                },
                new ParameterExtractor<Project>() {
                    @Override
                    public String getContextKey() {
                        return "project";
                    }

                    @Override
                    public Class<Project> getExpectedType() {
                        return Project.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Project value) {
                        moduleContext.addProject(value);
                    }
                },
                new ParameterExtractor<Principal>() {
                    @Override
                    public String getContextKey() {
                        return "profileUser";
                    }

                    @Override
                    public Class<Principal> getExpectedType() {
                        return Principal.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Principal value) {
                        UserProfile profile = userManager.getUserProfile(value.getName());
                        if (profile == null) {
                            throw new IllegalStateException("Couldn't resolve profile for user in web panel context!");
                        }
                        moduleContext.addProfileUser(profile);
                    }
                },
                // it needs to be Map<Object, Object> because we cannot guarantee generic type expectations, we can receive any map and there is nothing we can do about it!
                new ParameterExtractor<Map<Object, Object>>() {
                    @Override
                    public String getContextKey() {
                        return WebFragmentModuleContextExtractor.MODULE_CONTEXT_KEY;
                    }

                    @Override
                    public Class getExpectedType() {
                        return Map.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Map<Object, Object> value) {
                        for (Map.Entry<Object, Object> entry : value.entrySet()) {
                            if (entry.getKey() instanceof String && entry.getValue() instanceof String) {
                                moduleContext.put((String) entry.getKey(), (String) entry.getValue());
                            }
                        }
                    }
                },
                new ParameterExtractor<Map<Object, Object>>() {
                    @Override
                    public String getContextKey() {
                        return "dashboardItem";
                    }

                    @Override
                    public Class getExpectedType() {
                        return Map.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Map<Object, Object> value) {
                        final Object id = value.get("id");
                        if (id instanceof String) {
                            moduleContext.put("dashboardItem.id", (String) id);
                        }
                        final Object key = value.get("moduleKey");
                        if (key instanceof String) {
                            moduleContext.put("dashboardItem.key", (String) key);
                        }
                    }
                },
                new ParameterExtractor<Map<Object, Object>>() {
                    @Override
                    public String getContextKey() {
                        return "dashboard";
                    }

                    @Override
                    public Class getExpectedType() {
                        return Map.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Map<Object, Object> value) {
                        final Object id = value.get("id");
                        if (id instanceof String) {
                            moduleContext.put("dashboard.id", (String) id);
                        }
                    }
                },
                new ParameterExtractor<Map<Object, Object>>() {
                    @Override
                    public String getContextKey() {
                        return "view";
                    }

                    @Override
                    public Class getExpectedType() {
                        return Map.class;
                    }

                    @Override
                    public void addToContext(final JiraModuleContextParameters moduleContext, final Map<Object, Object> value) {
                        final Object viewType = value.get("viewType");
                        if (viewType instanceof Map) {
                            final Object name = ((Map) viewType).get("name");
                            if (name instanceof String) {
                                moduleContext.put("dashboardItem.viewType", (String) name);
                            }
                        }
                    }
                }
        );
    }

}
