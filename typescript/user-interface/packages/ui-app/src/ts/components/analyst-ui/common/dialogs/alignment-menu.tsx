/* eslint-disable react/destructuring-assignment */
/* eslint-disable react/sort-comp */
import { Radio, RadioGroup } from '@blueprintjs/core';
import type { CommonTypes } from '@gms/common-model';
import { DropDown, FilterableOptionList } from '@gms/ui-core-components';
import { AlignWaveformsOn } from '@gms/ui-state/lib/app/state/analyst/types';
import React from 'react';

export interface AlignmentMenuProps {
  alignedOn: AlignWaveformsOn;
  phaseAlignedOn?: CommonTypes.PhaseType;
  sdPhases: CommonTypes.PhaseType[];
  prioritySdPhases?: CommonTypes.PhaseType[];
  defaultPhaseAlignment: CommonTypes.PhaseType;

  onSubmit(alignedOn: AlignWaveformsOn, sdPhase?: CommonTypes.PhaseType);
}

export interface AlignmentMenuState {
  alignedOn: AlignWaveformsOn;
  phaseAlignedOn?: CommonTypes.PhaseType;
  predictedOrObservedVal?: AlignWaveformsOn;
}

/**
 * A sub-set of the AlignWaveformsOn enum
 */
const alignmentsWithoutTime = {
  PREDICTED_PHASE: AlignWaveformsOn.PREDICTED_PHASE,
  OBSERVED_PHASE: AlignWaveformsOn.OBSERVED_PHASE
};

/**
 * How wide to render internal elements
 */
const dropdownWidthPx = 95;
const filterWidthPx = 169;

/**
 * The popup used to selected phases to align on
 */
export class AlignmentMenu extends React.Component<AlignmentMenuProps, AlignmentMenuState> {
  // If the drop down has already called onSubmit, do not call onsubmit in onBlur
  private hasBeenSubmitted = false;

  private constructor(props) {
    super(props);
    this.state = {
      alignedOn: this.props.alignedOn,
      phaseAlignedOn: this.props.phaseAlignedOn
        ? this.props.phaseAlignedOn
        : this.props.defaultPhaseAlignment,
      predictedOrObservedVal: this.props.alignedOn
    };
  }

  /**
   * React component lifecycle.
   */
  public render(): JSX.Element {
    const disablePhaseOptions = this.state.alignedOn === AlignWaveformsOn.TIME;
    return (
      <div className="alignment-dropdown">
        <RadioGroup
          onChange={this.handleRadioClick}
          selectedValue={this.state.alignedOn === AlignWaveformsOn.TIME ? 'time' : 'phase'}
        >
          {this.state.alignedOn === AlignWaveformsOn.TIME ? (
            <Radio
              label="Time"
              value="time"
              key="time"
              data-cy="time"
              className="alignment-dropdown__radio"
              autoFocus
              onClick={() => {
                this.submit();
              }}
            />
          ) : (
            <Radio
              label="Time"
              value="time"
              key="time"
              data-cy="time"
              className="alignment-dropdown__radio"
            />
          )}
          <Radio
            disabled={this.props.sdPhases.length === 0}
            label="Phase:"
            value="phase"
            key="phase"
            data-cy="phase"
            className="alignment-dropdown__radio"
          >
            <div className="alignment-dropdown__phase-type-selector">
              <DropDown
                value={this.state.predictedOrObservedVal}
                title={this.state.predictedOrObservedVal}
                dropDownItems={alignmentsWithoutTime}
                onMaybeValue={this.onPredictedOrObserved}
                disabled={disablePhaseOptions}
                widthPx={dropdownWidthPx}
              />
            </div>
          </Radio>
        </RadioGroup>
        <div className="alignment-dropdown__list-positioner">
          <FilterableOptionList
            options={this.props.sdPhases}
            onSelection={this.onPhaseSelection}
            onDoubleClick={(phase: CommonTypes.PhaseType) => {
              this.submit(phase);
            }}
            onClick={(phase: CommonTypes.PhaseType) => {
              this.submit(phase);
            }}
            priorityOptions={this.props.prioritySdPhases}
            defaultSelection={this.props.phaseAlignedOn ? this.props.phaseAlignedOn : 'P'}
            widthPx={filterWidthPx}
            disabled={disablePhaseOptions}
            onEnter={(phase: CommonTypes.PhaseType) => {
              this.submit(phase);
            }}
          />
        </div>
      </div>
    );
  }

  public componentWillUnmount(): void {
    if (!this.hasBeenSubmitted) {
      if (
        this.state.alignedOn !== this.props.alignedOn ||
        this.state.phaseAlignedOn !== this.props.phaseAlignedOn
      ) {
        this.submit(this.state.phaseAlignedOn);
      }
    }
  }

  /**
   * Returns current state of menu
   *
   * @returns AlignmentMenuState
   */
  public getState = (): AlignmentMenuState =>
    this.state.alignedOn === AlignWaveformsOn.TIME
      ? { alignedOn: this.state.alignedOn }
      : this.state;

  private readonly onPredictedOrObserved = (val: AlignWaveformsOn) => {
    this.setState({ alignedOn: val });
    this.setState({ predictedOrObservedVal: val });
  };

  private readonly handleRadioClick = (event: React.FormEvent<HTMLInputElement>) => {
    if (event.currentTarget.value === 'time') {
      this.hasBeenSubmitted = true;
      this.setState({ alignedOn: AlignWaveformsOn.TIME });
      this.props.onSubmit(AlignWaveformsOn.TIME);
    } else {
      this.setState({ alignedOn: AlignWaveformsOn.PREDICTED_PHASE });
    }
  };

  /**
   * On phase selection handler.
   */
  private readonly onPhaseSelection = (phaseSelected: CommonTypes.PhaseType) => {
    this.setState({ phaseAlignedOn: phaseSelected });
  };

  private readonly submit = (phase?: CommonTypes.PhaseType) => {
    this.hasBeenSubmitted = true;
    this.props.onSubmit(this.state.alignedOn, phase);
  };
}
