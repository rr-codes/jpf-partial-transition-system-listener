package partialtransitionsystemlistener;

import gov.nasa.jpf.search.*;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

@SuppressWarnings("unused")
public class StateSpacePrinter extends SearchListenerAdapter {
    private int source;
    private int target;
    private PrintWriter writer;

    public StateSpacePrinter() {
        this.source = -1;
        this.target = -1;
    }

    @Override
    public void searchStarted(Search search) {
        String name = search.getVM().getSUTName() + ".dot";
        try {
            this.writer = new PrintWriter(name);
            this.writer.println("digraph statespace {");
            this.writer.println("node [style=filled]");
            this.writer.println("0 [fillcolor=green]");
        } catch (FileNotFoundException e) {
            System.err.println("Listener could not write to file " + name);
            search.terminate();
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        this.source = this.target;
        this.target = search.getStateId();

        if (this.source != -1) {
            System.out.printf("%d -> %d%n", source, target);
        }

        if (search.isEndState()) {
            this.writer.printf("%d [fillcolor=red]%n", target);
        }
    }

    @Override
    public void searchFinished(Search search) {
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