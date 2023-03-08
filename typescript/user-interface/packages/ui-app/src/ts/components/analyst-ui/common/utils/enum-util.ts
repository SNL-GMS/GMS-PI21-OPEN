/**
 * Gets key for value
 *
 * @param value value to get key for
 * @param enumToIterate enum that holds keys / value
 */
export function getKeyForEnumValue(value: unknown, enumToIterate: unknown): string {
  return Object.keys(enumToIterate).find(e => enumToIterate[e] === value);
}
