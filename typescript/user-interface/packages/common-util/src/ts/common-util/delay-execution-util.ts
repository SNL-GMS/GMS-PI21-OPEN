/**
 * Delay invoking a function for some number of milliseconds.
 *
 * @param func the function to call on a delayed time
 * @param delayMillis the number of milliseconds to delay
 */
export const delayExecution = async <T>(func: () => T, delayMillis = 50): Promise<T> =>
  new Promise<T>((resolve, reject) => {
    setTimeout(() => {
      try {
        resolve(func());
      } catch (e) {
        reject(e);
      }
    }, delayMillis);
  });

/**
 * Calls a function that accepts an arbitrary number of
 * arguments within setTimeout and returns clearTimeout
 *
 * @param func the function to be called
 * @param timeout optional timeout
 * @param funcArgs function arguments
 * @returns
 */
export function delayExecutionReturnClearTimeout(
  func: (...args: any[]) => void,
  timeout = 0,
  ...funcArgs: any[]
): () => void {
  const timer = setTimeout(() => func(...funcArgs), timeout);
  return () => {
    clearTimeout(timer);
  };
}
