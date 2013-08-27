package com.atlassian.plugin.connect.plugin.rest.email;

import com.atlassian.mail.Email;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class RemoteEmail
{
    @JsonProperty
    private String to;
    @JsonProperty
    private String subject;

    // optional fields
    @JsonProperty
    private String from;
    @JsonProperty
    private String fromName;
    @JsonProperty
    private String cc;
    @JsonProperty
    private String bcc;
    @JsonProperty
    private String replyTo;
    @JsonProperty
    private String inReplyTo;
    @JsonProperty
    private String bodyAsText;
    @JsonProperty
    private String bodyAsHtml;
    @JsonProperty
    private String mimeType;
    @JsonProperty
    private String encoding;
    @JsonProperty
    private String messageId;
    @JsonProperty
    private Map<String, String> headers = newHashMap();

    // todo: multipart not supported

    @JsonCreator
    public RemoteEmail(
            @JsonProperty("id") final String to,
            @JsonProperty("subject") final String subject,
            @JsonProperty("from") final String from,
            @JsonProperty("fromName") final String fromName,
            @JsonProperty("cc") final String cc,
            @JsonProperty("bcc") final String bcc,
            @JsonProperty("replyTo") final String replyTo,
            @JsonProperty("inReplyTo") final String inReplyTo,
            @JsonProperty("bodyAsText") final String bodyAsText,
            @JsonProperty("bodyAsHtml") final String bodyAsHtml,
            @JsonProperty("mimeType") final String mimeType,
            @JsonProperty("encoding") final String encoding,
            @JsonProperty("messageId") final String messageId,
            @JsonProperty("headers") final Map<String, String> headers)
    {
        this.to = to;
        this.subject = subject;
        this.from = from;
        this.fromName = fromName;
        this.cc = cc;
        this.bcc = bcc;
        this.replyTo = replyTo;
        this.inReplyTo = inReplyTo;
        this.bodyAsText = bodyAsText;
        this.bodyAsHtml = bodyAsHtml;
        this.mimeType = mimeType;
        this.encoding = encoding;
        this.messageId = messageId;
        if(headers != null)
        {
            this.headers.putAll(headers);
        }
    }

    public RemoteEmail(Email email)
    {
        setTo(email.getTo());
        setBcc(email.getBcc());
        setCc(email.getCc());
        setEncoding(email.getEncoding());
        setFrom(email.getFrom());
        setFromName(email.getFromName());
        setHeaders(email.getHeaders());
        setInReplyTo(email.getInReplyTo());
        setMessageId(email.getMessageId());
        setMimeType(email.getMimeType());
        setReplyTo(email.getReplyTo());
        setEncoding(email.getSubject());
    }

    public Email toEmail()
    {
        Email email = new Email(to)
                .setBcc(bcc)
                .setCc(cc)
                .setEncoding(encoding)
                .setFrom(from)
                .setFromName(fromName)
                .setInReplyTo(inReplyTo)
                .setMimeType(mimeType)
                .setReplyTo(replyTo)
                .setSubject(subject);

        email.setMessageId(messageId);

        for (Map.Entry<String, String> entry : headers.entrySet())
        {
            email.addHeader(entry.getKey(), entry.getValue());
        }

        return email;
    }

    public String getTo()
    {
        return to;
    }

    public void setTo(String to)
    {
        this.to = to;
    }

    public String getSubject()
    {
        return subject;
    }

    public void setSubject(String subject)
    {
        this.subject = subject;
    }

    public String getFrom()
    {
        return from;
    }

    public void setFrom(String from)
    {
        this.from = from;
    }

    public String getFromName()
    {
        return fromName;
    }

    public void setFromName(String fromName)
    {
        this.fromName = fromName;
    }

    public String getCc()
    {
        return cc;
    }

    public void setCc(String cc)
    {
        this.cc = cc;
    }

    public String getBcc()
    {
        return bcc;
    }

    public void setBcc(String bcc)
    {
        this.bcc = bcc;
    }

    public String getReplyTo()
    {
        return replyTo;
    }

    public void setReplyTo(String replyTo)
    {
        this.replyTo = replyTo;
    }

    public String getInReplyTo()
    {
        return inReplyTo;
    }

    public void setInReplyTo(String inReplyTo)
    {
        this.inReplyTo = inReplyTo;
    }

    public String getMimeType()
    {
        return mimeType;
    }

    public void setMimeType(String mimeType)
    {
        this.mimeType = mimeType;
    }

    public String getEncoding()
    {
        return encoding;
    }

    public void setEncoding(String encoding)
    {
        this.encoding = encoding;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    public Map<String, String> getHeaders()
    {
        return headers;
    }

    public void setHeaders(Map<String, String> headers)
    {
        this.headers = headers;
    }

    public String getBodyAsText()
    {
        return bodyAsText;
    }

    public void setBodyAsText(String bodyAsText)
    {
        this.bodyAsText = bodyAsText;
    }

    public String getBodyAsHtml()
    {
        return bodyAsHtml;
    }

    public void setBodyAsHtml(String bodyAsHtml)
    {
        this.bodyAsHtml = bodyAsHtml;
    }
}
