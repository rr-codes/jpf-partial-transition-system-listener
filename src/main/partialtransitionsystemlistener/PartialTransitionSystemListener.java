package partialtransitionsystemlistener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.annotation.JPFOption;
import gov.nasa.jpf.annotation.JPFOptions;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListenerAdapter;
import gov.nasa.jpf.vm.VM;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Add a partial state space observer to JPF and build a graph of the state space
 * that is explored by JPF. The graph can be generated in different formats.
 * The current formats that are supported are DOT (visualized by a tool
 * like GraphViz from ATT - http://www.graphviz.org/) and TRA.
 * The graph is stored in a file called "jpf-state-space.<extension>" where
 * extension is ".dot" or ".tra". By default it generates a DOT graph.
 *
 * @see gov.nasa.jpf.JPFListener
 *
 * @author Richard Robinson [Implementation, Documentation]
 * @author Matt Walker [Implementation, Testing]
 * @author Allen Kaplan [Testing, Documentation]
 * @author Akin Adewale [Testing, Documentation]
 */
@JPFOptions({
		@JPFOption(type = "Int", key = "partialtransitionsystemlistener.max_new_states", defaultValue = "0",
				comment = "maximum states for listener"),
		@JPFOption(type = "Boolean", key = "partialtransitionsystemlistener.use_dot", defaultValue = "true",
				comment = "If `true`, the generated file uses DOT notation; else, uses TRA notation."),
})
public class PartialTransitionSystemListener extends SearchListenerAdapter {
	private final static String CONFIG_PREFIX = "partialtransitionsystemlistener";

	private final Map<Integer, Set<Integer>> transitions;
	private final PartialStateSpacePrinter stateSpacePrinter;
	private final VM vm;
	private final int maxNewStates;

	private PrintWriter writer;

	private int source;
	private int target;
	private int newStates;
	private Integer endState;

	public PartialTransitionSystemListener(Config config, JPF jpf) {
		this.transitions = new HashMap<>();

		this.newStates = 0;

		this.source = -1;
		this.target = -1;
		this.endState = null;

		this.maxNewStates = config.getInt(CONFIG_PREFIX + ".max_new_states", 0);
		boolean useDOTFormat = config.getBoolean(CONFIG_PREFIX + ".use_dot", true);
		this.stateSpacePrinter = useDOTFormat ? new DOTListener() : new TRAListener();

		this.vm = jpf.getVM();
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

		if (search.isEndState()) {
			this.endState = search.getStateId();
		}

		if (!search.isNewState()) {
			return;
		}

		if (!this.vm.isTraceReplay()) {
			this.newStates++;
		}

		if (this.newStatesExceeded()) {
			search.notifySearchConstraintHit("New States Exceeded at: " + this.maxNewStates);
			search.terminate();
		}
	}

	public void searchFinished(Search search) {
		this.stateSpacePrinter.printResult(transitions, writer, endState);
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

	private boolean newStatesExceeded() {
		return this.maxNewStates > 0 && this.newStates > this.maxNewStates;
	}
}