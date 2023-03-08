/* eslint-disable react/prop-types */
import React from 'react';

import { MaybeQuietIndicator } from '~components/data-acquisition-ui/components/soh-environment/cell-renderers/maybe-quiet-indicator';

export class QuietIndicatorWrapper extends React.PureComponent {
  public render(): JSX.Element {
    const { height, datum, x, y } = this.props as any;
    const diameterPx = Math.round(datum.barWidth / 3);
    const quietData: any = {
      quietTimingInfo: {
        quietUntilMs: datum.quietUntilMs,
        quietDurationMs: datum.quietDurationMs
      },
      status: datum.channelStatus
    };
    // centers quiet timer on the bar
    const offset = 105;
    // bar height is height of entire chart minus y value which is distance from top of chart down minus padding offset
    const barHeight = height - y - offset;
    // need to add a constant for small bars as a buffer
    const PADDING_ABOVE = 30;
    const SMALL_PADDING = 15;
    // If the bar is too small for quiet timer, translate the timer above the bar.
    const renderTimerAboveBar: boolean = diameterPx * 4 + PADDING_ABOVE > barHeight;
    const translate = renderTimerAboveBar
      ? // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
        `translate(${x - offset}, ${y - diameterPx - SMALL_PADDING})`
      : // eslint-disable-next-line @typescript-eslint/restrict-plus-operands
        `translate(${x - offset}, ${y + SMALL_PADDING})`;
    return quietData.quietTimingInfo.quietUntilMs > 0 ? (
      <g transform={translate}>
        <foreignObject
          x="75"
          y="0"
          width="60"
          height="60"
          onContextMenu={e => datum.onContextMenus.onContextMenuBar(e, { datum })}
        >
          {/*
            https://developer.mozilla.org/en-US/docs/Web/SVG/Element/foreignObject
            In the context of SVG embedded in an HTML document, the XHTML
            namespace could be omitted, but it is mandatory in the
            context of an SVG document
          */}
          <MaybeQuietIndicator
            data={quietData}
            diameterPx={diameterPx}
            className={renderTimerAboveBar ? `quiet-indicator--above` : `quiet-indicator--inside`}
          />
        </foreignObject>
      </g>
    ) : null;
  }
}
