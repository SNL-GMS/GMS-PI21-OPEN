/* eslint-disable react/destructuring-assignment */
import { CommonTypes } from '@gms/common-model';
import { FilterableOptionList } from '@gms/ui-core-components';
import React from 'react';

/**
 * How wide to render internal elements
 */
const widthPx = 160;

export interface PhaseSelectionMenuProps {
  phase?: CommonTypes.PhaseType;
  sdPhases: CommonTypes.PhaseType[];
  prioritySdPhases?: CommonTypes.PhaseType[];
  onBlur(phase: CommonTypes.PhaseType);
  onEnterForPhases?(phase: CommonTypes.PhaseType);
  onPhaseClicked?(phase: CommonTypes.PhaseType);
}

export interface PhaseSelectionMenuState {
  phase: CommonTypes.PhaseType;
}

/**
 * Phase selection menu.
 */
export class PhaseSelectionMenu extends React.Component<
  PhaseSelectionMenuProps,
  PhaseSelectionMenuState
> {
  private constructor(props) {
    super(props);
    this.state = {
      // eslint-disable-next-line react/no-unused-state
      phase: this.props.phase ? this.props.phase : CommonTypes.PhaseType.P
    };
  }

  /**
   * React component lifecycle.
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div className="alignment-dropdown">
        <FilterableOptionList
          options={this.props.sdPhases}
          onSelection={this.onPhaseSelection}
          onClick={this.onClick}
          onDoubleClick={this.onClick}
          priorityOptions={this.props.prioritySdPhases}
          defaultSelection={this.props.phase}
          widthPx={widthPx}
          onEnter={this.props.onEnterForPhases}
        />
      </div>
    );
  }

  /**
   * Returns current state of menu
   *
   * @returns PhaseSelectionMenuState
   */
  public getState = (): PhaseSelectionMenuState => this.state;

  /**
   * On phase selection event handler.
   *
   * @param phase the selected phase
   */
  private readonly onPhaseSelection = (phase: CommonTypes.PhaseType) => {
    // eslint-disable-next-line react/no-unused-state
    this.setState({ phase });
  };

  private readonly onClick = (phase: CommonTypes.PhaseType) => {
    if (this.props.onPhaseClicked) {
      this.props.onPhaseClicked(phase);
    } else {
      // eslint-disable-next-line react/no-unused-state
      this.setState({ phase });
    }
  };
}
