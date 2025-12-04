package io.github.macfja.mpv.communication.handling;

import com.google.gson.JsonObject;

/**
 * An abstract/base implementation of a message handler for events
 *
 * @author MacFJA
 */
abstract public class AbstractEventHandler extends AbstractMessageHandler {
    @Override
    public boolean canHandle(JsonObject message) {
        if (!message.has("event")) {
            return false;
        }
        return canHandle(message.get("event").getAsString());
    }

    /**
     * Indicate if the message can by handled by testing its event name
     *
     * @param eventName The name of the event
     * @return {@code true} if the message can be handled
     */
    abstract public boolean canHandle(String eventName);
}
