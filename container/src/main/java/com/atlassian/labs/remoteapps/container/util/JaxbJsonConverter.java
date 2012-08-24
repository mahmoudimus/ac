package com.atlassian.labs.remoteapps.container.util;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.AnnotationIntrospector;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.xc.JaxbAnnotationIntrospector;
import org.springframework.stereotype.Component;

import javax.xml.bind.JAXBException;
import java.io.IOException;

/**
 * Created with IntelliJ IDEA. User: mrdon Date: 8/23/12 Time: 5:20 PM To change this template use
 * File | Settings | File Templates.
 */
public class JaxbJsonConverter
{
    public static String pojoToJson(Object employee)
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getSerializationConfig().setAnnotationIntrospector(introspector);
        String jsonData = null;
        try
        {
            jsonData = mapper.writeValueAsString(employee);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
        return jsonData;
    }

    public static <T> T jsonToPojo(String jsonData, Class<T> castTo)
    {
        ObjectMapper mapper = new ObjectMapper();
        AnnotationIntrospector introspector = new JaxbAnnotationIntrospector();
        mapper.getDeserializationConfig().setAnnotationIntrospector(
                introspector);
        T employee = null;
        try
        {
            employee = mapper.readValue(jsonData, castTo);
        }
        catch (IOException e)
        {
            throw new IllegalArgumentException(e);
        }
        return employee;
    }
}
