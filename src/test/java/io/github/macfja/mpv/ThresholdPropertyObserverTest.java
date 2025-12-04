package io.github.macfja.mpv;

import com.google.gson.JsonObject;
import io.github.kknifer7.util.PropertyUtil;
import io.github.macfja.mpv.communication.handling.ThresholdPropertyObserver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ThresholdPropertyObserverTest {
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

        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "debug");
        System.setProperty("org.slf4j.simpleLogger.showShortLogName", "true");
        mpvService = (new Service(mpvPath));
    }
    @AfterAll
    static public void finish() {
        try {
            mpvService.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mpvService = null;
    }

    @Test
    public void testCustomEvent()
    {
        JsonObject event = ThresholdPropertyObserver.buildPropertyChangeEvent("x-fake-prop", "ok", 1);

        JsonObject event2 =  ThresholdPropertyObserver.buildPropertyChangeEvent("x-fake-prop", "ok", 2);

        final StringBuilder called = new StringBuilder();
        try {
            mpvService.registerPropertyChange(new io.github.macfja.mpv.communication.handling.ThresholdPropertyObserver(1.5f, "x-fake-prop", 1) {
                @Override
                public void changed(String propertyName, Object value, Integer id) {
                    Assertions.assertEquals("x-fake-prop", propertyName);
                    Assertions.assertEquals("ok", value);
                    called.append(1);
                }
            });

            mpvService.registerPropertyChange(new io.github.macfja.mpv.communication.handling.ThresholdPropertyObserver(1.5f, "x-fake-prop", 2) {
                @Override
                public void changed(String propertyName, Object value, Integer id) {
                    Assertions.assertEquals("x-fake-prop", propertyName);
                    Assertions.assertEquals("ok", value);
                    Assertions.assertEquals(2, id);
                    called.append(1);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            mpvService.fireEvent(event2);
            mpvService.fireEvent(event);
            mpvService.fireEvent(event);
            mpvService.fireEvent(event);
            Thread.sleep(600);
            mpvService.fireEvent(event);
            mpvService.fireEvent(event2);
            mpvService.fireEvent(event);
            mpvService.fireEvent(event);
            Thread.sleep(600);
            mpvService.fireEvent(event);
            mpvService.fireEvent(event2);
            mpvService.fireEvent(event);
            mpvService.fireEvent(event);
            Thread.sleep(600);
            mpvService.fireEvent(event);
            mpvService.fireEvent(event);
            mpvService.fireEvent(event);

            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(3, called.length());
    }
}
