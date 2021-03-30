package partialtransitionsystemlistener;

import java.io.PrintWriter;
import java.util.*;

public class TRAListener implements PartialStateSpacePrinter {
    @Override
    public String getFileName(String sutName) {
        return sutName + ".txt";
    }

    @Override
    public void printResult(Map<Integer, Set<Integer>> transitions, PrintWriter writer) {
        Set<Integer> unexploredStates = new HashSet<>();

        for (Map.Entry<Integer, Set<Integer>> entry : transitions.entrySet()) {
            Integer source = entry.getKey();

            Set<Integer> targets = entry.getValue();

            for (int target : targets) {
                writer.printf("%d -> %d%n", source, target);

                if (!transitions.containsKey(target)) {
                    unexploredStates.add(target);
                }
            }
        }

        StringJoiner sj = new StringJoiner(" ");
        for (Integer state : unexploredStates) {
            sj.add(state.toString());
        }

        writer.printf(sj.toString());
    }
}
