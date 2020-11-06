package view;

import java.io.IOException;
import java.io.PrintStream;

public interface PrintStreamView {
    /**
     * Print a (visual) representation to given stream
     * @param ps
     * @throws IOException
     */
    void print(PrintStream ps) throws IOException;
}
