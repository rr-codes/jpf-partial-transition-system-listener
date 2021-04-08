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
 * that is explored by JPF, as well as any unexplored states. The graph can be
 * generated in different formats. The current formats that are supported are
 * DOT and TRA. The graph is stored in a file called "jpf-state-space.extension"
 * where extension is ".dot" or ".tra". By default it generates a DOT graph.
 *
 * <p></p>
 *
 * <b>Options</b> (all keys should be prefixed with {@code partialtransitionsystemlistener.}):
 * <table summary="options">
 * <tr>
 * <td> <b>Key</b> </td> <td> <b>Type</b> </td> <td> <b>Default</b> </td>
 * <td> <b>Description</b> </td>
 * </tr>
 * <tr>
 * <td> {@code max_new_states} </td>
 * <td> {@code Integer} </td> <td> {@code 0} </td> <td>The maximum amount of
 * allowed states for this listener</td>
 * </tr>
 * <tr>
 * <td> {@code use_dot} </td>
 * <td> {@code Boolean} </td> <td> {@code true} </td> <td>If `true`, the
 * generated file uses DOT notation; else, uses TRA notation.</td>
 * </tr>
 * </table>
 *
 * @see gov.nasa.jpf.JPFListener
 *
 * @author Richard Robinson [Implementation, Testing, Documentation]
 * @author Matt Walker [Implementation, Testing, Documentation]
 * @author Allen Kaplan [Documentation]
 * @author Akin Adewale [Documentation]
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
	private int endState;

	/**
	 * Creates a new PartialTransitionSystemListener instance
	 * @param config the config properties
	 * @param jpf the JPF instance
	 */
	public PartialTransitionSystemListener(Config config, JPF jpf) {
		this.transitions = new HashMap<>();

		this.newStates = 0;

		this.source = -1;
		this.target = -1;
		this.endState = -1;

		this.maxNewStates = config.getInt(CONFIG_PREFIX + ".max_new_states", 0);
		boolean useDOTFormat = config.getBoolean(CONFIG_PREFIX + ".use_dot", true);
		this.stateSpacePrinter = useDOTFormat ? new DOTListener() : new TRAListener();

		this.vm = jpf.getVM();
	}

	/**
	 * Invoked when the search is started.
	 *
	 * @implNote Creates and instantiates a {@code PrintWriter} to be used for
	 * output. The path of the outputted file is the SUT name of the VM
	 * concatenated with either {@code .dot} or {@code .tra}, depending on the
	 * configuration.
	 *
	 * @param search the Search instance
	 */
	public void searchStarted(Search search) {
		String name = stateSpacePrinter.getFileName(search.getVM().getSUTName());
		try {
			this.writer = new PrintWriter(name);
		} catch (FileNotFoundException e) {
			System.out.println("Listener could not write to file " + name);
			search.terminate();
		}
	}

	/**
	 * Invoked when a state has advanced.
	 *
	 * @implNote The output is not processed in this method. Instead, the
	 * method adds the source and the target to a private MultiMap field
	 * (a map whose keys are the source states and whose values are a list
	 * of all the targets reached from the source). This is done to allow
	 * for customization of the output formatting / processing by the
	 * designated {@code PartialStateSpacePrinter}.
	 *
	 * <p></p>
	 * In addition, this method does additional logic depending on if the
	 * state is an end state or a new state, or if the number of new states
	 * has been exceeded. In the latter case, the search terminates.
	 *
	 * @param search the Search instance
	 */
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

	/**
	 * Invoked when the search has finished.
	 *
	 * @implNote This method delegates the output processing to the internal
	 * {@code PartialStateSpacePrinter} field. After, the {@code writer} is closed.
	 *
	 * @param search the Search instance
	 */
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