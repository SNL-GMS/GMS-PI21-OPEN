import { H5 } from '@blueprintjs/core';
import { SimpleCheckboxList } from '@gms/ui-core-components';
import React from 'react';

import type { MapLayerSettingsPopoutProps } from '~analyst-ui/components/map/types';

/**
 * Used to construct the layer settings popout content on the ian map
 *
 * @param props
 * @constructor
 */
// eslint-disable-next-line react/function-component-definition
export const MapLayerSettingsPopout: React.FunctionComponent<MapLayerSettingsPopoutProps> = (
  props: MapLayerSettingsPopoutProps
) => {
  const { settingsEntries, onCheckedCallback } = props;
  return (
    <div className="layer-panel-content__container">
      <div className="layer-panel-content--header">
        <H5>Layer Settings</H5>
      </div>
      <div className="layer-panel-content--checkbox-list">
        <SimpleCheckboxList checkBoxListEntries={settingsEntries} onChange={onCheckedCallback} />
      </div>
    </div>
  );
};
