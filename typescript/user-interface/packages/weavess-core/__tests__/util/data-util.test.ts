/* eslint-disable @typescript-eslint/no-magic-numbers */
import { createStepPoints } from '../../src/ts/util';

describe('WEAVESS Core: Data Util', () => {
  it('has the expected createStepPoints function', () => {
    expect(createStepPoints).toBeDefined();
  });
  const values = [
    [0, 0],
    [1, 1],
    [2, 1],
    [3, 1],
    [4, 1],
    [5, 1],
    [6, 0]
  ];
  it('creates a step result that matches a snapshot', () => {
    const result = createStepPoints(values);
    expect(result).toMatchSnapshot();
  });

  it('can tell if there is a step', () => {
    const result = createStepPoints(values);
    expect(result.values[0]).toHaveProperty('value', 0);
    expect(result.values[1]).toHaveProperty('value', 1);
  });

  it('can tell if there is not a step', () => {
    const result = createStepPoints(values);
    expect(result.values[1]).toHaveProperty('value', 1);
    expect(result.values[2]).toHaveProperty('value', 1);
  });
});
