package com.atlassian.plugin.connect.plugin.threeleggedauth.servlet;

import com.atlassian.plugin.connect.modules.beans.ConnectAddonBean;
import com.atlassian.plugin.connect.modules.beans.nested.ScopeName;
import com.atlassian.plugin.connect.plugin.capabilities.JsonConnectAddOnIdentifierService;
import com.atlassian.plugin.connect.plugin.installer.ConnectAddonBeanFactory;
import com.atlassian.plugin.connect.plugin.registry.ConnectAddonRegistry;
import com.atlassian.plugin.connect.plugin.threeleggedauth.NoAgentScopeException;
import com.atlassian.plugin.connect.plugin.threeleggedauth.ThreeLeggedAuthService;
import com.atlassian.sal.api.message.I18nResolver;
import com.atlassian.sal.api.user.UserKey;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.templaterenderer.TemplateRenderer;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkNotNull;

@Component
public class GrantThreeLeggedAuthServlet extends HttpServlet
{
    private final JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService;
    private final ConnectAddonRegistry connectAddonRegistry;
    private final ConnectAddonBeanFactory connectAddonBeanFactory;
    private final UserManager userManager;
    private final I18nResolver i18nResolver;
    private final ThreeLeggedAuthService threeLeggedAuthService;
    private final TemplateRenderer templateRenderer;

    private static final String AGENCY_DESC_WITH_ADD_ON_I18N_KEY = "connect.scope.agent.description.personal.withkey";
    private static final String EXTANT_I18N_KEY = "connect.prefix.extant";
    private static final String GRANTED_I18N_KEY = "connect.prefix.granted";
    private static final Pattern PATH_PATTERN = Pattern.compile("^(/[^/]+){3}/([^/]+)"); // .../ac/tla/grant/{add-on-key}

    @Autowired
    public GrantThreeLeggedAuthServlet(JsonConnectAddOnIdentifierService jsonConnectAddOnIdentifierService,
                                       ConnectAddonRegistry connectAddonRegistry,
                                       ConnectAddonBeanFactory connectAddonBeanFactory,
                                       UserManager userManager,
                                       I18nResolver i18nResolver,
                                       ThreeLeggedAuthService threeLeggedAuthService,
                                       TemplateRenderer templateRenderer)
    {
        this.jsonConnectAddOnIdentifierService = checkNotNull(jsonConnectAddOnIdentifierService);
        this.connectAddonRegistry = checkNotNull(connectAddonRegistry);
        this.connectAddonBeanFactory = checkNotNull(connectAddonBeanFactory);
        this.userManager = checkNotNull(userManager);
        this.i18nResolver = checkNotNull(i18nResolver);
        this.threeLeggedAuthService = checkNotNull(threeLeggedAuthService);
        this.templateRenderer = checkNotNull(templateRenderer);
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws IOException
    {
        UserKey userKey = userManager.getRemoteUserKey(req);

        // accessible only to logged-in users so that we don't leak installed-add-on-key knowledge to anonymous randoms
        if (null == userKey)
        {
            resp.sendRedirect(getLoginUri(getUri(req)).toASCIIString());
        }
        else
        {
            Matcher matcher = PATH_PATTERN.matcher(req.getPathInfo());

            if (matcher.find())
            {
                String addOnKey = matcher.group(2);

                if (jsonConnectAddOnIdentifierService.isConnectAddOn(addOnKey))
                {
                    ConnectAddonBean addOnBean = connectAddonBeanFactory.fromJsonSkipValidation(connectAddonRegistry.getDescriptor(addOnKey));
                    String bodyContent;

                    if (addOnBean.getScopes().contains(ScopeName.AGENT))
                    {
                        String actionPrefixI18nKey = grantUserAgencyToAddOn(userKey, addOnBean) ? GRANTED_I18N_KEY : EXTANT_I18N_KEY;
                        bodyContent = String.format("%s %s", i18nResolver.getText(actionPrefixI18nKey), i18nResolver.getText(AGENCY_DESC_WITH_ADD_ON_I18N_KEY, addOnKey));
                    }
                    else
                    {
                        bodyContent = String.format("The Connect add-on \"%s\" cannot request the ability to act on your behalf as it does not request the \"%s\" scope.", addOnBean.getName(), ScopeName.AGENT);
                    }

                    templateRenderer.render("velocity/three-legged-auth.vm", ImmutableMap.<String, Object>of("bodyContent", bodyContent), resp.getWriter());
                }
                else
                {
                    resp.sendError(HttpServletResponse.SC_NOT_FOUND, String.format("'%s' is not an installed Connect add-on key", addOnKey));
                }
            }
            else
            {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        }
    }

    private boolean grantUserAgencyToAddOn(UserKey userKey, ConnectAddonBean addOnBean)
    {
        try
        {
            return threeLeggedAuthService.grant(userKey, addOnBean);
        }
        catch (NoAgentScopeException e)
        {
            throw new RuntimeException(e);
        }
    }

    private URI getLoginUri(URI destinationUri) throws UnsupportedEncodingException
    {
        // TODO: surely there has to be some service that we can call to get the login URL
        final String destinationUriString = destinationUri.toASCIIString();
        return URI.create(destinationUriString.replaceAll("/plugins/.*", "/login.jsp?permissionViolation=true&os_destination=" + URLEncoder.encode(destinationUriString, "UTF-8")));
    }

    private URI getUri(HttpServletRequest request)
    {
        StringBuffer builder = request.getRequestURL();

        if (request.getQueryString() != null)
        {
            builder.append("?");
            builder.append(request.getQueryString());
        }

        return URI.create(builder.toString());
    }
}
