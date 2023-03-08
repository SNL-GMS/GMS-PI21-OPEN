/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { classList } from '@gms/ui-util';
import * as React from 'react';

import type { ResizeContainerProps } from '.';
import type { ResizeData } from './resize-context';
import { ResizeContext } from './resize-context';

const defaultHeightPx = 360;
export const ResizeContainer: React.FunctionComponent<React.PropsWithChildren<
  ResizeContainerProps
  // eslint-disable-next-line react/function-component-definition
>> = props => {
  const [height, setHeight] = React.useState(defaultHeightPx);
  const [isResizing, setIsResizing] = React.useState(false);
  const containerRef = React.useRef<HTMLDivElement>();

  const resizeContextData: ResizeData = React.useMemo(
    () => ({
      isResizing,
      height,
      containerHeight: containerRef?.current?.clientHeight,
      setIsResizing,
      setHeight
    }),
    [height, isResizing]
  );

  return (
    <div
      className={classList(
        {
          'resize-container': true,
          'resize-container--resizing': isResizing
        },
        props.className
      )}
      data-cy={props.dataCy}
      ref={containerRef}
    >
      <ResizeContext.Provider value={resizeContextData}>{props.children}</ResizeContext.Provider>
    </div>
  );
};
