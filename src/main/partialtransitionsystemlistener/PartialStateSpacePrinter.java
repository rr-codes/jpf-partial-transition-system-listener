package partialtransitionsystemlistener;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Set;

public interface PartialStateSpacePrinter {
    void printResult(Map<Integer, Set<Integer>> transitions, PrintWriter writer);

    String getFileName(String sutName);
}
