package network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ProtocolEngine {
    void handleConnection(InputStream is, OutputStream os) throws IOException;
}
