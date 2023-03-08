package gms.core.performancemonitoring.soh.control.capabilityrollup;

import gms.core.performancemonitoring.soh.control.configuration.RollupOperator;
import gms.shared.frameworks.osd.coi.soh.SohStatus;
import org.apache.commons.lang3.Validate;

import java.util.AbstractCollection;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a rollup evaluator.
 * <p>
 * NOTE: This evaluator does not impose an ordering on operands. Therefore, it is assumed that
 * all operations that it evaluates are ASSOCIATIVE.
 * <p>
 * This assumption allows upstream processing to use parallelization on the operands if so desired.
 * <p>
 * If requirements ever include NON-ASSOCIATIVE operators, this code must be reworked to impose
 * a KNOWN ordering of operands, and upstream processing SHALL NOT parallelize the processing of
 * operands, unless ordering can be preserved.
 *
 * @param <V> Type of operands for the terminal operators (leaf nodes).
 */
class RollupEvaluator<V> {

  private RollupOperator rollupOperator;
  private Function<RollupOperator, List<V>> operandSelector;
  private Function<V, SohStatus> objectMapper;

  private RollupEvaluator(
    RollupOperator rollupOperator,
    Function<RollupOperator, List<V>> operandSelector,
    Function<V, SohStatus> objectMapper) {
    this.rollupOperator = rollupOperator;
    this.operandSelector = operandSelector;
    this.objectMapper = objectMapper;
  }

  /**
   * Creates an instance of the RollupEvaluator for an arbitrary level (channel, station, station
   * group)
   *
   * @param rollupOperator The rollupOperator to use
   * @param rollupOperator The rollupOperator to use
   * @param operandSelector Selects which parameters to feed to the operator. Should be one of *
   * RollupOperator::getSohMonitorTypeOperands (V = SohMonitorType), RollupOperator::getChannelOperands
   * * (V = String), RollupOperator::getStationOperands (V = String).
   * @param objectMapper A closure that takes an object of type V and returns an SohStatus
   */
  static <V> RollupEvaluator<V> create(
    RollupOperator rollupOperator,
    Function<RollupOperator, List<V>> operandSelector,
    Function<V, SohStatus> objectMapper
  ) {

    return new RollupEvaluator<>(
      rollupOperator,
      operandSelector,
      objectMapper
    );
  }

  /**
   * Calculates the rollup for an arbitrary level (channel, station, station group)
   *
   * @return The SohStatus that results from calling operate on the roll up operator type on the
   * operands.
   */
  SohStatus evaluate() {
    //
    // This is an iterative implementation of what amounts to an expression tree evaluator.
    //

    var expressionTreeNodeStack = getExpressionTreeNodeStack();

    //
    // Find the lowest level so that when we create the evaluationArrayList array its correct
    //
    var trueLevel = expressionTreeNodeStack.stream()
      .min(Comparator.comparingInt(node -> node.level))
      .orElseThrow(() -> new IllegalStateException(
        "CapabilityRollupUtility did not find a minimum level, cannot continue")).level;

    // trueLevel must be positive, so multiple by -1
    trueLevel *= -1;
    // trueLevel must be increased by one since the array size must be +1
    trueLevel++;

    ArrayList<LinkedList<SohStatus>> evaluationArrayList = new ArrayList<>();

    // Initialize the evaluation array
    for (var i = 0; i < trueLevel; i++) {
      evaluationArrayList.add(new LinkedList<>());
    }

    //
    // Simply loop over the tree stack, if a status is found throw it into the bucket
    // that corresponds to its level index. If a operator is found always evaluate the level
    // below it and store the value in it's own level.  Doing so will evaluate the entire tree
    // from the leaves all the way up to the root node, leaving evaluationArrayList[0] with the final status.
    // Note since level is stored as a negative number the indexing requires a multiplying the
    // level by -1
    //
    while (!expressionTreeNodeStack.isEmpty()) {

      var expressionTreeNode = expressionTreeNodeStack.poll();

      if (expressionTreeNode.status != null) {
        evaluationArrayList.get(expressionTreeNode.level * -1).add(expressionTreeNode.status);
      } else {
        var status = expressionTreeNode.operator.operation()
          .apply(evaluationArrayList.get((expressionTreeNode.level - 1) * -1));
        evaluationArrayList.get((expressionTreeNode.level - 1) * -1).clear();
        evaluationArrayList.get(expressionTreeNode.level * -1).add(status);
      }
    }

    //
    // This will validate there is only one list with one value remaining (the final status)
    //
    Validate.isTrue(evaluationArrayList.stream().filter(list -> list.size() == 1).count() == 1);

    //
    // This will validate that all the other lists are empty, which means all values were evaluated
    //
    Validate.isTrue(
      evaluationArrayList.stream().filter(AbstractCollection::isEmpty).count() == trueLevel - 1);

    //
    // Return the final calculated status. This will always be at the root level 0 list
    //
    return evaluationArrayList.get(0).getFirst();
  }

  private Deque<ExpressionTreeNode> getExpressionTreeNodeStack() {

    var operatorsFrontier = new PriorityQueue<RollupOperatorContainer>(
      Comparator.comparing(rollupOperatorContainer -> rollupOperatorContainer.level));

    var expressionTreeNodeStack = new ArrayDeque<ExpressionTreeNode>();

    //
    // Add the tree root to the frontier.  Note the root is always level 0
    //
    operatorsFrontier.add(new RollupOperatorContainer(rollupOperator, 0));

    //
    // At the end of this loop, expressionTreeNodeStack will always be ordered in such a way that
    // the children leaves are always above their respective parents. This order is important
    // for the next step in evaluating the stack.
    //
    while (!operatorsFrontier.isEmpty()) {

      //
      // pull from the frontier and explore the children
      //
      var rollupOperatorContainer = operatorsFrontier.poll();
      var nestedRollupOperator = rollupOperatorContainer.rollupOperator;
      var level = rollupOperatorContainer.level;

      //
      // Pass in the level to the expressionTreeNode constructor, this ensures the correct
      // bucket is used in the evaluation loop below
      //
      var expressionTreeNode = new ExpressionTreeNode(level);

      if (!isTerminalOperator(nestedRollupOperator)) {

        //
        // Add the operator to the expressionNodeStack
        // Note for each new addition to the frontier the level is decreased
        //
        expressionTreeNode.operator = nestedRollupOperator;
        nestedRollupOperator.getRollupOperatorOperands()
          .forEach(ro -> operatorsFrontier.add(new RollupOperatorContainer(ro, level - 1)));
      } else {
        expressionTreeNode.status = nestedRollupOperator.operation().apply(
          operandSelector.apply(nestedRollupOperator).stream().map(
            objectMapper).collect(Collectors.toList()));
      }

      expressionTreeNodeStack.push(expressionTreeNode);
    }

    return expressionTreeNodeStack;
  }

  /**
   * indicates whether the given rollup operator is "terminal", that is, does not have nested rollup
   * operators.
   */
  private static boolean isTerminalOperator(RollupOperator operator) {
    return operator.getRollupOperatorOperands().isEmpty();
  }

  /**
   * Something to allow us to put either an operator or a status on the operator stack
   */
  private static class ExpressionTreeNode {

    RollupOperator operator;
    SohStatus status;
    Integer level;

    ExpressionTreeNode(Integer level) {
      this.level = level;
    }

  }

  /**
   * Something that allows us to maintain order for the frontier as we explore via BFS
   */
  private static class RollupOperatorContainer {

    RollupOperator rollupOperator;
    Integer level;

    RollupOperatorContainer(RollupOperator ro, Integer level) {
      this.level = level;
      this.rollupOperator = ro;
    }

  }

}
