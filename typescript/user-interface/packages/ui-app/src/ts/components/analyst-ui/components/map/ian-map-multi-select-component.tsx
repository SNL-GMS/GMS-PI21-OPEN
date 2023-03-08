import { KeyboardEventModifier, ScreenSpaceEventType } from 'cesium';
import * as React from 'react';
import { Entity, ScreenSpaceEvent, ScreenSpaceEventHandler } from 'resium';

import type { MapHandlerProps } from '~common-ui/components/map/types';

// eslint-disable-next-line react/function-component-definition
export const IanMapMultiSelectComponent: React.FC<MapHandlerProps> = () => {
  const [showSelector, setShowSelector] = React.useState(false);

  return (
    <>
      <ScreenSpaceEventHandler>
        <ScreenSpaceEvent
          type={ScreenSpaceEventType.MOUSE_MOVE}
          modifier={KeyboardEventModifier.ALT}
        />
        <ScreenSpaceEvent
          type={ScreenSpaceEventType.LEFT_DOWN}
          modifier={KeyboardEventModifier.ALT}
        />
        <ScreenSpaceEvent
          type={ScreenSpaceEventType.LEFT_UP}
          modifier={KeyboardEventModifier.ALT}
        />
        <ScreenSpaceEvent
          action={() => setShowSelector(false)}
          type={ScreenSpaceEventType.LEFT_CLICK}
        />
      </ScreenSpaceEventHandler>
      <Entity id="selectionEntity" show={showSelector} />
    </>
  );
};
