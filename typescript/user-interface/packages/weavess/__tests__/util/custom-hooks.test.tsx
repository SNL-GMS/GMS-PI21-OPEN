import { useFollowMouse } from '../../src/ts/util/custom-hooks';
import { renderHook } from './render-hook-util';

describe('Custom Hooks', () => {
  it('exists', () => {
    expect(useFollowMouse).toBeDefined();
  });

  it('useFollowMouse', () => {
    const ref = renderHook(() => useFollowMouse());
    expect(ref).toBeDefined();
  });
});
