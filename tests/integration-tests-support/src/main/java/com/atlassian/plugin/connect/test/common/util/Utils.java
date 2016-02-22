package com.atlassian.plugin.connect.test.common.util;

import java.io.IOException;
import java.net.ServerSocket;

import org.apache.commons.io.IOUtils;

/**
 *
 */
public class Utils {
    public static String loadResourceAsString(String path) throws IOException {
        return IOUtils.toString(Utils.class.getClassLoader().getResourceAsStream(path));
    }

    public static int pickFreePort() {
        ServerSocket socket = null;
        try {
            socket = new ServerSocket(0);
            return socket.getLocalPort();
        } catch (IOException e) {
            throw new RuntimeException("Error opening socket", e);
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    throw new RuntimeException("Error closing socket", e);
                }
            }
        }
    }
}
