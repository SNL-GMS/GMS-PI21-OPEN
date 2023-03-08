import {
  getRandomIndexForArray,
  getRandomInRange,
  getSecureRandomNumber
} from '../../src/ts/common-util/random-number-util';

const testValue = 20;

describe('GMS Random', () => {
  test('should return random number 1', () => {
    const randomNumber = getSecureRandomNumber();
    expect(randomNumber).toBeDefined();
    expect(randomNumber).toBeGreaterThan(0);
    expect(randomNumber).toBeGreaterThanOrEqual(0);
    expect(randomNumber).toBeLessThan(1);
    expect(randomNumber).toBeLessThanOrEqual(1);
  });

  const randomArray: number[] = [];
  for (let i = 0; i < testValue; i += 1) {
    const r: number = getSecureRandomNumber();
    randomArray.push(r);
  }

  test('should return random number 2', () => {
    const randomNumber = getSecureRandomNumber();
    expect(randomNumber).toBeDefined();
    expect(randomNumber).toBeGreaterThanOrEqual(0);
    expect(randomNumber).toBeLessThan(1);
  });

  test('get random in range', () => {
    const lowerRange = 0;
    const upperRange = 5;
    const repeatLimit = 20;
    let i = 0;
    for (i; i < repeatLimit; i += 1) {
      const randomNumber = getRandomInRange(lowerRange, upperRange, 1);
      expect(randomNumber).toBeDefined();
      expect(randomNumber).toBeGreaterThanOrEqual(lowerRange);
      expect(randomNumber).toBeLessThanOrEqual(upperRange);
    }
  });

  test('get random for array', () => {
    const arrayLength = 5;
    const repeatLimit = 20;
    let i = 0;
    for (i; i < repeatLimit; i += 1) {
      const randomNumber = getRandomIndexForArray(arrayLength);
      expect(randomNumber).toBeDefined();
      expect(randomNumber).toBeGreaterThanOrEqual(0);
      expect(randomNumber).toBeLessThanOrEqual(arrayLength - 1);
    }
  });
});
