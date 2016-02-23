package com.atlassian.plugin.connect.jira.web.context;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.ModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.spi.module.ContextParametersExtractor;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.connect.spi.module.PermissionChecks;
import com.atlassian.plugin.connect.spi.web.context.AbstractModuleContextFilter;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterResolverModuleDescriptor;
import com.atlassian.plugin.connect.spi.web.context.ConnectContextParameterResolverModuleDescriptor.ConnectContextParametersResolver;
import com.atlassian.plugin.connect.spi.web.context.HashMapModuleContextParameters;
import com.atlassian.plugin.module.ModuleFactory;
import com.atlassian.plugin.predicate.ModuleDescriptorPredicate;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.Iterables.concat;
import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AbstractContextFilterTest {
    private static final ApplicationUser USER = mock(ApplicationUser.class);

    private final PluginAccessor pluginAccessor = mock(PluginAccessor.class);

    private final class ContextFilterImplementation extends AbstractModuleContextFilter<ApplicationUser> {
        private final List<PermissionCheck<ApplicationUser>> permissions;

        protected ContextFilterImplementation(List<PermissionCheck<ApplicationUser>> permissions) {
            super(pluginAccessor, ApplicationUser.class);
            this.permissions = permissions;
        }

        @Override
        protected ApplicationUser getCurrentUser() {
            return USER;
        }

        @Override
        protected Iterable<PermissionCheck<ApplicationUser>> getPermissionChecks() {
            return permissions;
        }
    }

    @Test
    public void permissionsFromImplementationAreRespectedWhenThereAreNoPlugins() {
        assumingImplementation()
                .allows("a", "b")
                .whenUnfilteredContextIs("a", "b", "c", "d")
                .thenTheResultContextIs("a", "b");

        assumingImplementation()
                .allows("a", "b")
                .andForbids("c")
                .whenUnfilteredContextIs("a", "b", "c", "d")
                .thenTheResultContextIs("a", "b");

        assumingImplementation()
                .allows("a", "b")
                .andForbids("b")
                .whenUnfilteredContextIs("a", "b", "c", "d")
                .thenTheResultContextIs("a");
    }

    @Test
    public void permissionsFromPluginsAreRespected() {
        assumingImplementation()
                .allows("a", "b")
                .andPluginsAllow("c")
                .whenUnfilteredContextIs("a", "b", "c", "d")
                .thenTheResultContextIs("a", "b", "c");
    }

    @Test
    public void allPermissionCheckMustAgreeOtherwiseForbid() {
        assumingImplementation()
                .allows("a", "b")
                .andForbids("c")
                .andPluginsAllow("c")
                .whenUnfilteredContextIs("a", "b", "c", "d")
                .thenTheResultContextIs("a", "b");

        assumingImplementation()
                .allows("a", "b")
                .andPluginsAllow("c")
                .andPluginsForbid("b")
                .whenUnfilteredContextIs("a", "b", "c", "d")
                .thenTheResultContextIs("a", "c");

        assumingImplementation()
                .allows("a", "b")
                .andPluginsAllow("a")
                .andPluginsForbid("a")
                .whenUnfilteredContextIs("a", "b", "c", "d")
                .thenTheResultContextIs("b");
    }

    private TestSpecification assumingImplementation() {
        return new TestSpecification();
    }

    private final class TestSpecification {
        private List<String> allowed = Collections.emptyList();
        private List<String> allowedByPlugins = Collections.emptyList();
        private List<String> forbidden = Collections.emptyList();
        private List<String> forbiddenByPlugins = Collections.emptyList();
        private List<String> whatToFilter = Collections.emptyList();
        private List<String> expectedResult = Collections.emptyList();

        public TestSpecification allows(String... allowed) {
            this.allowed = Arrays.asList(allowed);
            return this;
        }

        public TestSpecification andForbids(String... forbidden) {
            this.forbidden = Arrays.asList(forbidden);
            return this;
        }

        public TestSpecification whenUnfilteredContextIs(String... whatToFilter) {
            this.whatToFilter = Arrays.asList(whatToFilter);
            return this;
        }

        public void thenTheResultContextIs(String... expected) {
            this.expectedResult = Arrays.asList(expected);
            assertSpecification();
        }

        public TestSpecification andPluginsAllow(final String... allowed) {
            allowedByPlugins = Arrays.asList(allowed);
            return this;
        }

        public TestSpecification andPluginsForbid(final String... forbidden) {
            forbiddenByPlugins = Arrays.asList(forbidden);
            return this;
        }

        private void assertSpecification() {
            ContextFilterImplementation filter = contextFilter(permissionChecks(allowed, forbidden));

            when(pluginAccessor.getModules(argThat(predicateThatWillMatch(new ConnectContextParameterResolverModuleDescriptor(mock(ModuleFactory.class)))))).thenReturn(
                    Collections.singletonList(new ConnectContextParametersResolver(
                            Collections.<ContextParametersExtractor>emptyList(),
                            Collections.<ContextParametersValidator>singletonList(
                                    new ContextParametersValidator<ApplicationUser>() {
                                        @Override
                                        public Collection<PermissionCheck<ApplicationUser>> getPermissionChecks() {
                                            return permissionChecks(allowedByPlugins, forbiddenByPlugins);
                                        }

                                        @Override
                                        public Class<ApplicationUser> getUserType() {
                                            return ApplicationUser.class;
                                        }
                                    }
                            )
                    ))
            );

            ModuleContextParameters filtered = filter.filter(contextWithAllParameters(whatToFilter));

            assertThat(filtered.keySet(), equalTo((Set) ImmutableSet.copyOf(expectedResult)));
        }

        private List<PermissionCheck<ApplicationUser>> permissionChecks(Iterable<String> allowed, Iterable<String> forbidden) {
            return ImmutableList.copyOf(concat(
                    transform(allowed, AbstractContextFilterTest::allow),
                    transform(forbidden, AbstractContextFilterTest::forbid)
            ));
        }
    }

    private ModuleContextParameters contextWithAllParameters(Iterable<String> names) {
        ModuleContextParameters result = new HashMapModuleContextParameters(Collections.emptyMap());
        for (String name : names) {
            result.put(name, name);
        }
        return result;
    }

    private ContextFilterImplementation contextFilter(List<PermissionCheck<ApplicationUser>> permissions) {
        return new ContextFilterImplementation(permissions);
    }

    private static PermissionCheck<ApplicationUser> allow(String name) {
        return PermissionChecks.alwaysAllowed(name);
    }

    private static PermissionCheck<ApplicationUser> forbid(final String name) {
        return new PermissionCheck<ApplicationUser>() {
            @Override
            public String getParameterName() {
                return name;
            }

            @Override
            public boolean hasPermission(final String value, final ApplicationUser applicationUser) {
                return false;
            }
        };
    }

    // TODO Copied from com.atlassian.plugin.connect.util.matcher.UnitTestMatcher due to circular dependencies. Find better place to utils for tests. API?
    private static <T> Matcher<ModuleDescriptorPredicate<T>> predicateThatWillMatch(final ModuleDescriptor<T> exampleDescriptor) {
        return new TypeSafeMatcher<ModuleDescriptorPredicate<T>>() {
            @Override
            protected boolean matchesSafely(final ModuleDescriptorPredicate<T> item) {
                return item.matches(exampleDescriptor);
            }

            @Override
            public void describeTo(final Description description) {
                description.appendText("predicate that matches a " + exampleDescriptor.getClass().toString());
            }
        };
    }
}
