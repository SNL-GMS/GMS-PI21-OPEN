/**
 * A list of classes (as keys) to boolean values.
 * Classes can be toggled on and off by flipping the boolean.
 */
export interface ClassList {
  [className: string]: boolean;
}

/**
 * Used to generate a browser-ready string containing the classes
 * (keys) that are set to true
 *
 * @param classDefinitions a set of class definitions to process
 * @param cl
 * @param additionalClasses
 * @returns a space-separated string of the classNames that are
 * set to true
 */
export const classList = (cl: ClassList, additionalClasses?: string): string =>
  Object.keys(cl)
    .filter(className => cl[className])
    .join(' ') + (additionalClasses ? ` ${additionalClasses}` : '');
