/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { calculatePercentTimeRemaining, MILLISECONDS_IN_HALF_SECOND } from '@gms/common-util';
import { PieChart, TooltipWrapper } from '@gms/ui-core-components';
import { useActionEveryInterval, useForceUpdate } from '@gms/ui-util';
import * as React from 'react';

import { createTooltipMessage } from './quiet-indicator-utils';
import type { QuietIndicatorProps, QuietIndicatorWithTooltipProps, QuietTimingInfo } from './types';

const quietTimerIsActive = (quietTimingInfo: QuietTimingInfo) =>
  Date.now() < quietTimingInfo.quietUntilMs;

function BaseQuietIndicator(props: QuietIndicatorProps) {
  const { className, pieSliceClass, ...rest } = props;
  return (
    <PieChart
      className={`quiet-indicator ${className ?? ''} ${
        props.percent <= 0 ? 'quiet-indicator--expired' : ''
      }`}
      pieSliceClass={`${pieSliceClass} quiet-indicator__pie-slice`}
      // eslint-disable-next-line react/jsx-props-no-spreading
      {...rest}
      data-timer-status={props.status}
    />
  );
}

/**
 *
 * @param diameterPx the diameter, in px, of the indicator background
 * @param percent the percent, from 0 to 1.
 */
// eslint-disable-next-line react/display-name
export const QuietIndicator = React.memo((props: QuietIndicatorWithTooltipProps) => {
  const { quietTimingInfo, ...rest } = props;
  const forceUpdate = useForceUpdate();
  useActionEveryInterval(forceUpdate, MILLISECONDS_IN_HALF_SECOND);
  const message = createTooltipMessage(quietTimingInfo?.quietUntilMs);
  const percent = calculatePercentTimeRemaining(
    quietTimingInfo.quietUntilMs,
    quietTimingInfo.quietDurationMs
  );
  return quietTimerIsActive(quietTimingInfo) ? (
    <TooltipWrapper
      content={message}
      className="quiet-indicator-tooltip"
      targetClassName="quiet-indicator-tooltip__target"
    >
      <>
        {/* eslint-disable-next-line react/jsx-props-no-spreading */}
        <BaseQuietIndicator percent={percent} {...rest} />
      </>
    </TooltipWrapper>
  ) : null;
});
