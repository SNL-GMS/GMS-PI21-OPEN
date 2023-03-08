/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import type { HighlightManager } from '@gms/ui-util';
import { classList, HighlightVisualState, useHighlightManager } from '@gms/ui-util';
import throttle from 'lodash/throttle';
import * as React from 'react';

import type { ResizeHandleProps } from './types';
import { ResizeHandleOrientation } from './types';

// The number of ms between mouse move event calls
const MOUSE_MOVE_EVENT_FREQUENCY_MS = 32;

/**
 * Props for resize orientation.
 * eg: BOTTOM, RIGHT...
 */
interface ResizeOrientationProps {
  orientation: ResizeHandleOrientation;
}

/**
 * Props for DragSensor. Agnostic of orientation.
 */
interface DragSensorProps {
  highlightManager: HighlightManager;
}

/**
 * Generic Drag Sensor that is provided with an orientation.
 * DragSensor creates an invisible area around the ResizeHandle
 * that provides a larger touch target to click and drag.
 * DragSensor responds to the user interactions.
 */
const DragSensor: React.FunctionComponent<
  ResizeHandleProps & DragSensorProps & ResizeOrientationProps
  // eslint-disable-next-line react/function-component-definition
> = props => (
  // eslint-disable-next-line jsx-a11y/mouse-events-have-key-events, jsx-a11y/no-static-element-interactions
  <div
    className={classList({
      resizer__sensor: true,
      'resizer__sensor--bottom': props.orientation === ResizeHandleOrientation.BOTTOM
    })}
    onMouseDown={(e: React.MouseEvent<HTMLDivElement>) => {
      e.preventDefault();
      props.onResizeStart();
      const onMouseMove = throttle(
        props.handleMouseMove({ x: e.pageX, y: e.pageY }),
        MOUSE_MOVE_EVENT_FREQUENCY_MS
      );
      const resizeEnd = () => {
        document.body.removeEventListener('mousemove', onMouseMove);
        document.body.removeEventListener('mouseup', resizeEnd);
        props.highlightManager.onMouseUp();
        if (props.onResizeEnd) {
          props.onResizeEnd();
        }
      };
      document.body.addEventListener('mousemove', onMouseMove);
      document.body.addEventListener('mouseup', resizeEnd);
      props.highlightManager.onMouseDown();
    }}
    onMouseOver={() => props.highlightManager.onMouseOver()}
    onMouseOut={() => props.highlightManager.onMouseOut()}
  />
);
/**
 * Resize Handle is the visual handle that shows where to click to drag, and changes styles to indicate
 * the current state (hovered, resizing, default)
 */
// eslint-disable-next-line react/function-component-definition
const ResizeHandle: React.FunctionComponent<ResizeHandleProps & ResizeOrientationProps> = props => {
  const highlightManager = useHighlightManager();
  const visualState = highlightManager.getVisualState();
  const { orientation } = props;
  return (
    <>
      <div
        className={classList({
          resizer__handle: true,
          'resizer__handle--height': orientation === ResizeHandleOrientation.BOTTOM,
          'resizer__handle--width': orientation === ResizeHandleOrientation.RIGHT,
          'resizer__handle--bottom': orientation === ResizeHandleOrientation.BOTTOM,
          'resizer__handle--highlighted': visualState === HighlightVisualState.HIGHLIGHTED,
          'resizer__handle--hint': visualState === HighlightVisualState.REVEALED
        })}
      />
      {/* eslint-disable-next-line react/jsx-props-no-spreading */}
      <DragSensor highlightManager={highlightManager} orientation={orientation} {...props} />
    </>
  );
};

export const BottomResizeHandle: React.FunctionComponent<ResizeHandleProps> = React.memo(props => (
  // eslint-disable-next-line react/jsx-props-no-spreading
  <ResizeHandle orientation={ResizeHandleOrientation.BOTTOM} {...props} />
));
