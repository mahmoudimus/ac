package com.atlassian.plugin.connect.plugin.product;

import java.util.Map;

public interface EventMapper<T>
{
    public boolean handles(T e);

    /**
     * This transforms the event {@code e} into a simple {@link java.util.Map} that is easily transformed into a valid JSON object.
     * To make sure this works properly the objects put into the map (as values) should be of either types:
     * <li>
     *     <ul>{@link Byte}</ul>
     *     <ul>{@link Character}</ul>
     *     <ul>{@link Short}</ul>
     *     <ul>{@link Integer}</ul>
     *     <ul>{@link Long}</ul>
     *     <ul>{@link Boolean}</ul>
     *     <ul>{@link Double}</ul>
     *     <ul>{@link String}</ul>
     *     <ul>a {@link java.util.Collection} of type following those same rules</ul>
     *     <ul>an array of type following those same rules</ul>
     *     <ul>a {@link java.util.Map} with {@link String} as keys and values of type following those same rules</ul>
     * </li>
     *
     * The {@link Object#toString()} will be used on standard java objects (in packages java. and javax.).
     */
    public Map<String, Object> toMap(T e);
}
