package partialtransitionsystemlistener;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListenerAdapter;

public class PartialTransitionSystemListener extends SearchListenerAdapter {
    private final Set<Integer> sourceStates;
    private final Set<Integer> targetStates;

    private PrintWriter writer;

    private int source;
    private int target;

    public void searchStarted(Search search) {
        String name = search.getVM().getSUTName() + ".dot";
        try {
            this.writer = new PrintWriter(name);
            this.writer.println("digraph statespace {");
            this.writer.println("node [style=filled]");
        } catch (FileNotFoundException e) {
            System.out.println("Listener could not write to file " + name);
            search.terminate();
        }
    }

    public PartialTransitionSystemListener() {
        this.sourceStates = new HashSet<>();
        this.targetStates = new HashSet<>();

        this.source = -1;
        this.target = -1;
    }

    @Override
    public void stateAdvanced(Search search) {
        this.source = this.target;
        this.target = search.getStateId();

        this.writer.printf("%d -> %d%n", source, target);

        this.sourceStates.add(this.source);
        this.targetStates.add(this.target);

        this.writer.printf("%d -> %d%n", source, target);

        this.writer.printf("%d [fillcolor=green]%n", source);
    }

    public void searchFinished(Search search) {
        this.targetStates.removeAll(this.sourceStates);

        for (int i : this.targetStates) {
            this.writer.printf("%d [fillcolor=red]%n", i);
        }

        this.writer.println("}");
        this.writer.close();
    }

    @Override
    public void stateBacktracked(Search search) {
        this.target = search.getStateId();
    }

    @Override
    public void stateRestored(Search search) {
        this.target = search.getStateId();
    }
}