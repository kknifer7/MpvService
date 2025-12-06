package io.github.macfja.mpv.wrapper;

import io.github.macfja.mpv.Service;

import java.io.IOException;
import java.util.List;

/**
 * An implementation to connect to an existing instance of MPV
 *
 * @author MacFJA
 */
public class ExistingService extends Service {

    /**
     * Constructor
     *
     * @param socketPath The path to the socket where MPV communicate
     */
    public ExistingService(String mpvPath, String socketPath) {
        super(mpvPath);
        // Don't quit the existing service.
        ioCommunication.setExitOnClose(false);
        ioCommunication.setSocketPath(socketPath);
    }

    @Override
    protected void initialize(List<String> extraArgs) {
        try {
            ioCommunication.open();
        } catch (IOException e) {
            logger.error("Unable to start communication", e);
        }
    }

    @Override
    public void close() throws IOException {
        // Only close the communication part
        ioCommunication.close();
    }
}
