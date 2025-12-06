package io.github.macfja.mpv;

import com.google.gson.internal.LazilyParsedNumber;
import io.github.kknifer7.util.PropertyUtil;
import io.github.macfja.mpv.communication.handling.PropertyObserver;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Test case for the issue about nested call.
 *
 * @link https://github.com/MacFJA/MpvService/issues/2
 */
public class ConcurrencyTest {
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
        System.setProperty("org.slf4j.simpleLogger.showDateTime", "true");
        System.setProperty("org.slf4j.simpleLogger.dateTimeFormat", "HH:mm:ss");

        mpvService = new Service(mpvPath, List.of("--idle=yes", "--force-window=no"));
    }

    @AfterAll
    static public void finish() {
        try {
            Thread.sleep(1000);
            mpvService.close();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        mpvService = null;
    }

    @Test
    public void nestedCall() throws IOException {
        final AtomicBoolean observerCalled = new AtomicBoolean(false);
        final AtomicBoolean responseIsNull = new AtomicBoolean(true);
        mpvService.registerPropertyChange(new io.github.macfja.mpv.communication.handling.PropertyObserver("volume") {
            @Override
            public void changed(String propertyName, Object value, Integer id) {
                observerCalled.set(true);
                try {
                    String response = mpvService.getProperty("mpv-version");

                    // Before the correction, response is equals to `null`
                    responseIsNull.set(response == null);
                } catch (IOException e) {
                    Assertions.fail();
                    e.printStackTrace();
                }
            }
        });
        mpvService.setProperty("volume", "0");
        // The sendCommand timeout is defined to 10 * 500ms
        mpvService.waitForEvent("x-fake", 8000);
        Assertions.assertTrue(observerCalled.get(), "The event was never call");
        Assertions.assertFalse(responseIsNull.get(), "The response of the nested call is null");
    }

    @Test
    public void rapidEvent() throws IOException, InterruptedException {
        final List<Integer> values = new ArrayList<>();
        final List<Integer> expected = new ArrayList<>();
        PropertyObserver observer;
        mpvService.registerPropertyChange(observer = new PropertyObserver("volume") {
            @Override
            public void changed(String propertyName, Object value, Integer id) {
                synchronized (values) {
                    values.add(((LazilyParsedNumber) value).intValue());
                    values.notify();
                }
            }
        });
        synchronized (values) {
            values.wait();
        }
        // By default, an event with a volume of 100 will be received first
        expected.add(100);
        for (int iteration = 0; iteration < 10; iteration++) {
            mpvService.setProperty("volume", String.valueOf(iteration));
            expected.add(iteration);
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(11, values.size());
        mpvService.unregisterPropertyChange(observer);
    }
}
