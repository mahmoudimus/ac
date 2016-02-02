package it.com.atlassian.plugin.connect.confluence.web.condition;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import com.atlassian.confluence.mail.notification.NotificationManager;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.themes.ThemeManager;
import com.atlassian.confluence.userstatus.FavouriteManager;
import com.atlassian.confluence.web.context.HttpContext;
import com.atlassian.fugue.Pair;
import com.atlassian.plugin.connect.confluence.web.ConfluenceConditionClassResolver;
import com.atlassian.plugin.connect.plugin.web.context.condition.InlineCondition;
import com.atlassian.plugin.connect.plugin.web.context.condition.InlineConditionResolver;
import com.atlassian.plugin.connect.spi.web.condition.ConnectConditionClassResolver;
import com.atlassian.plugin.connect.spi.web.context.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.connect.testsupport.util.auth.TestAuthenticator;
import com.atlassian.plugins.osgi.test.Application;
import com.atlassian.plugins.osgi.test.AtlassianPluginsTestRunner;
import com.atlassian.user.User;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import it.com.atlassian.plugin.connect.confluence.util.ConfluenceTestUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.atlassian.fugue.Pair.pair;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toSet;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Application ("confluence")
@RunWith (AtlassianPluginsTestRunner.class)
public class InlineConditionResolverConfluenceWiredTest
{
    private final static List<Pair<Pair<String, Optional<Boolean>>, Map<String, String>>> CONDITIONS = ImmutableList.<Pair<Pair<String, Optional<Boolean>>, Map<String, String>>>builder()
            .add(pair(pair("active_theme", Optional.of(true)), ImmutableMap.of("themes", "")))
            .add(pair(pair("can_edit_space_styles", Optional.of(true)), emptyMap()))
            .add(pair(pair("can_signup", Optional.of(true)), emptyMap()))
            .add(pair(pair("content_has_any_permissions_set", Optional.of(false)), emptyMap()))
            .add(pair(pair("create_content", Optional.of(true)), ImmutableMap.of("content", "page")))
            .add(pair(pair("email_address_public", Optional.of(true)), emptyMap()))
            .add(pair(pair("favourite_page", Optional.of(true)), emptyMap()))
            .add(pair(pair("favourite_space", Optional.of(true)), emptyMap()))
            .add(pair(pair("following_target_user", Optional.empty()), emptyMap()))
            .add(pair(pair("has_attachment", Optional.of(false)), emptyMap()))
            .add(pair(pair("has_blog_post", Optional.of(false)), emptyMap()))
            .add(pair(pair("has_page", Optional.of(true)), emptyMap()))
            .add(pair(pair("has_space", Optional.of(true)), emptyMap()))
            .add(pair(pair("has_template", Optional.of(false)), emptyMap()))
            .add(pair(pair("latest_version", Optional.of(true)), emptyMap()))
            .add(pair(pair("not_personal_space", Optional.of(true)), emptyMap()))
            .add(pair(pair("printable_version", Optional.of(false)), emptyMap()))
            .add(pair(pair("showing_page_attachments", Optional.of(true)), emptyMap()))
            .add(pair(pair("space_function_permission", Optional.of(true)), ImmutableMap.of("permission", "VIEWSPACE")))
            .add(pair(pair("space_sidebar", Optional.of(true)), emptyMap()))
            .add(pair(pair("target_user_has_personal_blog", Optional.of(false)), emptyMap()))
            .add(pair(pair("target_user_has_personal_space", Optional.of(false)), emptyMap()))
            .add(pair(pair("threaded_comments", Optional.of(true)), emptyMap()))
            .add(pair(pair("tiny_url_supported", Optional.empty()), emptyMap()))
            .add(pair(pair("user_can_create_personal_space", Optional.of(true)), emptyMap()))
            .add(pair(pair("user_can_use_confluence", Optional.of(true)), emptyMap()))
            .add(pair(pair("user_favouriting_target_user_personal_space", Optional.of(false)), emptyMap()))
            .add(pair(pair("user_has_personal_blog", Optional.of(false)), emptyMap()))
            .add(pair(pair("user_has_personal_space", Optional.of(false)), emptyMap()))
            .add(pair(pair("user_logged_in_editable", Optional.of(true)), emptyMap()))
            .add(pair(pair("user_watching_page", Optional.of(true)), emptyMap()))
            .add(pair(pair("user_watching_space", Optional.of(false)), emptyMap()))
            .add(pair(pair("user_watching_space_for_content_type", Optional.of(false)), emptyMap()))
            .add(pair(pair("viewing_content", Optional.empty()), emptyMap()))
            .add(pair(pair("viewing_own_profile", Optional.empty()), emptyMap()))
            .add(pair(pair("user_is_confluence_administrator", Optional.of(true)), emptyMap()))
            .build();

    private final InlineConditionResolver inlineConditionResolver;
    private final WebFragmentModuleContextExtractor extractor;
    private final TestAuthenticator testAuthenticator;
    private final ConfluenceTestUtil confluenceTestUtil;
    private final ThemeManager themeManager;
    private final HttpContext httpContext;
    private final SettingsManager settingsManager;
    private final FavouriteManager favouriteManager;
    private final NotificationManager notificationManager;

    private final ConnectConditionClassResolver conditionClassResolver = new ConfluenceConditionClassResolver();

    private Page page;
    private Space space;

    public InlineConditionResolverConfluenceWiredTest(final InlineConditionResolver inlineConditionResolver, final WebFragmentModuleContextExtractor extractor, final TestAuthenticator testAuthenticator, final ConfluenceTestUtil confluenceTestUtil, final ThemeManager themeManager, final HttpContext httpContext, final SettingsManager settingsManager, final FavouriteManager favouriteManager, final NotificationManager notificationManager)
    {
        this.inlineConditionResolver = inlineConditionResolver;
        this.extractor = extractor;
        this.testAuthenticator = testAuthenticator;
        this.confluenceTestUtil = confluenceTestUtil;
        this.themeManager = themeManager;
        this.httpContext = httpContext;
        this.settingsManager = settingsManager;
        this.favouriteManager = favouriteManager;
        this.notificationManager = notificationManager;
    }

    @Before
    public void setUp() throws Exception
    {
        final User user = confluenceTestUtil.getAdmin();
        testAuthenticator.authenticateUser(user.getName());

        page = confluenceTestUtil.createPage();
        space = page.getSpace();
        themeManager.setSpaceTheme(space.getKey(), themeManager.getGlobalThemeKey());
        settingsManager.getGlobalSettings().setDenyPublicSignup(false);
        favouriteManager.addPageToFavourites(user, page);
        favouriteManager.addSpaceToFavourites(user, space);
        notificationManager.addContentNotification(user, page);
    }

    @Test
    public void testAllConditionsAreTested() throws Exception
    {
        Set<String> allConditions = conditionClassResolver.getEntries().stream().map(new Function<ConnectConditionClassResolver.Entry, String>()
        {
            @Override
            public String apply(final ConnectConditionClassResolver.Entry entry)
            {
                return entry.getConditionName();
            }
        }).collect(toSet());
        Set<String> testedConditions = CONDITIONS.stream().map(new Function<Pair<Pair<String, Optional<Boolean>>, Map<String, String>>, String>()
        {
            @Override
            public String apply(final Pair<Pair<String, Optional<Boolean>>, Map<String, String>> pair)
            {
                return pair.left().left();
            }
        }).collect(toSet());
        Sets.SetView<String> untestedConditions = Sets.difference(allConditions, testedConditions);
        assertTrue("All conditions should be tested, untested conditions: " + untestedConditions, untestedConditions.isEmpty());
    }

    @Test
    public void testConditions() throws Exception
    {
        Map<String, Object> reversedContext = extractor.reverseExtraction(httpContext.getRequest(), redirectContext());

        for (Pair<Pair<String, Optional<Boolean>>, Map<String, String>> conditionAndParams : CONDITIONS)
        {
            String name = conditionAndParams.left().left();
            Optional<Boolean> expectedValue = conditionAndParams.left().right();
            Map<String, String> params = conditionAndParams.right();
            InlineCondition condition = new InlineCondition(name, params);
            Optional<Boolean> resolved = inlineConditionResolver.resolve(condition, reversedContext);
            assertEquals(pair(name, expectedValue), pair(name, resolved));
        }
    }

    private Map<String, String> redirectContext()
    {
        return ImmutableMap.of(
                "page.id", "" + page.getId(),
                "space.id", "" + space.getId());
    }
}

