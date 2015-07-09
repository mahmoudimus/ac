package com.atlassian.plugin.connect.confluence.webhook;

import java.util.Map;

import com.atlassian.confluence.event.events.ConfluenceEvent;
import com.atlassian.confluence.event.events.content.attachment.AttachmentEvent;
import com.atlassian.confluence.pages.Attachment;
import com.atlassian.confluence.setup.settings.SettingsManager;
import com.atlassian.sal.api.user.UserManager;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

public class AttachmentEventMapper extends ConfluenceEventMapper
{
    public AttachmentEventMapper(UserManager userManager, SettingsManager confluenceSettingsManager)
    {
        super(userManager, confluenceSettingsManager);
    }

    @Override
    public boolean handles(ConfluenceEvent e)
    {
        return e instanceof AttachmentEvent;
    }

    @Override
    public Map<String, Object> toMap(ConfluenceEvent e)
    {
        AttachmentEvent event = (AttachmentEvent) e;

        ImmutableMap.Builder<String, Object> builder = ImmutableMap.builder();
        builder.putAll(super.toMap(e));
        builder.put("attachedTo", contentEntityObjectToMap(event.getContent()));

        Function<Attachment, Map<String, Object>> f = new Function<Attachment, Map<String, Object>>()
        {
            @Override
            public Map<String, Object> apply(Attachment attachment)
            {
                return attachmentToMap(attachment);
            }
        };
        builder.put("attachments", Lists.transform(event.getAttachments(),  f));
        return builder.build();
    }
}
