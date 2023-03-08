/* eslint-disable react/destructuring-assignment */
import React from 'react';

import { FilterableOptionList } from '../components';

/**
 * Example of using the form that actually accepts input
 */

interface FilterableOptionListExampleState {
  selectedOptionA: string;
  selectedOptionB: string;
}

const options = ['option A', 'option B', 'option C', 'option D', 'option E'];

/**
 * Example displaying how to use the Table component.
 */
export class FilterableOptionListExample extends React.Component<
  unknown,
  FilterableOptionListExampleState
> {
  public constructor(props: unknown) {
    super(props);
    this.state = {
      selectedOptionA: '',
      selectedOptionB: 'option A'
    };
  }

  /**
   * React render method
   */
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div
        className="ag-dark"
        style={{
          flex: '1 1 auto',
          position: 'relative',
          width: '700px'
        }}
      >
        <FilterableOptionList options={options} onSelection={this.onChangeA} />
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`Selected Option: ${this.state.selectedOptionA}`}
          <br />
        </div>
        <FilterableOptionList
          options={options}
          priorityOptions={['Option A', 'Option B']}
          onSelection={this.onChangeB}
          defaultFilter="c"
          defaultSelection="Option C"
        />
        <div style={{ color: '#D7B740', fontFamily: 'monospace' }}>
          {`Selected Option: ${this.state.selectedOptionB}`}
          <br />
        </div>
      </div>
    );
  }

  private readonly onChangeA = (selectedOption: string) => {
    this.setState({ selectedOptionA: selectedOption });
  };

  private readonly onChangeB = (selectedOption: string) => {
    this.setState({ selectedOptionB: selectedOption });
  };
}
