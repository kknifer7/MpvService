package io.github.macfja.mpv;

import com.google.gson.JsonObject;
import io.github.kknifer7.util.PropertyUtil;
import io.github.macfja.mpv.communication.handling.ResponseHandler;
import io.github.macfja.mpv.communication.handling.NamedEventHandler;
import io.github.macfja.mpv.communication.handling.PropertyObserver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ServiceTest {
    static MpvService mpvService;

    @BeforeAll
    static public void init() {
        String mpvPath = null;

        try {
            mpvPath = PropertyUtil.load("mpv.properties").getProperty("path");
        } catch (IOException e) {
            e.printStackTrace();
            Assertions.fail();
        }

        mpvService = new Service(mpvPath);
    }
    @AfterAll
    static public void finish() {
        if (mpvService == null) {

            return;
        }
        try {
            mpvService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mpvService = null;
    }

    @Test
    public void testGetProperty()
    {
        try {
            String result = mpvService.getProperty("mpv-version");
            Assertions.assertTrue(result != null && result.contains("mpv"));
            Assertions.assertTrue(ResponseHandler.isResultSuccess(result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPropertyChange() throws InterruptedException {
        final StringBuilder called = new StringBuilder();
        try {
            mpvService.registerPropertyChange(new PropertyObserver("metadata", 1) {
                @Override
                public void changed(String propertyName, Object value, Integer id) {
                    Assertions.assertEquals("metadata", propertyName);
                    Assertions.assertEquals("hello", value);
                    called.append(1);
                }
            });
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
        mpvService.fireEvent(
                PropertyObserver.buildPropertyChangeEvent("metadata", "hello", 1)
        );
        mpvService.waitForEvent("metadata", 100);
        Assertions.assertEquals(1, called.length());
    }

    @Test
    public void testCustomEvent() throws InterruptedException {
        final StringBuilder called = new StringBuilder();
        mpvService.registerEvent(new NamedEventHandler("x-custom") {
            @Override
            public Runnable doHandle(JsonObject message) {
                Assertions.assertEquals("x-custom", message.get("event").getAsString());
                called.append(1);
                return null;
            }
        });
        mpvService.fireEvent("x-custom");
        mpvService.fireEvent("x-custom");
        mpvService.fireEvent("x-custom");
        Thread.sleep(100);
        Assertions.assertEquals(3, called.length());
    }

    @Test
    public void testNonExistingEvent() throws InterruptedException {
        Thread.sleep(1000);
        final StringBuilder called = new StringBuilder();
        mpvService.registerEvent(new NamedEventHandler("x-custom") {
            @Override
            public Runnable doHandle(JsonObject message) {
                Assertions.assertEquals("x-custom", message.get("event").getAsString());
                called.append(1);
                return null;
            }
        });
        mpvService.fireEvent("x-custom2");
        mpvService.fireEvent("x-custom3");
        Thread.sleep(500);
        Assertions.assertEquals(0, called.length());
    }
}
