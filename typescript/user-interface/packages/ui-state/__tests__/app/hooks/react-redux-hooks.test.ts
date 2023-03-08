import { useAppDispatch, useAppSelector } from '../../../src/ts/app/hooks';

describe('react redux hooks', () => {
  it('exists', () => {
    expect(useAppDispatch).toBeDefined();
    expect(useAppSelector).toBeDefined();
  });
});
