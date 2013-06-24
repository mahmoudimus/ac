package com.atlassian.plugin.remotable.api.service.http.bigpipe;

/**
 * A content consumer interface for draining the content promised to BigPipe instances.  One instance may
 * exist per request, and is paired with a BigPipe counterpart.
 *
 * This represents a big pipe instance for a specific request.
 *
 * Yields content in the following format:
 * <pre>
 * {
 *     "items": [{"content": "...", "channelId": "..."}, ...],
 *     "pending": ["channel-id", ...]
 * }
 * </pre>
 *
 * @since 0.7
 */
// @ThreadSafe
public interface ConsumableBigPipe
{
    /**
     * @return The captured request id at the time that this instance was created; never null
     */
    String getRequestId();

    /**
     * Consume all completed content without blocking. When an envelope containing an empty pending channels
     * array is returned, there will be no more content published to this bigpipe instance.
     *
     * @return A JSON envelope containing an array of all content from promises that have been fulfilled at
     *         the time of the call as well as a list of channels for which content is still pending; never null
     */
    String consumeContent();

    /**
     * Consume all completed content, blocking until content becomes available if not yet completely drained.
     * When an envelope containing an empty pending channels array is returned, there will be no more content
     * published to this bigpipe instance.
     *
     * @return A JSON envelope containing an array of all content from promises that have been fulfilled at
     *         the time of the call as well as a list of channels for which content is still pending; never null
     */
    String waitForContent();
}
