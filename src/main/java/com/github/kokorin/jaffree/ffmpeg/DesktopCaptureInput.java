/*
 *    Copyright  2020 Vicne
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.github.kokorin.jaffree.ffmpeg;

import com.github.kokorin.jaffree.OS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * This Input provides a live capture of your computer desktop as source.
 * <p>
 * Most of the information comes from https://trac.ffmpeg.org/wiki/Capture/Desktop
 * <p>
 * TODO list:
 * - Screen selection when multiscreen?
 * - Audio Capture?
 * - Warn if framerate is not set (like FrameInput)
 * - Call ffmpeg to return list of devices? list of screens?
 */
public class DesktopCaptureInput extends BaseInput<DesktopCaptureInput> implements Input {
    // Constant to select Windows capture mode (GDI vs DirectShow). I don't know which one to choose...
    private final static boolean WINDOWS_USE_GDI = true;

    private String input = "";

    private static final Logger LOGGER = LoggerFactory.getLogger(DesktopCaptureInput.class);

    /**
     * Test if the current OS and configuration supports selecting an area to capture.
     * @return true if setArea() is effective on this platform, false if it is ignored (with a warning).
     */
    public static boolean isAreaSelectionSupported() {
        return (OS.IS_LINUX || (OS.IS_WINDOWS && WINDOWS_USE_GDI));
    }

    /**
     * Create a DesktopCaptureInput suitable for your platform
     * @param screen (unused for now)
     */
    public DesktopCaptureInput(String screen) {
        if (OS.IS_LINUX) {
            setFormat("x11grab");
            input = ":0.0";
            setInput(input);
        }
        else if (OS.IS_MAC) {
            // Device list can be obtained with ffmpeg -f avfoundation -list_devices true -i ""
            setFormat("avfoundation");
            setInput("default:none");
            // For audio: setInput("default:default");
        }
        else if (OS.IS_WINDOWS) {
            if (WINDOWS_USE_GDI) {
                setFormat("gdigrab");
                setInput("desktop");
            }
            else {
                // Using DirectShow
                // Device list can be obtained with ffmpeg -f dshow -list_devices true -i ""
                setFormat("dshow");
                setInput("video=\"screen-capture-recorder\"");
                // For audio: setInput("video=\"screen-capture-recorder\":audio=\"virtual-audio-capturer\"");
            }
        }
    }

    /**
     * Limit capture to the given area.
     * <p>
     * Note that this feature is not supported on all OS/configuration combination.
     * In case it is not supported, a warning will be printed and the full desktop will be captured.
     * @param area the Rectangle to limit capture to
     * @return this
     */
    public DesktopCaptureInput setArea(Rectangle area) {
        if (area != null) {
            if (isAreaSelectionSupported()) {
                if (OS.IS_LINUX) {
                    // Specific way to select area with avfoundation
                    addArguments("-video_size", area.width + "x" + area.height);
                    setInput(input + "+" + area.x + "," + area.y);
                    return this;
                }
                else if (OS.IS_WINDOWS && WINDOWS_USE_GDI) {
                    // Specific way to select area with gdigrab
                    addArguments("-video_size", area.width + "x" + area.height);
                    addArguments("-offset_x", String.valueOf(area.x));
                    addArguments("-offset_y", String.valueOf(area.x));
                }
                else {
                    LOGGER.error("Error selecting area - Unknown OS/Device configuration");
                }
            }
            else {
                LOGGER.warn("Setting an area is not supported with this OS/configuration. " +
                            "Please add the following filter to your FFmpeg chain: " +
                            ".setFilter(\"crop=" + area.width + ":" + area.height + ":" + area.x + ":" + area.y + "\")");
            }
        }
        return this;
    }


    /**
     * Set frame rate.
     * <p>
     * Captures the desktop at the given frame rate
     *
     * @param value Hz value, fraction or abbreviation
     * @return this
     */
    @Override
    public DesktopCaptureInput setFrameRate(Number value) {
        return super.setFrameRate(value);
    }

    /**
     * Include mouse cursor (only works on Mac)
     * <p>
     * Note that this feature is not supported on all OS/configuration combination.
     * In case it is not supported, a warning will be printed and the full desktop will be captured.
     *
     * @param includeCursor
     * @return
     */
    public DesktopCaptureInput includeMouseCursor(boolean includeCursor) {
        if (OS.IS_MAC && includeCursor) {
            addArguments("-capture_cursor", "1");
        }
        else {
            LOGGER.warn("Choosing whether mouse cursor should be included is not supported with your configuration. ");
        }
        return this;
    }

    public static DesktopCaptureInput fromScreen() {
        return new DesktopCaptureInput(null);
    }

    public static DesktopCaptureInput fromScreen(String screen) {
        return new DesktopCaptureInput(screen);
    }
}
