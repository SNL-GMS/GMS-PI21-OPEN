import {
  arePropsOfType,
  isArray,
  isArrayOfObjects,
  isNumber,
  isObject,
  isObjectDefined,
  isString
} from '../../src/ts/common-util';

describe('type utils', () => {
  interface TestType {
    someParam: string;
    anotherParam: number;
  }

  it('can check if type is an array', () => {
    expect(isArray([])).toBeTruthy();
    expect(isArray([1, 2, 3])).toBeTruthy();

    expect(isArray(undefined)).toBeFalsy();
    expect(isArray({})).toBeFalsy();
    expect(isArray('string')).toBeFalsy();
    expect(isArray(1)).toBeFalsy();
  });

  it('can check if type is an object', () => {
    expect(isObject({})).toBeTruthy();
    expect(isObject({ value: 2 })).toBeTruthy();

    expect(isObject([])).toBeFalsy();
    expect(isObject([1, 2, 3])).toBeFalsy();
    expect(isObject(undefined)).toBeFalsy();
    expect(isObject('string')).toBeFalsy();
    expect(isObject(1)).toBeFalsy();
  });

  it('can check if type is an string', () => {
    expect(isString('')).toBeTruthy();
    expect(isString('string')).toBeTruthy();

    expect(isString(undefined)).toBeFalsy();
    expect(isString({})).toBeFalsy();
    expect(isString([])).toBeFalsy();
    expect(isString([1, 2, 3])).toBeFalsy();
    expect(isString(1)).toBeFalsy();
  });

  it('can check if type is an number', () => {
    expect(isNumber(1)).toBeTruthy();

    expect(isNumber(undefined)).toBeFalsy();
    expect(isNumber({})).toBeFalsy();
    expect(isNumber([])).toBeFalsy();
    expect(isNumber([1, 2, 3])).toBeFalsy();
    expect(isNumber('string')).toBeFalsy();
  });

  it('can check if type is an array of objects', () => {
    expect(isArrayOfObjects([{}, { a: true }])).toBeTruthy();

    expect(isArrayOfObjects([{}, 1, 2, 3])).toBeFalsy();
    expect(isArrayOfObjects(undefined)).toBeFalsy();
    expect(isArrayOfObjects([])).toBeFalsy();
    expect(isArrayOfObjects({})).toBeFalsy();
    expect(isArrayOfObjects('string')).toBeFalsy();
    expect(isArrayOfObjects(1)).toBeFalsy();
  });

  it('check is an object defined function', () => {
    expect(isObjectDefined(undefined)).toBeFalsy();
    expect(isObjectDefined(null)).toBeFalsy();
    expect(isObjectDefined(0)).toBeTruthy();
    expect(isObjectDefined([])).toBeTruthy();
    expect(isObjectDefined({})).toBeTruthy();
  });

  it('can tell if props are of a given type', () => {
    const validProps = {
      someParam: 'this is a string',
      anotherParam: 1
    };
    const testFn = (p: TestType) =>
      typeof p.someParam === 'string' && typeof p.anotherParam === 'number';
    expect(arePropsOfType<TestType>(validProps, testFn)).toBe(true);
  });

  it('can tell if props are not of a given type', () => {
    const invalidProps = {
      someParam: 'this is a string',
      anotherParam: 'this is also a string'
    };
    const testFn = (p: TestType) =>
      typeof p.someParam === 'string' && typeof p.anotherParam === 'number';
    expect(arePropsOfType<TestType>(invalidProps, testFn)).toBe(false);
  });
});
