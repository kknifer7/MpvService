package io.github.macfja.mpv.communication.handling;

import com.google.gson.JsonObject;

/**
 * Interface of a message handler that will work with MPV (JSON) message.
 *
 * @author MacFJA
 */
public interface MessageHandlerInterface {
    /**
     * Indicate if the {@code message} can by handled
     *
     * @param message The message to handle
     * @return {@code true} if the message can be handled
     */
    boolean canHandle(JsonObject message);

    /**
     * Handle the message.
     *
     * @param message The message to handle
     */
    void handle(JsonObject message);
}
