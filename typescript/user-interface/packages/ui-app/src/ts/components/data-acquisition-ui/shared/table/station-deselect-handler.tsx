import * as React from 'react';

export interface StationDeselectHandlerProps {
  className?: string;
  dataCy?: string;
  setSelectedStationIds(ids: string[]): void;
}

// eslint-disable-next-line @typescript-eslint/explicit-module-boundary-types
export const keyDown = (
  e: React.KeyboardEvent<HTMLDivElement>,
  props: StationDeselectHandlerProps
) => {
  if (e.nativeEvent.code === 'Escape') {
    props.setSelectedStationIds([]);
  }
};

export function StationDeselectHandler(
  props: React.PropsWithChildren<StationDeselectHandlerProps>
) {
  const { className, dataCy, children } = props;
  return (
    // eslint-disable-next-line jsx-a11y/no-static-element-interactions
    <div
      className={`deselect-handler ${className ?? ''}`}
      onKeyDown={e => keyDown(e, props)}
      data-cy={dataCy}
    >
      {children}
    </div>
  );
}
