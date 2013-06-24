package com.atlassian.plugin.remotable.api.service.http.bigpipe;

/**
 * A delayed content provider interface for the big pipe subsystem.  Big pipe content
 * can be promised on the channels made available by this interface.  One instance may
 * exist per request, and is paired with a ConsumableBigPipe counterpart.
 *
 * @since 0.7
 */
// @ThreadSafe
public interface BigPipe
{
    /**
     * The reserved HTML channel id.
     */
    String HTML_CHANNEL_ID = "html";

    /**
     * Returns a handle to the HTML channel.
     *
     * @return The HTML channel; never null
     */
    HtmlChannel getHtmlChannel();

    /**
     * Returns a handle to a data channel, by id.
     *
     * @param channelId The id of the channel
     * @return The specified data channel; never null
     * @throws NullPointerException If the channelId is null
     * @throws IllegalArgumentException If the channelId is {@link BigPipe#HTML_CHANNEL_ID}
     */
    DataChannel getDataChannel(String channelId)
        throws NullPointerException, IllegalArgumentException;
}
