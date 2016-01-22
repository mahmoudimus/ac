package it.confluence.item;

import java.util.List;
import java.util.Map;

import com.atlassian.confluence.security.SpacePermission;
import com.google.common.collect.ImmutableMap;
import it.confluence.ConfluenceWebDriverTestBase;
import org.junit.Ignore;

import static com.google.common.collect.Lists.newArrayList;

@Ignore
public abstract class AbstractConfluenceConditionsTest extends ConfluenceWebDriverTestBase
{
    protected static final List<String> CONDITION_NAMES = newArrayList(
            "can_edit_space_styles",
            "create_content",
            "email_address_public",
            "has_page",
            "has_space",
            "latest_version",
            "not_personal_space",
            "showing_page_attachments",
            "space_function_permission",
            "space_sidebar",
            "threaded_comments",
            "tiny_url_supported",
            "user_can_create_personal_space",
            "user_logged_in_editable",
            "viewing_content"
    );

    protected static final Map<String, Map<String, String>> CONDITION_PARAMETERS = ImmutableMap.of(
            "create_content", ImmutableMap.of("content", "Page"),
            "space_function_permission", ImmutableMap.of("permission", SpacePermission.VIEWSPACE_PERMISSION)
    );
}
