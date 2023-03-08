/* eslint-disable react/prop-types */
import {
  classList,
  HighlightVisualState,
  useHighlightManager,
  useMouseUpListenerBySelector
} from '@gms/ui-util';
import * as React from 'react';

import type { DragHandleDividerProps } from './types';

// eslint-disable-next-line react/function-component-definition
export const DragHandleDivider: React.FunctionComponent<DragHandleDividerProps> = ({
  handleHeight,
  onDrag
}) => {
  const highlightManager = useHighlightManager();
  const isHighlighted = highlightManager.getVisualState();
  useMouseUpListenerBySelector('html', () => highlightManager.onMouseOut());
  return (
    // eslint-disable-next-line jsx-a11y/mouse-events-have-key-events, jsx-a11y/no-static-element-interactions
    <div
      className={classList({
        'horizontal-divider': true,
        'horizontal-divider--highlighted': isHighlighted === HighlightVisualState.HIGHLIGHTED,
        'horizontal-divider--hint': isHighlighted === HighlightVisualState.REVEALED
      })}
      onMouseDown={e => {
        highlightManager.onMouseDown();
        onDrag(e);
      }}
      onMouseUp={() => highlightManager.onMouseUp()}
      onMouseOver={() => highlightManager.onMouseOver()}
      onMouseOut={() => highlightManager.onMouseOut()}
    >
      <div className="horizontal-divider__target" />
      <div className="horizontal-divider__spacer" />
      <svg
        className="resize-handle"
        viewBox={`0 0 18 ${handleHeight}`}
        width="18"
        height={`${handleHeight}`}
      >
        <title>Resize</title>
        <g className="resize-handle-group" data-name="resize-handle-group">
          <rect width="2" height={`${handleHeight}`} rx="1" />
          <rect x="4" width="2" height={`${handleHeight}`} rx="1" />
          <rect x="8" width="2" height={`${handleHeight}`} rx="1" />
          <rect x="12" width="2" height={`${handleHeight}`} rx="1" />
          <rect x="16" width="2" height={`${handleHeight}`} rx="1" />
        </g>
      </svg>
      <div className="horizontal-divider__spacer" />
    </div>
  );
};
