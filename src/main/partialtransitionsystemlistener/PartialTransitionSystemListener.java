package partialtransitionsystemlistener;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListenerAdapter;

public class PartialTransitionSystemListener extends SearchListenerAdapter {
    private final Map<Integer, Set<Integer>> transitions;
    private PrintWriter writer;

    private int source;
    private int target;

    private final PartialStateSpacePrinter stateSpacePrinter;

    public PartialTransitionSystemListener(Config config, JPF jpf) {
        this.transitions = new HashMap<>();

        this.source = -1;
        this.target = -1;

        boolean useDOTFormat = config.getBoolean("jpf.partialtransitionsystemlistener.usedot", true);
        this.stateSpacePrinter = useDOTFormat ? new DOTListener() : new TRAListener();
    }


    public void searchStarted(Search search) {
        String name = stateSpacePrinter.getFileName(search.getVM().getSUTName());
        try {
            this.writer = new PrintWriter(name);
        } catch (FileNotFoundException e) {
            System.out.println("Listener could not write to file " + name);
            search.terminate();
        }
    }

    @Override
    public void stateAdvanced(Search search) {
        this.source = this.target;
        this.target = search.getStateId();

        this.transitions.computeIfAbsent(this.source, k -> new HashSet<>()).add(this.target);
    }

    public void searchFinished(Search search) {
        this.stateSpacePrinter.printResult(transitions, writer);
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