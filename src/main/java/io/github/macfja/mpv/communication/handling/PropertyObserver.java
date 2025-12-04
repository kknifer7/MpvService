package io.github.macfja.mpv.communication.handling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.github.kknifer7.util.GsonUtil;

/**
 * Abstract PropertyObserver class.
 * Observer of a property changes.
 *
 * @author MacFJA
 */
public abstract class PropertyObserver extends AbstractMessageHandler implements MessageHandlerInterface {
    /**
     * The property observer change group.
     */
    private final Integer id;
    /**
     * The name of the property that is observed
     */
    private final String propertyName;

    /**
     * Create a new property observer for a property name and a group
     *
     * @param propertyName The name of the property to observe
     * @param id           The id of the group to be associated with
     */
    public PropertyObserver(String propertyName, Integer id) {
        this.propertyName = propertyName;
        this.id = id;
    }

    /**
     * Create a new property observer (group defaulted to 1)
     *
     * @param propertyName The name of the property to observe
     */
    public PropertyObserver(String propertyName) {
        this(propertyName, propertyName.hashCode());
    }

    /**
     * Get the property change group id.
     * All property change associated in the group can be manage at the same time (mainly, un-observe at the same time)
     *
     * @return the group id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Get the name of the observed property
     *
     * @return the property name
     */
    public String getPropertyName() {
        return propertyName;
    }

    /**
     * The method that will be call when the property changed
     *
     * @param propertyName The name of the property
     * @param value        The new property value
     * @param id           The id of the associated group
     */
    abstract public void changed(String propertyName, Object value, Integer id);

    @Override
    public boolean canHandle(JsonObject message) {
        return message.has("event")
                && message.get("event").getAsString().equals("property-change")
                && message.get("name").getAsString().equals(propertyName)
                && message.has("data")
                && message.has("id")
                && message.get("id").getAsInt() == id;

    }

    @Override
    public Runnable doHandle(final JsonObject message) {
        return () -> changed(
                message.get("name").getAsString(),
                jsonElementToObject(message.get("data")),
                message.get("id").getAsInt()
        );
    }

    private Object jsonElementToObject(JsonElement jsonElement) {
        if (jsonElement == null || jsonElement.isJsonNull()) {

            return null;
        }
        if (jsonElement.isJsonPrimitive()) {
            JsonPrimitive jsonPrimitive = jsonElement.getAsJsonPrimitive();
            if (jsonPrimitive.isBoolean()) {

                return jsonPrimitive.getAsBoolean();
            } else if (jsonPrimitive.isNumber()) {

                return jsonPrimitive.getAsNumber();
            } else if (jsonPrimitive.isString()) {

                return jsonPrimitive.getAsString();
            }
        }

        return jsonElement;
    }

    /**
     * Build a compatible property event
     *
     * @param propertyName The name of the property
     * @param newValue     The new value of the property
     * @param id           The group id of the event
     * @return The event json
     */
    public static JsonObject buildPropertyChangeEvent(String propertyName, Object newValue, int id) {
        JsonObject jsonObject = new JsonObject();

        jsonObject.addProperty("event", "property-change");
        jsonObject.addProperty("name", propertyName);
        jsonObject.add("data", GsonUtil.toJsonTree(newValue));
        jsonObject.addProperty("id", id);

        return jsonObject;
    }
}
