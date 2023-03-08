import { SohTypes } from '@gms/common-model';
import * as React from 'react';

import { messageConfig } from '~components/data-acquisition-ui/config/message-config';

/* eslint-disable @typescript-eslint/no-require-imports, @typescript-eslint/no-var-requires */
const badBadge = require('./resources/status-bad.svg');
const goodBadge = require('./resources/status-good.svg');
const marginalBadge = require('./resources/status-marginal.svg');
/* eslint-enable @typescript-eslint/no-require-imports, @typescript-eslint/no-var-requires */

const badgeTooltipMsg = messageConfig.tooltipMessages.stationStatistics.badge;

const badBadgeImage = (
  <img
    className="badge"
    width="17.583"
    height="23"
    src={badBadge}
    title={`${badgeTooltipMsg}BAD`}
    alt="BAD"
  />
);
const goodBadgeImage = (
  <img
    className="badge"
    width="20"
    height="20"
    src={goodBadge}
    title={`${badgeTooltipMsg}GOOD`}
    alt="GOOD"
  />
);
const marginalBadgeImage = (
  <img
    className="badge"
    width="18.996"
    height="16.88"
    src={marginalBadge}
    title={`${badgeTooltipMsg}MARGINAL`}
    alt="MARGINAL"
  />
);

export interface WorstOfBadgeProps {
  worstOfSohStatus: SohTypes.SohStatusSummary;
  widthPx: number;
}

export interface WorstOfImageProps {
  worstOfSohStatus: SohTypes.SohStatusSummary;
}

export function WorstOfImage({ worstOfSohStatus }: WorstOfImageProps) {
  let badgeToUse;
  if (worstOfSohStatus === SohTypes.SohStatusSummary.BAD) {
    badgeToUse = badBadgeImage;
  } else if (worstOfSohStatus === SohTypes.SohStatusSummary.GOOD) {
    badgeToUse = goodBadgeImage;
  } else if (worstOfSohStatus === SohTypes.SohStatusSummary.MARGINAL) {
    badgeToUse = marginalBadgeImage;
  }
  return badgeToUse ?? null;
}

const shouldShowBadge = (worstOfSohStatus: SohTypes.SohStatusSummary) =>
  worstOfSohStatus === SohTypes.SohStatusSummary.BAD ||
  worstOfSohStatus === SohTypes.SohStatusSummary.MARGINAL;

/**
 * Selects which badge to use for group/station based on status. A status of "NONE" shows no badge.
 *
 * @param worstOfSohStatus - the status of "GOOD," "BAD," or "MARGINAL"
 */
export function WorstOfBadge(props: WorstOfBadgeProps) {
  const { worstOfSohStatus, widthPx } = props;
  return (
    shouldShowBadge(worstOfSohStatus) && (
      <div
        className="soh-cell__right-container"
        data-capability-status={worstOfSohStatus.toLowerCase()}
        style={{ width: widthPx }}
        data-cy="soh-worst-of-badge"
      >
        <WorstOfImage worstOfSohStatus={worstOfSohStatus} />
      </div>
    )
  );
}
