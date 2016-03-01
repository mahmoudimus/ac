package com.atlassian.plugin.connect.modules.beans.nested;

import com.atlassian.json.schema.annotation.Required;
import com.atlassian.json.schema.annotation.StringSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;
import com.atlassian.plugin.connect.modules.beans.builder.ControlBeanBuilder;

/**
 * Defines a control which may appear in control extension points such as the dialog control bar
 *
 * <h4>Example</h4>
 *
 * @exampleJson {@link com.atlassian.plugin.connect.modules.beans.ConnectJsonExamples#CONTROL_EXAMPLE}
 * @schemaTitle Control
 * @since 1.0
 */
public class ControlBean extends RequiredKeyBean {
    /**
     * The control type, i.e. button, text field
     */
    @Required
    @StringSchemaAttributes(pattern = "^[a-zA-Z0-9-]+$")
    private String type;

    public ControlBean() {
        init();
    }

    public ControlBean(ControlBeanBuilder builder) {
        super(builder);
        init();
    }

    private void init() {
        if (null == type) {
            type = "";
        }
    }

    public String getType() {
        return type;
    }

    public static ControlBeanBuilder newControlBean() {
        return new ControlBeanBuilder();
    }

    public static ControlBeanBuilder newControlBean(ControlBean bean) {
        return new ControlBeanBuilder(bean);
    }

}
