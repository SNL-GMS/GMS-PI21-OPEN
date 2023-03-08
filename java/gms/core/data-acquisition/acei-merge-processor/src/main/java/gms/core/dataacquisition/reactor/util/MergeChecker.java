package gms.core.dataacquisition.reactor.util;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Base method for determining if two objects can be merged together
 *
 * @param <T>
 */
@FunctionalInterface
public interface MergeChecker<T> {

  boolean canMerge(T t1, T t2);

  default MergeChecker<T> and(MergeChecker<? super T> other) {
    return (t1, t2) -> canMerge(t1, t2) && checkNotNull(other).canMerge(t1, t2);
  }
}
