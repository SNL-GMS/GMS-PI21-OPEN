# Testing of `RollupEvaluator`

During the early stages of the development of `RollupEvaluator`, some bugs were found, 
even though there was full 100% coverage. Thus some innovation occured to provide a novel
and extremely thorough suite of regression tests. This document explains the approach.

## Expression tree evaluation

The `RollupEvaluator` is effectively an expression tree evaluator, where leaves of the tree
are `SohStatus` values, and nodes are one of the operators `BEST_OF`, `WORST_OF`, `MIN_GOOD_OF`.
There are only only a limited noumber of possible values that any combination of these
operators can evaluate to: `BAD`, `MARGINAL`, `GOOD`. Thus it was easy to think that an evaluation
was correct, even when it was out of pure coincedence that it arrived at the right answer. This is
why the coverage of 100% could be misleading.

Because of this, testing was designed to test that `RollupEvaluator` respected the structure of
this expression tree - that it did not effectively shuffle nodes to create a different expression.

## `RollupEvaluatorTests`

This test suite contains basic tests of `RollupEvaluator` using just the normal `BEST_OF`, `WORST_OF`,
`MIN_GOOD_OF` operations. It includes some basic 2- to 3- deep nested operators, and an operator
that whose operands at each level are nondeterministically ordered becuase it is construct from
nested `Map.of` operations. This reached 100% coverage or near it for `RollupEvaluator`.

## `RollupEvaluatorExtraTests`

This test suite contains more advanced tests. It introduces two new test-only RollupOperators,
`AdditionRollupOperator` and `MultiplicationRollupOperator`. These operators are non-sensical in the
real system, but are useful for testing `RollupEvaluator` because, mathematically, thay are associative
and commutative, and `MultiplicationRollupOperator` distributes across `AdditionRollupOperator`, just like
the analogous operators on real numbers. What makes them useful as that they *do not* distribute across 
the normal operators. Therefore, for some combination of these operators and the normal ones, an inadverdant 
shifting of the tree structure by the evaluator is more likely to be detected.

One example of these tests tests the following:

```java
        // Let ^ represent BEST_OF, then:
        //
        // MARGINAL = (GOOD + MARGINAL) ^ [(MARGINAL + GOOD) + (BAD + BAD)]                  (1)
        //
        //  ~ BUT ~
        //
        // BAD = [(GOOD + MARGINAL) ^ (MARGINAL + GOOD)] + [(GOOD + MARGINAL) ^ (BAD + BAD)] (2)
```

In equation (2), we are distributing the BEST_OF operation (^) across the `addition` in the middle of the 
square bracks in (1). The `RollupEvaluator` should evaluate to `MARGINAL` for (1) and `BAD` for (2). If it
evaluates to `MARGINAL` for (2), like it would if the BEST_OF operator was replaced by `multiplication`,
it means that the evaluator has effectively changed the tree structure and a bug has been found.

## Final not about coverage

Until `Validate` statements were added into the `evaluate` alghorithm, there was 100% test coverage. With them,
the test coverage is 97.2%. These `Validate` statements exist to debug the code upon future refactors and rewrites.
