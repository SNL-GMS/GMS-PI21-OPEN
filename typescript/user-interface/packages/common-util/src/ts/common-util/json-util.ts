/**
 * Converts an JS object to a prettified JSON object string.
 *
 * NOTE: This protects against and handles any circular references.
 *
 * @param object object to stringify and written to a file
 * @param fileName filename do NOT include extension
 */
export function jsonPretty(object: unknown): string {
  const getCircularReplacer = () => {
    const seen = new WeakSet();
    return (key, value) => {
      if (typeof value === 'object' && value !== null) {
        if (seen.has(value)) {
          return;
        }
        seen.add(value);
      }
      // eslint-disable-next-line consistent-return
      return value;
    };
  };
  return JSON.stringify(object, getCircularReplacer(), 2);
}
