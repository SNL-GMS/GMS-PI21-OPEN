import React from 'react';
import { CustomDataSource } from 'resium';

import { mapIanEntitiesToEntityComponent } from '~analyst-ui/components/map/ian-map-utils';
import type { IanMapDataSourceProps } from '~analyst-ui/components/map/types';

/**
 * Creates a CustomDataSource to add to the cesium map by converting an array of entities into entity components
 * and spreading them into the DataSource
 *
 * @param props
 */
// eslint-disable-next-line react/function-component-definition
export const IanMapDataSource: React.FunctionComponent<IanMapDataSourceProps> = (
  props: IanMapDataSourceProps
): JSX.Element => {
  const {
    entities,
    leftClickHandler,
    rightClickHandler,
    doubleClickHandler,
    name,
    onMount,
    show
  } = props;
  const entityComponents = mapIanEntitiesToEntityComponent(
    entities,
    leftClickHandler,
    rightClickHandler,
    doubleClickHandler,
    onMount
  );
  return (
    <CustomDataSource name={name} show={show}>
      {...entityComponents}
    </CustomDataSource>
  );
};
