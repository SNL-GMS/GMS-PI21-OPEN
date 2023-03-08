/* eslint-disable react/prop-types */
/* eslint-disable react/destructuring-assignment */
import { Radio, RadioGroup, Switch } from '@blueprintjs/core';
import type { CheckboxSearchListTypes } from '@gms/ui-core-components';
import { CheckboxSearchList } from '@gms/ui-core-components';
import cloneDeep from 'lodash/cloneDeep';
import React from 'react';

import { FkUnits } from '~analyst-ui/components/azimuth-slowness/types';

export interface FkConfigurationDefaultPanelProps {
  normalizeWaveforms: boolean;
  channelCheckboxes: CheckboxSearchListTypes.CheckboxItem[];
  fkUnits: FkUnits;
  handleFkUnitsChange(val: any);
  setNormalizeWaveforms(val: boolean);
  setChannelEnabledById(id: string, val: boolean);
}

// Max height of channels list, hard coded for now
const MAX_HEIGHT_LIST_PX = 130;

/**
 * FkConfigurationDefaultPanel component.
 */
// eslint-disable-next-line react/function-component-definition
export const FkConfigurationDefaultPanel: React.FunctionComponent<FkConfigurationDefaultPanelProps> = props => {
  const channelTrackers = cloneDeep(props.channelCheckboxes);
  channelTrackers.sort((a, b) => {
    const nameA = a.name.toLowerCase();
    const nameB = b.name.toLowerCase();
    return nameA.localeCompare(nameB);
  });
  return (
    <div className="continous-fk-popover-panel">
      <div className="popover-section">
        <div className="popover-header">
          <div className="popover-header__text">FK Units</div>
          <div className="popover-header__widget">
            <RadioGroup
              className="fk-units-picker__radio-group"
              onChange={(event: React.FormEvent<HTMLInputElement>) => {
                props.handleFkUnitsChange(event.currentTarget.value);
              }}
              selectedValue={props.fkUnits}
            >
              <Radio
                label="Fstat"
                value={FkUnits.FSTAT}
                style={{ marginRight: '20px', marginBottom: '0px' }}
              />
              <Radio label="Power" value={FkUnits.POWER} style={{ marginBottom: '0px' }} />
            </RadioGroup>
          </div>
        </div>
      </div>
      <div className="popover-section">
        <div className="popover-header">
          <div className="popover-header__text">Normalize Channels</div>
          <div className="popover-header__widget">
            <Switch
              checked={props.normalizeWaveforms}
              onChange={() => {
                props.setNormalizeWaveforms(!props.normalizeWaveforms);
              }}
              style={{ marginBottom: '0px' }}
            />
          </div>
        </div>
        <div className="popover-body-text">
          Controls whether channel waveforms are normalized prior to the FK calculation
        </div>
      </div>
      <div className="popover-section">
        <div className="popover-header">
          <div className="popover-header__text">Contributing Channels</div>
        </div>
        <div className="popover-body-text">
          Controls which channel waveforms contribute to the fk calculation
          <br />
          <CheckboxSearchList
            items={channelTrackers}
            onCheckboxChecked={(id: string, val: boolean) => {
              props.setChannelEnabledById(id, val);
            }}
            maxHeightPx={MAX_HEIGHT_LIST_PX}
          />
        </div>
      </div>
    </div>
  );
};
