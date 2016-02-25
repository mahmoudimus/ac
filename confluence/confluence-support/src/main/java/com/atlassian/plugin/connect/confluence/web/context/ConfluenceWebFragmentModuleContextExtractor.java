package com.atlassian.plugin.connect.confluence.web.context;

import com.atlassian.confluence.core.ConfluenceActionSupport;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.pages.AbstractPage;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.pages.PageManager;
import com.atlassian.confluence.pages.actions.AbstractPageAwareAction;
import com.atlassian.confluence.plugin.descriptor.web.WebInterfaceContext;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.SpaceManager;
import com.atlassian.confluence.themes.Theme;
import com.atlassian.confluence.themes.ThemeContext;
import com.atlassian.confluence.themes.ThemeManager;
import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.plugin.connect.api.web.context.ModuleContextParameters;
import com.atlassian.plugin.connect.spi.web.context.WebFragmentModuleContextExtractor;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsDevService;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

/**
 *
 */
@ConfluenceComponent
@ExportAsDevService
public class ConfluenceWebFragmentModuleContextExtractor implements WebFragmentModuleContextExtractor {
    private final UserManager userManager;
    private final PageManager pageManager;
    private final SpaceManager spaceManager;
    private final ThemeManager themeManager;

    @Inject
    public ConfluenceWebFragmentModuleContextExtractor(final UserManager userManager, final PageManager pageManager, final SpaceManager spaceManager, final ThemeManager themeManager) {
        this.userManager = userManager;
        this.pageManager = pageManager;
        this.spaceManager = spaceManager;
        this.themeManager = themeManager;
    }

    @Override
    public ModuleContextParameters extractParameters(final Map<String, ? extends Object> webFragmentContext) {
        if (ModuleContextParameters.class.isAssignableFrom(webFragmentContext.getClass())) {
            return (ModuleContextParameters) webFragmentContext;
        }

        ConfluenceModuleContextParameters moduleContext = new ConfluenceModuleContextParametersImpl((Map) webFragmentContext);

        {
            @SuppressWarnings("unchecked") // it is what it is
                    WebInterfaceContext webInterfaceContext = (WebInterfaceContext) webFragmentContext.get("webInterfaceContext");
            if (webInterfaceContext != null) {
                moduleContext.addPage(webInterfaceContext.getPage());
                moduleContext.addContent(webInterfaceContext.getPage());
                moduleContext.addSpace(webInterfaceContext.getSpace());
            }
        }

        final Object action = webFragmentContext.get("action");

        if (action instanceof ConfluenceActionSupport) {
            WebInterfaceContext webInterfaceContext = ((ConfluenceActionSupport) action).getWebInterfaceContext();

            if (webInterfaceContext != null) {
                moduleContext.addPage(webInterfaceContext.getPage());
                moduleContext.addContent(webInterfaceContext.getPage());
                moduleContext.addSpace(webInterfaceContext.getSpace());
            }
        }

        Space space = (Space) webFragmentContext.get("space");
        if (space != null) {
            moduleContext.addSpace(space);
        }

        AbstractPage page = (AbstractPage) webFragmentContext.get("page");
        if (page != null) {
            moduleContext.addContent(page);
            moduleContext.addPage(page);
        }

        if (action instanceof AbstractPageAwareAction) {
            AbstractPageAwareAction pageAwareAction = (AbstractPageAwareAction) action;
            if (!moduleContext.containsKey(ConfluenceModuleContextFilter.PAGE_ID)) {
                moduleContext.addContent(pageAwareAction.getPage());
                moduleContext.addPage(pageAwareAction.getPage());
            }
        }

        Object content = webFragmentContext.get("content");
        if (content != null && content instanceof ContentEntityObject) {
            moduleContext.addContent((ContentEntityObject) content);
        }

        ConfluenceUser profileUser = (ConfluenceUser) webFragmentContext.get("targetUser");
        if (profileUser != null) {
            UserProfile profile = userManager.getUserProfile(profileUser.getKey());
            moduleContext.addProfileUser(profile);
        }

        ModuleContextParameters nestedContext = (ModuleContextParameters) webFragmentContext.get(MODULE_CONTEXT_KEY);
        if (nestedContext != null) {
            moduleContext.putAll(nestedContext);
        }

        return moduleContext;
    }

    @Override
    public Map<String, Object> reverseExtraction(final HttpServletRequest request, final Map<String, String> queryParams) {
        Map<String, Object> context = new HashMap<>();

        ConfluenceUser user = AuthenticatedUserThreadLocal.get();
        Optional<Page> page = mapParam(queryParams, "page.id", id -> pageManager.getPage(Long.valueOf(id)));
        Optional<Space> space = mapParam(queryParams, "space.id", id -> spaceManager.getSpace(Long.valueOf(id)));

        context.put(WebInterfaceContext.CONTEXT_KEY_USER, user);
        context.put(WebInterfaceContext.CONTEXT_KEY_TARGET_USER, user);
        page.ifPresent(value -> context.put("page", value));
        space.ifPresent(value -> {
            context.put("space", value);
            Theme spaceTheme = themeManager.getSpaceTheme(value.getKey());
            Theme globalTheme = themeManager.getGlobalTheme();
            request.setAttribute(ThemeContext.ATTRIBUTE_KEY, new ThemeContext(value, spaceTheme, globalTheme));
        });

        return context;
    }

    private <T> Optional<T> mapParam(Map<String, String> queryParams, String paramName, Function<String, T> valueMapping) {
        return Optional.ofNullable(queryParams.get(paramName))
                .flatMap(valueMapping.andThen(Optional::ofNullable));
    }
}
