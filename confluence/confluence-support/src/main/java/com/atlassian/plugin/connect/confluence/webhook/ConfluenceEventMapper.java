package com.atlassian.plugin.connect.confluence.webhook;

import com.atlassian.confluence.core.ContentEntityObject;
import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.labels.Label;
import com.atlassian.confluence.labels.Labelable;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.pages.Comment;
import com.atlassian.confluence.pages.Page;
import com.atlassian.confluence.plugins.createcontent.impl.ContentBlueprint;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.confluence.spaces.Space;
import com.atlassian.confluence.spaces.Spaced;
import com.atlassian.confluence.user.ConfluenceUser;
import com.atlassian.sal.api.user.UserManager;
import com.atlassian.sal.api.user.UserProfile;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.defaultIfBlank;

import java.util.Map;

public class ConfluenceEventMapper {
    private final UserManager userManager;
    private final SettingsManager confluenceSettingsManager;

    public ConfluenceEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager) {
        this.userManager = userManager;
        this.confluenceSettingsManager = confluenceSettingsManager;
    }

    /**
     * This transforms the event {@code e} into a simple {@link java.util.Map} that is easily transformed into a valid JSON object.
     * To make sure this works properly the objects put into the map (as values) should be of either types:
     * <ul>
     *     <li>{@link Byte}</li>
     *     <li>{@link Character}</li>
     *     <li>{@link Short}</li>
     *     <li>{@link Integer}</li>
     *     <li>{@link Long}</li>
     *     <li>{@link Boolean}</li>
     *     <li>{@link Double}</li>
     *     <li>{@link String}</li>
     *     <li>a {@link java.util.Collection} of type following those same rules</li>
     *     <li>an array of type following those same rules</li>
     *     <li>a {@link java.util.Map} with {@link String} as keys and values of type following those same rules</li>
     * </ul>
     *
     * The {@link Object#toString()} will be used on standard java objects (in packages java. and javax.).
     *
     * @param event the event
     * @return a map of event properties
     */
    public Map<String, Object> toMap(ConfluenceEvent event) {
        UserProfile user = userManager.getRemoteUser();
        String username = user == null ? "" : user.getUsername();
        String userKey = user == null ? "" : user.getUserKey().getStringValue();

        return ImmutableMap.<String, Object>of(
                "timestamp", event.getTimestamp(),
                "user", username, // deprecated
                "username", username,
                "userKey", userKey
        );
    }

    public boolean handles(ConfluenceEvent e) {
        return true; // can handle any kind of ConfluenceEvent, but not in any particularly meaningful way :-)
    }

    protected Map<String, Object> labelableToMap(Labelable labelable) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("labels", Lists.transform(labelable.getLabels(), label -> labelToMap(label, true)));

        if (labelable instanceof ContentEntityObject) {
            builder.putAll(contentEntityObjectToMap((ContentEntityObject) labelable));
        } else if (labelable instanceof Attachment) {
            builder.putAll(attachmentToMap((Attachment) labelable));
        }

        return builder.build();
    }

    protected Map<String, Object> labelToMap(Label label) {
        return labelToMap(label, false);
    }

    private String getUserUsername(ConfluenceUser user) {
        return user == null ? "" : user.getName();
    }

    private String getUserUserKey(ConfluenceUser user) {
        return user == null ? "" : user.getKey().getStringValue();
    }

    protected Map<String, Object> labelToMap(Label label, boolean nameOnly) {
        if (nameOnly) {
            return ImmutableMap.<String, Object>of("name", label.getName());
        }

        ConfluenceUser ownerUser = label.getOwnerUser();

        return ImmutableMap.<String, Object>of(
                "name", label.getName(),
                // TODO: this should be an owner 'user' object
                "owner", getUserUsername(ownerUser),
                "ownerKey", getUserUserKey(ownerUser),
                "title", label.getDisplayTitle(),
                "self", getFullUrl(label.getUrlPath())
                // TODO: Consider adding additional label data, including the label's namespace, owner and view URL
        );
    }

    protected Map<String, Object> userProfileToMap(UserProfile userProfile) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("userKey", userProfile.getUserKey().getStringValue());
        builder.put("username", userProfile.getUsername());
        builder.put("email", userProfile.getEmail());
        builder.put("fullName", userProfile.getFullName());

        return builder.build();
    }

    protected Map<String, Object> contentEntityObjectToMap(ContentEntityObject ceo, boolean idOnly) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("id", ceo.getId());
        if (!idOnly) {
            if (!isBlank(ceo.getTitle())) {
                builder.put("title", ceo.getTitle());
            }

            builder.put("creatorName", getUserUsername(ceo.getCreator()));
            builder.put("creatorKey", getUserUserKey(ceo.getCreator()));
            builder.put("lastModifierName", getUserUsername(ceo.getLastModifier()));
            builder.put("lastModifierKey", getUserUserKey(ceo.getLastModifier()));
            builder.put("creationDate", ceo.getCreationDate() != null ? ceo.getCreationDate().getTime() : "");
            builder.put("modificationDate", ceo.getLastModificationDate() != null ? ceo.getLastModificationDate().getTime() : "");
            builder.put("version", ceo.getVersion());
            builder.put("self", getFullUrl(ceo.getUrlPath()));
            if (ceo instanceof Spaced) {
                // TODO: Consider adding additional information about the space, eg. title, logo & description.
                Space space = ((Spaced) ceo).getSpace();
                if (space != null) {
                    builder.put("spaceKey", space.getKey());
                }
            }
        }
        return builder.build();
    }

    protected Map<String, Object> contentBlueprintToMap(ContentBlueprint blueprint) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("id", blueprint.getId());
        builder.put("indexKey", blueprint.getIndexKey());
        builder.put("spaceKey", defaultIfBlank(blueprint.getSpaceKey(), ""));
        builder.put("i18nNameKey", blueprint.getI18nNameKey());
        builder.put("indexTitleI18nKey", defaultIfBlank(blueprint.getIndexTitleI18nKey(), ""));
        builder.put("moduleCompleteKey", blueprint.getModuleCompleteKey());
        builder.put("createResult", defaultIfBlank(blueprint.getCreateResult(), ""));
        builder.put("howToUseTemplate", defaultIfBlank(blueprint.getHowToUseTemplate(), ""));

        return builder.build();
    }

    protected String getFullUrl(String relativeUrl) {
        return confluenceSettingsManager.getGlobalSettings().getBaseUrl() + relativeUrl;
    }

    protected Map<String, Object> attachmentToMap(Attachment attachment) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.put("fileName", attachment.getFileName());
        builder.put("version", attachment.getVersion());
        builder.put("comment", defaultIfBlank(attachment.getComment(), ""));
        builder.put("fileSize", attachment.getFileSize());
        builder.put("id", attachment.getId());
        builder.put("creatorName", getUserUsername(attachment.getCreator()));
        builder.put("creatorKey", getUserUserKey(attachment.getCreator()));
        builder.put("creationDate", attachment.getCreationDate().getTime());
        builder.put("lastModifierName", getUserUsername(attachment.getLastModifier()));
        builder.put("lastModifierKey", getUserUserKey(attachment.getLastModifier()));
        builder.put("modificationDate", attachment.getLastModificationDate().getTime());
        builder.put("self", getFullUrl(attachment.getDownloadPath()));

        return builder.build();
    }

    protected Map<String, Object> spaceToMap(Space space) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();

        builder.put("key", space.getKey());
        builder.put("title", space.getDisplayTitle());
        if (space.getDescription() != null) {
            builder.put("description", space.getDescription().getBodyAsString());
        }
        builder.put("isPersonalSpace", space.isPersonal());
        builder.put("self", getFullUrl(space.getUrlPath()));
        Page homePage = space.getHomePage();
        if (homePage != null) {
            builder.put("homePage", contentEntityObjectToMap(homePage, true));
        }

        builder.put("creatorName", getUserUsername(space.getCreator()));
        builder.put("creatorKey", getUserUserKey(space.getCreator()));
        builder.put("creationDate", space.getCreationDate().getTime());
        builder.put("lastModifierName", getUserUsername(space.getLastModifier()));
        builder.put("lastModifierKey", getUserUserKey(space.getLastModifier()));
        builder.put("modificationDate", space.getLastModificationDate().getTime());

        return builder.build();
    }

    protected Map<String, Object> contentEntityObjectToMap(ContentEntityObject ceo) {
        return contentEntityObjectToMap(ceo, false);
    }

    protected Map<String, Object> commentToMap(Comment comment) {
        return commentToMap(comment, false);
    }

    protected Map<String, Object> commentToMap(Comment comment, boolean idOnly) {
        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(contentEntityObjectToMap(comment, idOnly));
        if (!idOnly) {
            builder.put("parent", contentEntityObjectToMap(comment.getOwner()));
            if (comment.getParent() != null) {
                builder.put("inReplyTo", commentToMap(comment.getParent(), true));
            }
        }
        return builder.build();
    }
}
