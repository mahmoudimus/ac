package com.atlassian.plugin.connect.plugin.rest.email;

import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.mail.Email;

import static com.google.common.collect.Maps.newHashMap;

/**
 */
@XmlRootElement
public class RemoteEmail
{
    @XmlAttribute
    private String to;
    @XmlAttribute
    private String subject;

    // optional fields
    @XmlAttribute
    private String from;
    @XmlAttribute
    private String fromName;
    @XmlAttribute
    private String cc;
    @XmlAttribute
    private String bcc;
    @XmlAttribute
    private String replyTo;
    @XmlAttribute
    private String inReplyTo;
    @XmlElement
    private String bodyAsText;
    @XmlElement
    private String bodyAsHtml;
    @XmlAttribute
    private String mimeType;
    @XmlAttribute
    private String encoding;
    @XmlAttribute
    private String messageId;
    @XmlElement
    private Map<String,String> headers = newHashMap();

    // todo: multipart not supported

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

        for (Map.Entry<String,String> entry : headers.entrySet())
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
