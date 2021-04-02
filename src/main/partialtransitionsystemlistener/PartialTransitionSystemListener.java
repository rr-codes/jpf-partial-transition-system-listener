package partialtransitionsystemlistener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListenerAdapter;
import gov.nasa.jpf.vm.VM;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PartialTransitionSystemListener extends SearchListenerAdapter {
	private final static String CONFIG_PREFIX = "jpf.partialtransitionsystemlistener";

	private final Map<Integer, Set<Integer>> transitions;
	private final PartialStateSpacePrinter stateSpacePrinter;
	private final VM vm;
	private final int maxNewStates;

	private PrintWriter writer;

	private int source;
	private int target;
	private int newStates;

	public PartialTransitionSystemListener(Config config, JPF jpf) {
		this.transitions = new HashMap<>();

		this.newStates = 0;

		this.source = -1;
		this.target = -1;

		this.maxNewStates = config.getInt(CONFIG_PREFIX + ".max_new_states", 0);
		boolean useDOTFormat = config.getBoolean(CONFIG_PREFIX + ".usedot", true);
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

	private boolean newStatesExceeded() {
		return this.maxNewStates > 0 && this.newStates > this.maxNewStates;
	}
}