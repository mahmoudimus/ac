package com.atlassian.plugin.connect.bitbucket;

import com.atlassian.json.schema.annotation.ObjectSchemaAttributes;
import com.atlassian.plugin.connect.modules.beans.BaseModuleBean;

/**
 * A container class used for generation of JSON schema for Bitbucket modules.
 */
@SuppressWarnings("UnusedDeclaration")
@ObjectSchemaAttributes(additionalProperties = false)
public class BitbucketModuleList extends BaseModuleBean {

    private BitbucketModuleList()
    {
    }
}
