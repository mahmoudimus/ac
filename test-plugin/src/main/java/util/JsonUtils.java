package util;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import static java.util.Arrays.asList;

/**
 */
public class JsonUtils
{
    public static JSONArray toArray(Object... values)
    {
        JSONArray arr = new JSONArray();
        arr.addAll(asList(values));
        return arr;
    }

    public static JSONObject parseObject(String value)
    {
        try
        {
            return (JSONObject) new JSONParser().parse(value);
        }
        catch (ParseException e)
        {
            throw new RuntimeException(e);
        }
    }

}
