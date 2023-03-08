/**
 * Finds all parameters that start with data-, and returns them
 *
 * @param props the props to search for data-* params
 * @returns an object containing 0 or more key-value pairs of the form:
 *  data-*: value
 * For example:
 *   { 'data-cy': 'example-component' }
 */
export const getDataAttributesFromProps = (
  // eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types, @typescript-eslint/no-explicit-any
  props: any
): {
  [name: string]: string;
} =>
  Object.keys(props)
    .filter(propName => propName.startsWith('data-'))
    .reduce((result: { [name: string]: string }, key) => {
      // eslint-disable-next-line no-param-reassign
      result[key] = props[key];
      return result;
    }, {});
