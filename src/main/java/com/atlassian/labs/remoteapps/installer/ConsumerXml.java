package com.atlassian.labs.remoteapps.installer;

import com.atlassian.oauth.Consumer;
import com.atlassian.oauth.util.RSAKeys;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;

/**
 *
 */
@XmlRootElement(name = "consumer")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class ConsumerXml
{
    private String key;
    private String name;
    private String description;
    private String publicKey;
    private URI callback;

    public ConsumerXml()
    {
    }

    public ConsumerXml(Consumer consumer)
    {
        this.key = consumer.getKey();
        this.name = consumer.getName();
        this.description = consumer.getDescription();
        this.callback = consumer.getCallback();
        this.publicKey = RSAKeys.toPemEncoding(consumer.getPublicKey());
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getPublicKey()
    {
        return publicKey;
    }

    public void setPublicKey(String publicKey)
    {
        this.publicKey = publicKey;
    }

    public URI getCallback()
    {
        return callback;
    }

    public void setCallback(URI callback)
    {
        this.callback = callback;
    }
}
