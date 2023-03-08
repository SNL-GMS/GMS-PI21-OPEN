/* eslint-disable react/destructuring-assignment */
import { Checkbox, HTMLTable } from '@blueprintjs/core';
import React from 'react';

import type { MaskDisplayFilter } from '~analyst-ui/config/user-preferences';

import type { QcMaskFilterProps } from './types';

export class QcMaskFilter extends React.Component<QcMaskFilterProps> {
  // eslint-disable-next-line react/sort-comp
  public render(): JSX.Element {
    return (
      <div>
        <HTMLTable className="qc-mask-legend__body">
          <tbody>
            {Object.keys(this.props.maskDisplayFilters).map(key => (
              <tr key={key}>
                <td>
                  <Checkbox
                    className="qc-mask-legend-table__checkbox"
                    defaultChecked={this.props.maskDisplayFilters[key].visible}
                    onChange={() => this.onChange(key, this.props.maskDisplayFilters[key])}
                  />
                </td>
                <td>{this.props.maskDisplayFilters[key].name}</td>
                <td>
                  <div
                    className="qc-mask-legend-table__legend-box"
                    style={{
                      backgroundColor: this.props.maskDisplayFilters[key].color
                    }}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </HTMLTable>
      </div>
    );
  }

  private readonly onChange = (key: string, maskDisplayFilter: MaskDisplayFilter) => {
    // eslint-disable-next-line no-param-reassign
    maskDisplayFilter.visible = !maskDisplayFilter.visible;
    this.props.setMaskDisplayFilters(key, maskDisplayFilter);
  };
}
