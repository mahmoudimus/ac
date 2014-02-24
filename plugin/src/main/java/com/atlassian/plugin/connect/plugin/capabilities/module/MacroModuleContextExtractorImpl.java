package com.atlassian.plugin.connect.plugin.capabilities.module;

import com.atlassian.confluence.content.render.xhtml.ConversionContext;
import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.Spaced;
import com.atlassian.plugin.connect.plugin.iframe.context.HashMapModuleContextParameters;
import com.atlassian.plugin.connect.plugin.iframe.context.ModuleContextParameters;
import com.atlassian.plugin.spring.scanner.annotation.component.ConfluenceComponent;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 *
 */
@ConfluenceComponent
public class MacroModuleContextExtractorImpl implements MacroModuleContextExtractor
{
    /**
     * Specifies how much of the body to allow in the GET request to a remote app.  If the body parameter is
     * included in a URL, this values specifies how its truncated.
     */
    private final static int MAX_BODY_LENGTH = 128;

    private final UserManager userManager;

    @Autowired
    public MacroModuleContextExtractorImpl(UserManager userManager)
    {
        this.userManager = userManager;
    }

    @Override
    public ModuleContextParameters extractParameters(final String storageFormatBody, final ConversionContext conversionContext, final Map<String, String> parameters)
    {
        ModuleContextParameters moduleContext = new HashMapModuleContextParameters();

        moduleContext.putAll(parameters);

        moduleContext.put("macro.body", StringUtils.left(storageFormatBody, MAX_BODY_LENGTH));
        moduleContext.put("macro.truncated", Boolean.toString(storageFormatBody.length() > MAX_BODY_LENGTH));
        moduleContext.put("macro.hash", DigestUtils.md5Hex(storageFormatBody));
        moduleContext.put("output.type", conversionContext.getOutputType());

        ContentEntityObject entity = conversionContext.getEntity();

        String pageId = "";
        String pageTitle = "";
        String pageType = "";
        String spaceId = "";
        String spaceKey = "";
        String versionId = "";
        String userId = "";
        String userKey = "";

        if (entity != null)
        {
            pageId = entity.getIdAsString();
            pageTitle = StringUtils.defaultString(entity.getTitle());
            pageType = entity.getType();
            if (entity instanceof Spaced)
            {
                Space space = ((Spaced) entity).getSpace();
                spaceKey = space.getKey();
                spaceId = Long.toString(space.getId());
            }
            versionId = Integer.toString(entity.getVersion());
        }

        UserProfile currentUser = userManager.getRemoteUser();
        if (currentUser != null)
        {
            userId = currentUser.getUsername();
            userKey = currentUser.getUserKey().getStringValue();
        }

        moduleContext.put("page.id", pageId);
        moduleContext.put("page.title", pageTitle);
        moduleContext.put("page.type", pageType);
        moduleContext.put("page.version", versionId);

        moduleContext.put("space.id", spaceId);
        moduleContext.put("space.key", spaceKey);

        moduleContext.put("user.id", userId);
        moduleContext.put("user.key", userKey);

        return moduleContext;
    }

}
