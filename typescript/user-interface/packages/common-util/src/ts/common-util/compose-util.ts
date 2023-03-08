/**
 * Composes a function that invokes the given functions from right to left.
 *
 * @param funcs the functions to compose
 */
// TODO Replace this with lodash's flowRight function
// eslint-disable-next-line @typescript-eslint/ban-types, @typescript-eslint/explicit-module-boundary-types
export function compose(...funcs: Function[]) {
  funcs.reverse();
  return (...args: unknown[]) => {
    const [firstFunction, ...restFunctions] = funcs;
    // eslint-disable-next-line prefer-spread
    let result = firstFunction.apply(null, args);
    restFunctions.forEach(fnc => {
      result = fnc.call(null, result);
    });
    return result;
  };
}
