package io.github.macfja.mpv.communication.handling;

import com.google.gson.JsonObject;
import io.github.kknifer7.util.GsonUtil;

/**
 * An abstract/base implementation of a message handler to handle result of a command
 *
 * @author MacFJA
 */
public abstract class ResponseHandler extends AbstractMessageHandler {
    @Override
    public boolean canHandle(JsonObject message) {
        if (!message.has("request_id")) {
            return false;
        }
        return canHandle(message.get("request_id").getAsInt());
    }

    /**
     * Indicate if the provided requestId is handled by this handler
     *
     * @param requestId The is of the request associated with the response
     * @return {@code true} if the handler can work with this request id
     */
    abstract public boolean canHandle(Integer requestId);

    /**
     * Test if the result of a command is a success or not
     *
     * @param rawResult The textual raw result to check
     * @return {@code true} if the result is a success
     */
    public static boolean isResultSuccess(String rawResult) {
        return isResultSuccess(GsonUtil.fromJson(rawResult, JsonObject.class));
    }

    /**
     * Test if the result of a command is a success or not
     *
     * @param result The JSONObject to check
     * @return {@code true} if the result is a success
     */
    public static boolean isResultSuccess(JsonObject result) {
        return result.has("error") && result.get("error").getAsString().equals("success");
    }
}
