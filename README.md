# EECS 4315 Project

Requires Java 1.8.0 v281.

**To generate JavaDoc**: `gradle javadoc`

**To run test suite**: `gradle test`

**To compile classes**: `gradle compile`

## Implementation Details

*Refer to JavaDoc for most implementation details*.

For each `PartialStateSpacePrinter` implementation (currently, only `DOTListener` and `TRAListener`),
the core logic is within the `printResult` method, which is invoked after the search is complete.
The listeners use the multimap provided as the parameter to `printResult` to process the state space
and output it to a file. 

The multimap is a map whose keys are the source states, and whose values is a set of every state directly
reachable from the source state. This allows the map to act as a simple directed graph data structure.

We consider states to be _explored_ or _unexplored_. A state is considered _unexplored_ if it is a destination
state, but never a source state; all other states are considered to be _explored_. In the DOT config, unexplored
states are red. In the TRA config, the unexplored states are listed at the end of the file.


The following config options are defined for the PartialStateSpacePrinter:

Name | Type | Default Value | Comment
--- | --- | ---
partialtransitionsystemlistener.max_new_states | Int | 0 | maximum states for listener
partialtransitionsystemlistener.use_dot | Boolean | true | If `true`, the generated file uses DOT notation; else, uses TRA notation.
