/**
 * Represents a key/value pair for an object.
 * Allows for a mapping from id to `undefined`.
 */
export interface KeyValue<K, V> {
  id: K;
  value: V;
}
