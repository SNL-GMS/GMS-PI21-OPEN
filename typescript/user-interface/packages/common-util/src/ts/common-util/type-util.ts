/**
 * Checks if an `unknown` object is an array. Returns true if it is an
 * array; false otherwise.
 *
 * @param object the object to check if the type is an array
 * @returns returns true if the object type is an array
 */
export const isArray = (object: unknown): object is unknown[] => Array.isArray(object);

/**
 * Checks if an `unknown` object is an object (and not an array). Returns true if it is an
 * object; false otherwise.
 *
 * @param object the object to check if the type is an object
 * @returns returns true if the object type is an object
 */
export const isObject = (object: unknown): object is unknown =>
  typeof object === 'object' && !isArray(object);

/**
 * Checks if an `unknown` object is an string. Returns true if it is an
 * string; false otherwise.
 *
 * @param object the object to check if the type is an string
 * @returns returns true if the object type is an string
 */
export const isString = (object: unknown): object is string => typeof object === 'string';

/**
 * Checks if an `unknown` object is an number. Returns true if it is an
 * number; false otherwise.
 *
 * @param object the object to check if the type is an number
 * @returns returns true if the object type is an number
 */
export const isNumber = (object: unknown): object is number => typeof object === 'number';

export interface Cancelable {
  cancel(): void;
  flush(): void;
}

/**
 * Checks if an `unknown` object is an array and it's contents are of type
 * object (i.e. not a string or a number). Returns true if it is an
 * array of objects; false otherwise.
 *
 * @param object the object to check if the type is an array of objects
 * @param shallow if true only checks the first element; false otherwise checks all elements. @default false
 * @returns returns true if the object type is an array of objects
 */
export const isArrayOfObjects = (object: unknown, shallow = false): object is unknown[] =>
  isArray(object) &&
  object.length > 0 &&
  (!shallow ? object.every(v => isObject(v)) : isObject(object[0]));

/**
 * Checks if object is defined
 *
 * @param object
 * @returns returns true if the object is not null or undefined
 */
export const isObjectDefined = (object: unknown): boolean =>
  object !== null && object !== undefined;

/**
 * Higher order function that takes in a string-typed enum and generates
 * a type guard function that can be used to check if a value
 * is of the original enum's type.
 *
 * * For example:
 *
 * enum MyEnum {
 *  ENTRY: "ENTRY"
 * }
 * const isMyEnum = createEnumTypeGuard(MyEnum);
 * isMyEnum(MyEnum.ENTRY); // true
 * isMyEnum("ENTRY"); // true
 * isMyEnum(MyEnum.MISS); // false
 * isMyEnum(SomeOtherEnum.ENTRY); // false
 * isMyEnum('random string that doesn\'t match'); // false
 *
 * @param en the enum for which to generate the type guard
 */
export const createEnumTypeGuard = <T>(en: T) => (token: unknown): token is T[keyof T] =>
  Object.values(en).includes(token as T[keyof T]);

/**
 * Runs the test function and ensures that the type returned is of the expected type.
 *
 * @param props the props to test
 * @param testFn a function that returns true if the props are of the expected type, false otherwise.
 * @returns a boolean and type guard for the generic TResult type provided
 */
export function arePropsOfType<TResult>(
  props: unknown,
  testFn: (p: TResult) => boolean
): props is TResult {
  return testFn(props as TResult);
}

/**
 * Type guard that checks if something is a promise or not.
 *
 * @param p something that may be a promise
 * @returns true if p is a promise
 */
export function isPromise(p): p is Promise<unknown> | PromiseLike<unknown> {
  return Boolean(typeof p === 'object' && typeof p.then === 'function');
}
