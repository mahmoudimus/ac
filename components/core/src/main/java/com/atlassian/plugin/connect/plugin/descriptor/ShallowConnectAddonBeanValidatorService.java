package com.atlassian.plugin.connect.plugin.descriptor;

import com.atlassian.plugin.connect.api.util.ConnectContainerUtil;
import com.atlassian.plugin.connect.modules.beans.ShallowConnectAddonBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Looks up {@link ShallowConnectAddonBeanValidator} implementations from the plugin system.
 */
@Component
public class ShallowConnectAddonBeanValidatorService {

    private final ConnectContainerUtil connectContainerUtil;

    @Autowired
    public ShallowConnectAddonBeanValidatorService(ConnectContainerUtil connectContainerUtil) {
        this.connectContainerUtil = connectContainerUtil;
    }

    public void validate(final ShallowConnectAddonBean descriptor) throws InvalidDescriptorException {
        for (ShallowConnectAddonBeanValidator validator : connectContainerUtil.getBeansOfType(ShallowConnectAddonBeanValidator.class)) {
            validator.validate(descriptor);
        }
    }
}
