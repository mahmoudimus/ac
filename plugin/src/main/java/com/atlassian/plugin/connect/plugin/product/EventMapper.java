package com.atlassian.plugin.connect.plugin.product;

import java.util.Map;

public interface EventMapper<T>
{
    public boolean handles(T e);

    /**
     * This transforms the event {@code e} into a simple {@link java.util.Map} that is easily transformed into a valid JSON object.
     * To make sure this works properly the objects put into the map (as values) should be of either types:
     * <ul>
     *     <li>{@link Byte}</li>
     *     <li>{@link Character}</li>
     *     <li>{@link Short}</li>
     *     <li>{@link Integer}</li>
     *     <li>{@link Long}</li>
     *     <li>{@link Boolean}</li>
     *     <li>{@link Double}</li>
     *     <li>{@link String}</li>
     *     <li>a {@link java.util.Collection} of type following those same rules</li>
     *     <li>an array of type following those same rules</li>
     *     <li>a {@link java.util.Map} with {@link String} as keys and values of type following those same rules</li>
     * </ul>
     *
     * The {@link Object#toString()} will be used on standard java objects (in packages java. and javax.).
     *
     * @param e the event
     * @return a map of event properties
     */
    public Map<String, Object> toMap(T e);
}
