import type { IconName, Intent } from '@blueprintjs/core';
import type { WeavessTypes } from '@gms/weavess-core';
import type * as React from 'react';

export interface MeasureWindowSelectionListenerProps {
  displayInterval: WeavessTypes.TimeRange;
  offsetSecs: number;
  hotKeys: WeavessTypes.HotKeysConfiguration;
  isMeasureWindowEnabled(): boolean;
  children(measureWindowParams: {
    contentRenderer: JSX.Element;
    onMouseDown: (e: React.MouseEvent<HTMLDivElement>) => void;
  }): JSX.Element | null;
  toast(
    message: string,
    intent?: Intent | undefined,
    icon?: IconName | undefined,
    timeout?: number | undefined
  ): void;
  updateMeasureWindowPanel(
    timeRange: WeavessTypes.TimeRange,
    removeMeasureWindowSelection: () => void
  ): void;
  computeTimeSecsFromMouseXPixels(mouseXPx: number): number;
}
