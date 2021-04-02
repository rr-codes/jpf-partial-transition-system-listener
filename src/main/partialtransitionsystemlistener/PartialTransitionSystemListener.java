package partialtransitionsystemlistener;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.*;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.search.SearchListenerAdapter;
import gov.nasa.jpf.vm.VM;

public class PartialTransitionSystemListener extends SearchListenerAdapter {
	private final Map<Integer, Set<Integer>> transitions;
	private PrintWriter writer;

	private int source;
	private int target;

	private VM vm;
	private int maxNewStates;
	private int newStates;

	private final PartialStateSpacePrinter stateSpacePrinter;

	public PartialTransitionSystemListener(Config config, JPF jpf) {
		this.transitions = new HashMap<>();

		this.newStates = 0;

		this.source = -1;
		this.target = -1;

		boolean useDOTFormat = config.getBoolean("jpf.partialtransitionsystemlistener.usedot", true);
		this.stateSpacePrinter = useDOTFormat ? new DOTListener() : new TRAListener();
		this.maxNewStates = config.getInt("jpf.partialtransitionsystemlistener.max_new_states", 0);

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
		
		if (search.isNewState()) {
			if (!vm.isTraceReplay()) {
				newStates++;
			}
			if (newStatesExceeded()) {
				search.notifySearchConstraintHit("New States Exceeded at: " + this.maxNewStates);
				search.terminate();
			}
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

	public boolean newStatesExceeded() {
		if (maxNewStates > 0) {
			if (newStates > maxNewStates) {
				return true;
			}
		}
		return false;
	}

}