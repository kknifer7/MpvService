package io.github.macfja.mpv.communication;

import com.google.gson.JsonObject;
import io.github.kknifer7.util.GsonUtil;
import io.github.kknifer7.util.SystemUtil;
import io.github.macfja.mpv.communication.handling.MessageHandlerInterface;
import org.scalasbt.ipcsocket.UnixDomainSocket;
import org.scalasbt.ipcsocket.Win32NamedPipeSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The implementation of communication interface.
 * It use the unix "{@code nc}" command communicate with MPV IPC.
 *
 * @author MacFJA
 */
public class Communication implements CommunicationInterface {
    /**
     * The process used to write and read data.
     */
    private Socket ioSocket;
    /**
     * The writer
     */
    private BufferedWriter ioWriter;
    /**
     * Indicate if we should send a quite command to MPV when the {@code close} method is call.
     *
     * @see Communication#close()
     */
    private boolean exitOnClose = true;
    /**
     * The path to the socket that MPV listen
     */
    private String socketPath;
    /**
     * The class logger
     */
    protected Logger logger = LoggerFactory.getLogger(getClass());
    /**
     * The listening part
     */
    private final MessagesListener messagesListener;
    /**
     * Indicate if the communication is open
     */
    private boolean isOpen = false;

    @Override
    public void setExitOnClose(boolean exitOnClose) {
        this.exitOnClose = exitOnClose;
    }

    @Override
    public void setSocketPath(String socketPath) {
        this.socketPath = socketPath;
    }

    @Override
    public void addMessageHandler(MessageHandlerInterface messageHandler) {
        messagesListener.addMessageHandler(messageHandler);
    }

    /**
     * Constructor and initializer.
     */
    public Communication() {
        messagesListener = new MessagesListener(logger);
    }

    @Override
    public void removeMessageHandler(MessageHandlerInterface messageHandler) {
        messagesListener.removeMessageHandler(messageHandler);
    }

    @Override
    public List<MessageHandlerInterface> getMessageHandlers() {
        return messagesListener.getMessageHandlers();
    }

    @Override
    public void clearMessageHandlers() {
        messagesListener.clearMessageHandlers();
    }

    /**
     * Check if every component is ready ti be used.
     * Start them if necessary.
     *
     * @throws IOException If an error occurs when opening the communication or if the communication was closed unexpectedly.
     */
    private void ensureIoReady() throws IOException {
        if (ioWriter != null && messagesListener.isRunning() && ioSocket != null) {

            return;
        }
        if (isOpen) {
            throw new IOException("The communication was closed unexpectedly");
        }
        open();
    }

    @Override
    public int write(String command, List<? extends Serializable> arguments) throws IOException {
        ensureIoReady();

        ArrayList<Object> parameters = new ArrayList<>();
        parameters.add(command);
        parameters.addAll(arguments == null ? Collections.EMPTY_LIST : arguments);
        JsonObject json = new JsonObject();
        json.add("command", GsonUtil.toJsonTree(parameters));
        int requestId = ((int) Math.ceil(Math.random() * 1000));
        json.addProperty("request_id", requestId);
        String jsonStr = GsonUtil.toJson(json);
        logger.debug("Send: {}", jsonStr);

        ioWriter.write(jsonStr);
        ioWriter.newLine();
        ioWriter.flush();

        return requestId;
    }

    @Override
    public void simulateMessage(JsonObject message) {
        messagesListener.handleLine(message);
    }

    @Override
    public void open() throws IOException {
        logger.info("Starting processes");
        try {
            if (ioSocket == null) {
                logger.info("Start MPV communication");
                ioSocket = newClientSocket();
                Thread.sleep(500);
            }
        } catch (IOException e) {
            logger.error("Unable to start communication", e);
            ioSocket = null;

            throw e;
        } catch (InterruptedException e) {
            logger.warn("Sleeping interrupted", e);
        }

        if (ioWriter == null) {
            logger.info("Start MPV writer");
            ioWriter = new BufferedWriter(new OutputStreamWriter(ioSocket.getOutputStream(), StandardCharsets.UTF_8));
        }
        if (!messagesListener.isRunning()) {
            logger.info("Start MPV reader");
            messagesListener.start(ioSocket.getInputStream());
        }
        isOpen = true;
    }

    private Socket newClientSocket() throws IOException {
        return SystemUtil.IS_OS_WINDOWS ?
                new Win32NamedPipeSocket(socketPath) : new UnixDomainSocket(socketPath);
    }

    @Override
    public void close() throws IOException {
        boolean closed = false;

        if (ioSocket == null && ioWriter == null) {

            return;
        }
        try {
            if (exitOnClose) {
                write("quit", null);
            }
            if (ioWriter != null) {
                ioWriter.close();
                closed = true;
            }
            if (!closed && ioSocket != null) {
                ioSocket.close();
            }
        } finally {
            ioSocket = null;
            ioWriter = null;
        }
    }
}
