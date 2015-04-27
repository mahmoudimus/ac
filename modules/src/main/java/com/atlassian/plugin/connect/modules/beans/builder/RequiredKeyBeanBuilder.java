package com.atlassian.plugin.connect.modules.beans.builder;

import com.atlassian.plugin.connect.modules.beans.RequiredKeyBean;

/**
 * @since 1.0
 */
public class RequiredKeyBeanBuilder<T extends RequiredKeyBeanBuilder, B extends RequiredKeyBean> extends NamedBeanBuilder<T, B>
{
    private String key;
    private boolean useKeyAsIs = false;

    public RequiredKeyBeanBuilder()
    {
    }

    public RequiredKeyBeanBuilder(RequiredKeyBean defaultBean)
    {
        super(defaultBean);

        this.key = defaultBean.getRawKey();
    }

    public T withKey(String key)
    {
        this.key = key;
        return (T) this;
    }

    /**
     * Use only if absolutely needed. This will lock the key to the specified value, and no transformations
     * of the key will be performed.
     *
     * @param useKeyAsIs the value to set for the flag
     * @return the builder
     */
    public T useKeyAsIs(boolean useKeyAsIs)
    {
        this.useKeyAsIs = useKeyAsIs;
        return (T) this;
    }

    public boolean useKeyAsIs()
    {
        return useKeyAsIs;
    }

    @Override
    public B build()
    {
        return (B) new RequiredKeyBean(this);
    }
}
