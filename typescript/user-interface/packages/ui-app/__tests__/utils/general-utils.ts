export const testPermutationsUndefined = (testFunc, params: any[]): void => {
  params.forEach((param, index) => {
    const customParams = [...params];
    customParams[index] = undefined;
    expect(testFunc(customParams)).toBeUndefined();
  });
};

export const testPermutationsFalsy = (testFunc, params: any[]): void => {
  params.forEach((param, index) => {
    const customParams = [...params];
    customParams[index] = undefined;
    expect(testFunc(customParams)).toBeFalsy();
  });
};
