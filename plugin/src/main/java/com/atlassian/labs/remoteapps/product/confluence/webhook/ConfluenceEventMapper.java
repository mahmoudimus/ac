package com.atlassian.labs.remoteapps.product.confluence.webhook;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.Spaced;
import com.atlassian.sal.api.user.UserManager;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang.StringUtils;

import java.util.Map;

public class ConfluenceEventMapper implements EventMapper
{
    private final UserManager userManager;
    private final SettingsManager confluenceSettingsManager;

    public ConfluenceEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        this.userManager = userManager;
        this.confluenceSettingsManager = confluenceSettingsManager;
    }

    public Map<String, Object> toMap(ConfluenceEvent event)
    {
        final String username = userManager.getRemoteUsername();
        return ImmutableMap.<String, Object>of(
                "timestamp", event.getTimestamp(),
                "user", StringUtils.isBlank(username) ? "" : username
        );
    }

    @Override
    public boolean handles(ConfluenceEvent e)
    {
        return true; // can handle any kind of ConfluenceEvent, but not in any particularly meaningful way :-)
    }

    protected Map<String, Object> labelToMap(Label label)
    {
        return ImmutableMap.<String, Object>of(
                "name", label.getName(),
                "owner", label.getOwner(),
                "title", label.getDisplayTitle(),
                "self", getFullUrl(label.getUrlPath())
                // TODO: Consider adding additional label data, including the label's namespace, owner and view URL
        );
    }

    protected Map<String, Object> contentEntityObjectToMap(ContentEntityObject ceo, boolean idOnly)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("id", ceo.getId());
        if (!idOnly)
        {
            if (!StringUtils.isBlank(ceo.getTitle()))
                builder.put("title", ceo.getTitle());

            builder.put("creatorName", StringUtils.isBlank(ceo.getCreatorName()) ? "" : ceo.getCreatorName());
            builder.put("lastModifierName", StringUtils.isBlank(ceo.getLastModifierName()) ? "" : ceo.getLastModifierName());
            builder.put("creationDate", ceo.getCreationDate().getTime());
            builder.put("modificationDate", ceo.getLastModificationDate().getTime());
            builder.put("version", ceo.getVersion());
            builder.put("self", getFullUrl(ceo.getUrlPath()));
            if (ceo instanceof Spaced)
            {
                // TODO: Consider adding additional information about the space, eg. title, logo & description.
                Space space = ((Spaced)ceo).getSpace();
                if (space != null)
                {
                    builder.put("spaceKey", space.getKey());
                }
            }
        }
        return builder.build();
    }

    protected String getFullUrl(String relativeUrl)
    {
        return confluenceSettingsManager.getGlobalSettings().getBaseUrl() + relativeUrl;
    }

    protected Map<String, Object> attachmentToMap(Attachment attachment)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("fileName", attachment.getFileName());
        builder.put("version", attachment.getVersion());
        builder.put("comment", StringUtils.isBlank(attachment.getComment()) ? "" : attachment.getComment());
        builder.put("fileSize", attachment.getFileSize());
        builder.put("id", attachment.getId());
        builder.put("creatorName", StringUtils.isBlank(attachment.getCreatorName()) ? "" : attachment.getCreatorName());
        builder.put("creationDate", attachment.getCreationDate().getTime());
        builder.put("lastModifierName", StringUtils.isBlank(attachment.getLastModifierName()) ? "" : attachment.getLastModifierName());
        builder.put("modificationDate", attachment.getLastModificationDate().getTime());
        builder.put("self", getFullUrl(attachment.getDownloadPath()));

        return builder.build();
    }

    protected Map<String, Object> spaceToMap(Space space)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("key", space.getKey());
        builder.put("title", space.getDisplayTitle());
        builder.put("description", space.getDescription().getBodyAsString());
        builder.put("isPersonalSpace", space.isPersonal());
        builder.put("self", getFullUrl(space.getUrlPath()));
        builder.put("homePage", contentEntityObjectToMap(space.getHomePage(), true));
        builder.put("creatorName", StringUtils.isBlank(space.getCreatorName()) ? "" : space.getCreatorName());
        builder.put("creationDate", space.getCreationDate().getTime());
        builder.put("lastModifierName", StringUtils.isBlank(space.getLastModifierName()) ? "" : space.getLastModifierName());
        builder.put("modificationDate", space.getLastModificationDate().getTime());

        return builder.build();
    }

    protected Map<String, Object> contentEntityObjectToMap(ContentEntityObject ceo)
    {
        return contentEntityObjectToMap(ceo, false);
    }

    protected Map<String, Object> commentToMap(Comment comment)
    {
        return commentToMap(comment, false);
    }

    protected Map<String, Object> commentToMap(Comment comment, boolean idOnly)
    {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(contentEntityObjectToMap(comment, idOnly));
        if (!idOnly)
        {
            builder.put("parent", contentEntityObjectToMap(comment.getOwner()));
            if (comment.getParent() != null)
                builder.put("inReplyTo", commentToMap(comment.getParent(), true));
        }
        return builder.build();
    }
}
