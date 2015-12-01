package com.atlassian.plugin.connect.plugin.rest.data;

import java.io.IOException;

import com.atlassian.fugue.Either;

import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;

/**
 * Created by rmassaioli on 26/11/2015.
 */
public class RestAddonPropertyValueSerializer extends JsonSerializer<Either<String, JsonNode>>
{
    @Override
    public void serialize(Either<String, JsonNode> value, JsonGenerator jgen, SerializerProvider provider)
        throws IOException, JsonProcessingException
    {
        if(value.isLeft()) {
            jgen.writeString(value.left().get());
        } else {
            jgen.writeTree(value.right().get());
        }
    }
}
