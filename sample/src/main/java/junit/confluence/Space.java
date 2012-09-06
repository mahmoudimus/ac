package junit.confluence;

import com.atlassian.xmlrpc.ServiceBean;
import com.atlassian.xmlrpc.ServiceBeanField;

/**
 *
 */
@ServiceBean
public class Space
{
    private String key;
    private String name;

    public String getName()
    {
        return name;
    }

    @ServiceBeanField("name")
    public void setName(String name)
    {
        this.name = name;
    }

    public String getKey()
    {
        return key;
    }

    @ServiceBeanField("key")
    public void setKey(String key)
    {
        this.key = key;
    }
}
