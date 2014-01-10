package com.atlassian.plugin.connect.plugin.capabilities.beans.nested;

/**
 * The type of the indexed value.
 *
 * @schemaTitle Entity Property Index Type
 * @since 1.0
 */
public enum EntityPropertyIndexType
{
    /**
     * The value indexed as a number. This allows the range ordering and searching on this field.
     */
    number,

    /**
     * The value tokenized before indexing. This would be used on a 'body' field, that contains the bulk of document's text.
     */
    text,

    /**
     * The value indexed but not tokenized.
     */
    string,

    /**
     * The value indexed as a date. This allows range date range searching and ordering on this field.
     */
    date
}
