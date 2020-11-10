package network;

import tictactoe.StatusException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ProtocolEngine {
    /**
     * Setup protocol engine. Provide streams to read from and write to.
     * @param is
     * @param os
     * @throws IOException
     */
    void handleConnection(InputStream is, OutputStream os) throws IOException;

    /**
     * Stop engine - close streams and release all resources
     * @throws IOException
     */
    void close() throws IOException;

    void subscribeGameSessionEstablishedListener(GameSessionEstablishedListener ocListener);

    void unsubscribeGameSessionEstablishedListener(GameSessionEstablishedListener ocListener);
}
