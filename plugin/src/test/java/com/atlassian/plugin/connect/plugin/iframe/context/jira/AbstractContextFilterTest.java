package com.atlassian.plugin.connect.plugin.iframe.context.jira;

import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.connect.plugin.iframe.context.AbstractModuleContextFilter;
import com.atlassian.plugin.connect.plugin.iframe.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.module.ConnectContextVariablesValidatorModuleDescriptor;
import com.atlassian.plugin.connect.spi.module.ContextParametersValidator;
import com.atlassian.plugin.connect.spi.module.PermissionCheck;
import com.atlassian.plugin.connect.spi.module.PermissionChecks;
import com.atlassian.plugin.module.ModuleFactory;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static com.atlassian.plugin.connect.test.util.UnitTestMatchers.predicateThatWillMatch;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AbstractContextFilterTest
{
    private static final ApplicationUser USER = mock(ApplicationUser.class);

    private final PluginAccessor pluginAccessor = mock(PluginAccessor.class);

    private final class ContextFilterImplementation extends AbstractModuleContextFilter<ApplicationUser>
    {
        private final List<PermissionCheck<ApplicationUser>> permissions;

        protected ContextFilterImplementation(List<PermissionCheck<ApplicationUser>> permissions)
        {
            super(pluginAccessor, ApplicationUser.class);
            this.permissions = permissions;
        }

        @Override
        protected ApplicationUser getCurrentUser()
        {
            return USER;
        }

        @Override
        protected Iterable<PermissionCheck<ApplicationUser>> getPermissionChecks()
        {
            return permissions;
        }
    }

    @Test
    public void permissionsFromImplementationAreRespectedWhenThereAreNoPlugins()
    {
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
    public void permissionsFromPluginsAreRespected()
    {
        assumingImplementation()
                .allows("a", "b")
                .andPluginsAllow("c")
                .whenUnfilteredContextIs("a", "b", "c", "d")
                .thenTheResultContextIs("a", "b", "c");
    }

    @Test
    public void allPermissionCheckMustAgreeOtherwiseForbid()
    {
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

    private TestSpecification assumingImplementation()
    {
        return new TestSpecification();
    }

    private final class TestSpecification
    {
        private List<String> allowed = Collections.emptyList();
        private List<String> allowedByPlugins = Collections.emptyList();
        private List<String> forbidden = Collections.emptyList();
        private List<String> forbiddenByPlugins = Collections.emptyList();
        private List<String> whatToFilter = Collections.emptyList();
        private List<String> expectedResult = Collections.emptyList();

        public TestSpecification allows(String... allowed)
        {
            this.allowed = Arrays.asList(allowed);
            return this;
        }

        public TestSpecification andForbids(String... forbidden)
        {
            this.forbidden = Arrays.asList(forbidden);
            return this;
        }

        public TestSpecification whenUnfilteredContextIs(String... whatToFilter)
        {
            this.whatToFilter = Arrays.asList(whatToFilter);
            return this;
        }

        public void thenTheResultContextIs(String... expected)
        {
            this.expectedResult = Arrays.asList(expected);
            assertSpecification();
        }

        public TestSpecification andPluginsAllow(final String... allowed)
        {
            allowedByPlugins = Arrays.asList(allowed);
            return this;
        }

        public TestSpecification andPluginsForbid(final String... forbidden)
        {
            forbiddenByPlugins = Arrays.asList(forbidden);
            return this;
        }

        private void assertSpecification()
        {
            ContextFilterImplementation filter = contextFilter(permissionChecks(allowed, forbidden));

            when(pluginAccessor.getModules(argThat(predicateThatWillMatch(new ConnectContextVariablesValidatorModuleDescriptor(mock(ModuleFactory.class)))))).thenReturn(
                    Collections.<ContextParametersValidator<?>>singletonList(new ContextParametersValidator<ApplicationUser>()
                    {
                        @Override
                        public Collection<PermissionCheck<ApplicationUser>> getPermissionChecks()
                        {
                            return permissionChecks(allowedByPlugins, forbiddenByPlugins);
                        }

                        @Override
                        public Class<ApplicationUser> getUserType()
                        {
                            return ApplicationUser.class;
                        }
                    })
            );

            ModuleContextParameters filtered = filter.filter(contextWithAllParameters(whatToFilter));

            assertThat(filtered.keySet(), equalTo((Set) ImmutableSet.copyOf(expectedResult)));
        }

        private List<PermissionCheck<ApplicationUser>> permissionChecks(Iterable<String> allowed, Iterable<String> forbidden)
        {
            return ImmutableList.copyOf(Iterables.concat(Iterables.transform(allowed, new Function<String, PermissionCheck<ApplicationUser>>()
            {
                @Override
                public PermissionCheck<ApplicationUser> apply(final String input)
                {
                    return allow(input);
                }
            }), Iterables.transform(forbidden, new Function<String, PermissionCheck<ApplicationUser>>()
            {
                @Override
                public PermissionCheck<ApplicationUser> apply(final String input)
                {
                    return forbid(input);
                }
            })));
        }
    }

    private ModuleContextParameters contextWithAllParameters(Iterable<String> names)
    {
        ModuleContextParameters result = new HashMapModuleContextParameters();
        for (String name : names)
        {
            result.put(name, name);
        }
        return result;
    }

    private ContextFilterImplementation contextFilter(List<PermissionCheck<ApplicationUser>> permissions)
    {
        return new ContextFilterImplementation(permissions);
    }

    private static PermissionCheck<ApplicationUser> allow(String name)
    {
        return PermissionChecks.alwaysAllowed(name);
    }

    private static PermissionCheck<ApplicationUser> forbid(final String name)
    {
        return new PermissionCheck<ApplicationUser>()
        {
            @Override
            public String getParameterName()
            {
                return name;
            }

            @Override
            public boolean hasPermission(final String value, final ApplicationUser applicationUser)
            {
                return false;
            }
        };
    }
}
