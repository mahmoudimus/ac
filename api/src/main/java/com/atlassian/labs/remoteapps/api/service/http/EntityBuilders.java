package com.atlassian.labs.remoteapps.api.service.http;

import com.atlassian.labs.remoteapps.spi.http.DefaultFormBuilder;

import java.util.List;
import java.util.Map;

/**
 * Provides access to various entity builders
 */
public final class EntityBuilders
{
    private EntityBuilders() { }

    /**
     * Creates a new form entity builder for content-type "application/x-www-form-urlencoded".
     *
     * @return The new form builder
     */
    public static FormBuilder newForm()
    {
        return new DefaultFormBuilder();
    }

    /**
     * Creates a form entity builder for content-type "application/x-www-form-urlencoded"
     * from a parameter map.
     *
     * @param params The parameter map
     * @return The new form builder
     */
    FormBuilder newFormWithParams(Map<String, String> params)
    {
        FormBuilder form = newForm();
        for (Map.Entry<String, String> param : params.entrySet())
        {
            form.addParam(param.getKey(), param.getValue());
        }
        return form;
    }

    /**
     * Creates a form entity builder for content-type "application/x-www-form-urlencoded"
     * from a multi-valued parameter map.
     *
     * @param params The multi-valued parameter map
     * @return The new form builder
     */
    FormBuilder newFormWithListParams(Map<String, List<String>> params)
    {
        FormBuilder form = newForm();
        for (Map.Entry<String, List<String>> param : params.entrySet())
        {
            String key = param.getKey();
            List<String> values = param.getValue();
            if (values != null && values.size() > 0)
            {
                for (String value : values)
                {
                    form.addParam(key, value);
                }
            }
            else
            {
                form.addParam(key);
            }
        }
        return form;
    }
}
