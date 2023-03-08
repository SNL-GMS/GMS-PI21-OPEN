/* eslint-disable react/destructuring-assignment */
import { Checkbox } from '@blueprintjs/core';
import { DropDown } from '@gms/ui-core-components';
import type { AnalystWorkspaceTypes } from '@gms/ui-state';
import produce from 'immer';
import isEqual from 'lodash/isEqual';
import React from 'react';

import { MagnitudeCategory, systemConfig } from '~analyst-ui/config/system-config';

export interface MagnitudeConfigurationProps {
  displayedMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes;
  setCategoryAndTypes(types: AnalystWorkspaceTypes.DisplayedMagnitudeTypes): void;
}
export interface MagnitudeConfigurationState {
  displayedMagnitudeTypes: AnalystWorkspaceTypes.DisplayedMagnitudeTypes;
}

export class MagnitudeConfiguration extends React.Component<
  MagnitudeConfigurationProps,
  MagnitudeConfigurationState
> {
  /**
   * constructor
   */
  public constructor(props: MagnitudeConfigurationProps) {
    super(props);
    this.state = {
      displayedMagnitudeTypes: props.displayedMagnitudeTypes
    };
  }

  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div className="magnitude-configuration-popover">
        <div className="magnitude-configuration-popover__dropdown">
          <DropDown
            dropDownItems={MagnitudeCategory}
            value={
              // eslint-disable-next-line no-nested-ternary
              this.isCustom()
                ? ''
                : // eslint-disable-next-line no-nested-ternary
                this.isBodyWave()
                ? MagnitudeCategory.BODY
                : this.isSurfaceWave()
                ? MagnitudeCategory.SURFACE
                : ''
            }
            custom={this.isCustom()}
            onMaybeValue={val => {
              this.setState(
                {
                  displayedMagnitudeTypes: systemConfig.displayedMagnitudesForCategory.get(val)
                },
                this.callback
              );
            }}
          />
          <div className="magnitude-configuration-popover__label">Customize Magnitude Types:</div>
          <div className="magnitude-configuration-checkboxes">
            {Object.keys(this.state.displayedMagnitudeTypes).map((key, index) => (
              <Checkbox
                // eslint-disable-next-line react/no-array-index-key
                key={index}
                label={key}
                checked={this.state.displayedMagnitudeTypes[key]}
                onClick={() => {
                  this.setState(
                    prevState =>
                      produce(prevState, draft => {
                        draft.displayedMagnitudeTypes[key] = !draft.displayedMagnitudeTypes[key];
                      }),
                    this.callback
                  );
                }}
              />
            ))}
          </div>
        </div>
      </div>
    );
  }

  private readonly callback = () =>
    this.props.setCategoryAndTypes(this.state.displayedMagnitudeTypes);

  /**
   * Returns true if the selected state is a custom configuration,
   * does not match the configuration for body wave or surface wave.
   */
  private readonly isCustom = (): boolean => !(this.isBodyWave() || this.isSurfaceWave());

  /**
   * Returns true if the selected state matches the state for body waves.
   */
  private readonly isBodyWave = (): boolean =>
    isEqual(
      this.state.displayedMagnitudeTypes,
      systemConfig.displayedMagnitudesForCategory.get(MagnitudeCategory.BODY)
    );

  /**
   * Returns true if the selected state matches the state for surface waves.
   */
  private readonly isSurfaceWave = (): boolean =>
    isEqual(
      this.state.displayedMagnitudeTypes,
      systemConfig.displayedMagnitudesForCategory.get(MagnitudeCategory.SURFACE)
    );
}
