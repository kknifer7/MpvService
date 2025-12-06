package io.github.macfja.mpv;

import com.google.gson.JsonObject;
import io.github.kknifer7.util.PropertyUtil;
import io.github.kknifer7.util.SystemUtil;
import io.github.macfja.mpv.communication.handling.NamedEventHandler;
import io.github.macfja.mpv.wrapper.Shorthand;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.util.List;
import java.util.Map;

public class MpvShortHandTest {
    static Shorthand mpvService;

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

        mpvService = new Shorthand((new Service(mpvPath, List.of("--idle=yes", "--force-window=no"))));
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
    public void testMultiple() throws IOException {
        final StringBuilder sb = new StringBuilder();
        mpvService.registerEvent(new NamedEventHandler("start-file") {
            @Override
            public Runnable doHandle(JsonObject message) {
                sb.append(1);
                return null;
            }
        });
        mpvService.setProperty("volume", "13");
        Assertions.assertEquals(13f, mpvService.getProperty("volume", Float.class), 0.f);
        URL url = this.getClass().getResource("../../../../Discovery_Hit.mp3");
        mpvService.addMedia(
                SystemUtil.IS_OS_WINDOWS ? url.getPath().substring(1) : url.getPath(), false
        );
        mpvService.play();
        try {
            mpvService.waitForEvent("playback-restart");
            float pos1 = mpvService.getProperty("time-pos", Float.class);
            Thread.sleep(1000);
            float pos2 = mpvService.getProperty("time-pos", Float.class);
            Thread.sleep(2000);
            float pos3 = mpvService.getProperty("time-pos", Float.class);
            float delta1 = pos2 - pos1;
            float delta2 = pos3 - pos2;
            float tolerance = 0.05f;

            Assertions.assertTrue(delta1 > 0.95f && delta1 < 1.05, "Backing time! (" + pos1 + ", " + pos2 + ")");
            Assertions.assertTrue(delta2 > 1.9f && delta2 < 2.1f, "In a time machine!");

            Assertions.assertEquals(1.0f, delta1, tolerance,
                    "第一次增长时间偏差太大。预期 ~1.0 秒，实际：" + delta1 + " 秒");
            Assertions.assertEquals(2.0f, delta2, tolerance,
                    "第二次增长时间偏差太大。预期 ~2.0 秒，实际：" + delta2 + " 秒");

            mpvService.seek(1, Shorthand.Seek.Absolute);
            Assertions.assertEquals(1, mpvService.getProperty("time-pos", Integer.class));

            Thread.sleep(1000);
            float pos4 = mpvService.getProperty("time-pos", Float.class);
            mpvService.pause();
            Thread.sleep(1000);
            float pos5 = mpvService.getProperty("time-pos", Float.class);
            Assertions.assertEquals(pos4, pos5, 0.1f);
            mpvService.seek(-1, Shorthand.Seek.Relative);
            Assertions.assertEquals(pos5 - 1, mpvService.getProperty("time-pos", Float.class), 0.05f);
            Map<Shorthand.TimeKey, BigDecimal> time = mpvService.getTimes();
            Assertions.assertEquals(pos5 - 1, time.get(Shorthand.TimeKey.Elapsing).floatValue(), 0.5f);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assertions.assertEquals(1, sb.length());
    }

    protected void silentLoadFile() {
        try {
            mpvService.setProperty("volume", "0");
            URL url = this.getClass().getResource("../../../../Discovery_Hit.mp3");
            mpvService.addMedia(
                    SystemUtil.IS_OS_WINDOWS ? url.getPath().substring(1) : url.getPath(),
                    false
            );
            mpvService.waitForEvent("playback-restart");
            mpvService.pause();
            mpvService.seek(0, Shorthand.Seek.Absolute);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSeeks() {
        silentLoadFile();
        double total = 15.333875;
        try {
            mpvService.seek(1, Shorthand.Seek.Absolute);
            Assertions.assertEquals(1, mpvService.getProperty("time-pos", Float.class), 0.1f);
        } catch (IOException e) {
            Assertions.fail();
        }

        try {
            mpvService.seek(1, Shorthand.Seek.Relative);
            Assertions.assertEquals(2, mpvService.getProperty("time-pos", Float.class), 0.1f);
        } catch (IOException e) {
            Assertions.fail();
        }

        try {
            mpvService.seek(50, Shorthand.Seek.AbsolutePercent);
            Assertions.assertEquals(total / 2, mpvService.getProperty("time-pos", Float.class), 0.1f);
        } catch (IOException e) {
            Assertions.fail();
        }

        try {
            mpvService.seek(25, Shorthand.Seek.RelativePercent);
            Assertions.assertEquals(3 * total / 4, mpvService.getProperty("time-pos", Float.class), 0.1f);
        } catch (IOException e) {
            Assertions.fail();
        }
    }
}
