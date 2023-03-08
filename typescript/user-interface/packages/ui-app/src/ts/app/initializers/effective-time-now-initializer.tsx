import { MILLISECONDS_IN_SECOND } from '@gms/common-util';
import { analystActions, useAppDispatch, useAppSelector } from '@gms/ui-state';

/**
 * take the results of the effectiveNowTime and see if it set. If it is not, then we set it to now
 */
export function EffectiveNowTimeInitializer() {
  const effectiveNowTime = useAppSelector(state => state.app.analyst?.effectiveNowTime);
  const dispatch = useAppDispatch();
  if (!effectiveNowTime) {
    dispatch(analystActions.setEffectiveNowTime(Date.now() / MILLISECONDS_IN_SECOND));
  }
  return null;
}
