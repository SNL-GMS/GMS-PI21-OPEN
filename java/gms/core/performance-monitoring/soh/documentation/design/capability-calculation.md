# Capability Rollup Calculation

## General Rollup Evaluation

A capability rollup calculation involves the evaluation of a *single* rollup operator.
A single rollup is evaluated for station group, one for each station in the station group,
and another for each channel in each station. Later sections will describe those levels.
This section describes the evaluation of a rollup operator, regardles of whether it is
for the station group, station, or channel.

### `RollupOperator` class

The `RollupOperator` class contains four "operand" fields: `stationOperands`, `channelOperands`,
`sohMonitorTypeOperands`, and `rollupOperatorOperands`. That latter is for "non-terminal"
operators. The former three are filled out according to whether this operator is for station group,
station, or channel. These three fields are not important for the rollup evaluator. All four
fields are prescribed by architecture guidance.

The fifth field in this class is `rollupOperatorType`. This is the type of operator, and indicates
which calculation to perform on the operands (`BEST_OF`, `WORST_OF`, `MIN_GOOD_OF`).
This is also prescribed by architecture.

A sixth field, not prescribed by architecture (and therefore ignored during serialization), 
is `operation`. This returns a closure that does the actual calculation.

### `RollupEvaluator` class

The `RollupEvaluator` class performs the rollup calculation. An instance is obtained with
the `create` static method:

```java
  static <V> RollupEvaluator<V> create(
      RollupOperator rollupOperator,
      Function<RollupOperator, List<V>> operandSelector,
      Function<V, SohStatus> objectMapper
  ) 
```

This takes three arguments: `rollupOperator` is the operator to evaluate, `operandSelector`
indicates which set of operands to use (this is normally a method reference to `stationOperands`, 
`channelOperands`, or `sohMonitorTypeOperands`), and `objectMapper` takes those operands to an
actual calculated status that can be given to the calculation.

As an example, to calculate the rollup for a single channel, we would do the following:

```java

    var channelSoh = ... // get ChannelSoh object for some channel 
...

    var rollupEvaluator = RollupEvaluator.create(
        myChannelRollupOperator,
        RollupOperator::getChannelOperands,
        sohMonitorType -> getSohStatusForMonitorType(channelSoh, sohMonitorType)
    );
    
    var sohStatus = rollupEvaluator.evaluate();
...
```

The `evaluate` method then evaluates the rollup operator by doing the following:
* call `operator()` on the RollupOperator to get the calculation to perform
* call the `operandSelector` (in this case, getChannelOperands) on the RollupOperator to get the operands to use
* map the operands to actual `SohStatus`es, in this case via some method called `getSohStatusForMonitorType`
* apply the closure returned by `operator` to the list of `SohStatus`es from the previous step. This returns a new single `SohStatus`.

The `evaluate` method makes use of `rollupOperatorOperands`, which is a list of more `RollupOperators`, to evaluate 
nested rollup operators. The `RollupEvaluator` class contains detailed comments about how the evaluation
algorithm works, including how nested operators are handled.

The document `rollup-evaluation-testing` describes the thorough testing done on the `RollupEvaluator` class.

## `enum RollupOperatorType`

The `RollupOperatorType` enum contains enumeration objects `BEST_OF`, `WORST_OF`, `MIN_GOOD_OF` as prescribed by
architecture. Each object overrides the abstract `getOperation` method:

```java
public abstract Function<List<SohStatus>, SohStatus> getOperation(int ... parameters);
```

This returns a closure that encapsulates an optional list of paramaters. The parameters tweak the
behavior of the calculation. At the moment, the only time this list is non-empty is when populated
by `MinGoodOfRollupOperator`, which extends `RollupOperator` and always has a `rollupOperatorType` of
`MIN_GOOD_OF`. The `MIN_GOOD_OF` operation includes two parameters: the first is the `goodThreshold`
and the second is the `marginalThreshold`. Descriptions of these paramters can be found in code documentation
and in architecture guidance.

The return closure takes in a list of `SohStatus` objects, and reduces them to a single `SohStatus` which
depends on the type of operation.

## Evaluation for station group, station, and channel

`RollupOperators` for the different SOH levels are handled in `CapabilityRollupUtility`. `calculateCapabilitySohRollupSet`
calculates the set of `CapabilitySohRollups` for an enitre set of `StationSoh` objects, where the calculation is defined 
by a set of `CapabilitySohRollupDefinition` objects. The following steps are taken:

* For each station group (represented by a single `CapabilityRollupDefinition`):
    1.  For each station in the station group:
        1.  For each channel in the station:
            * Get the `SohStatus`es for all monitors that are specified in confiuration
            * Evaluate the rollup operator on that list of `SohStatus`es, to create a single `SohStatus` for the channel
        2. Collect all of the `SohStatus`es for all of the channels of the station
        3. Evaluate the rollup operator on that list of `SohStatus`es, to create a single `SohStatus` for the station
    2.  Collect all of the `SohStatus`es for all of the stations of the station group
    3.  Evaluate the rollup operator on that list of `SohStatus`es, to create a single `SohStatus` for the station group.

This is roughly translated to this sequence of nested method calls in `CapabilityRollupUtility`: 
`calculateCapabilitySohRollup` -> `calculateChannelSohSetRollup` -> `calculateSohMvasSetRollup`

## Test Coverage
Test coverage for this functionality is the following:
* `CapabilityRollupUtility`: 100%
* `RollupEvaluator`: 97.2% (See `rollup-evaluation-testing` for more on what this means)
* `RollupOperatorType`: 100%