package partialtransitionsystemlistener;

import java.io.PrintWriter;
import java.util.*;

class TRAListener implements PartialStateSpacePrinter {
    @Override
    public String getFileName(String sutName) {
        return sutName + ".txt";
    }

    @Override
    public void printResult(Map<Integer, Set<Integer>> transitions, PrintWriter writer, Integer endState) {
        Set<Integer> unexploredStates = new HashSet<>();

        for (Map.Entry<Integer, Set<Integer>> entry : transitions.entrySet()) {
            int source = entry.getKey();
            Set<Integer> targets = entry.getValue();

            for (int target : targets) {
                writer.printf("%d -> %d%n", source, target);

                if (!transitions.containsKey(target)) {
                    unexploredStates.add(target);
                }
            }
        }

        StringJoiner sj = new StringJoiner(" ");
        for (int state : unexploredStates) {
            sj.add("" + state);
        }

        writer.printf(sj.toString());
    }
}
