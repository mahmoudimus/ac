package com.atlassian.plugin.connect.plugin;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 */
@XmlRootElement
public class PermissionDeniedException extends RuntimeException {
    private final String pluginKey;

    public PermissionDeniedException(String message) {
        super(message);
        this.pluginKey = null;
    }

    public PermissionDeniedException(String pluginKey, String message) {
        super(message);
        this.pluginKey = pluginKey;
    }

    @XmlAttribute
    public String getPluginKey() {
        return pluginKey;
    }

    @Override
    @XmlElement
    public String getMessage() {
        return super.getMessage();
    }
}
