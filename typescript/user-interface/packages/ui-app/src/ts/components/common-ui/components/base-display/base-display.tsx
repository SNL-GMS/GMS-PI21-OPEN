/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/prop-types */
import { getDataAttributesFromProps, useElementSize, useForceUpdate } from '@gms/ui-util';
import defer from 'lodash/defer';
import * as React from 'react';

import { TabContextMenu } from '~analyst-ui/common/menus/tab-context-menu';

import type { BaseDisplayContextData } from './base-display-context';
import { BaseDisplayContext } from './base-display-context';
import type { BaseDisplayProps } from './types';

const useForceUpdateOnFirstRender = () => {
  const forceUpdate = useForceUpdate();
  React.useEffect(() => {
    defer(forceUpdate);
    // !FIX ESLINT Validate and check REACT HOOK dependencies
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, []);
};

/**
 * A base display that should be at the base of all display components.
 * Adds consistent padding to each display, and exposes the width and height
 * of the display in the BaseDisplayContext.
 *
 * @param props requires a reference to the glContainer.
 * Also accepts data attributes in the form 'data-cy': 'example-component'
 */
export const BaseDisplay: React.FunctionComponent<React.PropsWithChildren<
  BaseDisplayProps
  // eslint-disable-next-line react/function-component-definition
>> = props => {
  /**
   * Base display size behavior
   */
  const [displayRef, heightPx, widthPx] = useElementSize();

  /**
   * On the very first mount, call forceUpdate so that height and width will
   * propagate to the consumers of the context.
   */
  useForceUpdateOnFirstRender();

  /**
   * the context menu handler, if provided
   */
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const { onContextMenu, tabName } = props;

  /**
   * Get any data attributes provided to this display (like data-cy attributes)
   */
  const dataAttributes = getDataAttributesFromProps(props);

  const baseDisplayContextData: BaseDisplayContextData = React.useMemo(
    () => ({
      glContainer: props.glContainer,
      widthPx: props.glContainer?.width ?? widthPx ?? 0,
      heightPx: props.glContainer?.height ?? heightPx ?? 0
    }),
    [heightPx, props.glContainer, widthPx]
  );

  return (
    <div
      className={`base-display ${props.className ?? ''}`}
      ref={ref => {
        displayRef.current = ref;
      }}
      onContextMenu={onContextMenu}
      // eslint-disable-next-line react/jsx-props-no-spreading
      {...dataAttributes}
    >
      <BaseDisplayContext.Provider value={baseDisplayContextData}>
        {tabName ? <TabContextMenu tabName={tabName} /> : null}
        {props.children}
      </BaseDisplayContext.Provider>
    </div>
  );
};
