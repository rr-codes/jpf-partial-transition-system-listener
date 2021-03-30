package partialtransitionsystemlistener;

import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DOTListener implements PartialStateSpacePrinter {
    @Override
    public String getFileName(String sutName) {
        return sutName + ".dot";
    }

    @Override
    public void printResult(Map<Integer, Set<Integer>> transitions, PrintWriter writer) {
        final Set<Integer> unexploredStates = new HashSet<>();
        final Set<Integer> allNodes = new HashSet<>();

        for (Map.Entry<Integer, Set<Integer>> entry : transitions.entrySet()) {
            int source = entry.getKey();
            Set<Integer> targets = entry.getValue();

            writer.printf("%d [fillcolor=green]%n", source);
            allNodes.add(source);

            for (int target : targets) {
                if (!transitions.containsKey(target)) {
                    unexploredStates.add(target);
                }

                if (!allNodes.contains(target)) {
                    writer.printf("%d [fillcolor=green]%n", target);
                }

                writer.printf("%d -> %d%n", source, target);

                allNodes.add(target);
            }
        }

        for (int i : unexploredStates) {
            writer.printf("%d [fillcolor=red]%n", i);
        }

        writer.println("}");
    }
}
