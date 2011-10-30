package com.atlassian.labs.remoteapps.installer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class RegistrationRequest
{
    private final String token;
    private final ConsumerXml consumer;

    public RegistrationRequest(ConsumerXml consumer, String token)
    {
        this.consumer = consumer;
        this.token = token;
    }

    public ConsumerXml getConsumer()
    {
        return consumer;
    }

    public String getToken()
    {
        return token;
    }
}
