import { H3, H4, Icon } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import React from 'react';
import { Link } from 'react-router-dom';

/**
 * Example Home component
 */
export class Home extends React.PureComponent<unknown, unknown> {
  /**
   * React render method
   */
  public render(): JSX.Element {
    return (
      <div>
        <div>
          <H3>Examples</H3>
          <p>
            If this project is cloned or downloaded, these examples can be loaded as files in the
            browser. Otherwise, peruse the source code for ideas on how to use the components in UI
            Core Components.
          </p>
          <H3>Documentation</H3>
          <p>Currently, the only documentation is on an internal wiki.</p>
        </div>
        <br />
        <div>
          <H4>
            <Icon icon={IconNames.PROPERTIES} />
            <Link to="/DropDownExample"> Drop Down Example</Link>
          </H4>
          <p>Select items from a dropdown populated by an enum.</p>
          <H4>
            <Icon icon={IconNames.CALENDAR} />
            <Link to="/IntervalPickerExample"> Interval Picker Example</Link>
          </H4>
          <p>Select a time range.</p>
          <H4>
            <Icon icon={IconNames.TIME} />
            <Link to="/FilterableOptionListExample"> FilterableOptionList Example</Link>
          </H4>
          <p>You literally select things from a filterable list</p>

          <H4>
            <Icon icon={IconNames.APPLICATION} />
            <Link to="/FormNoInput"> Form No Input Example</Link>
          </H4>
          <p>
            A form that does not accept input, but uses form features such as showing extra content
            and having multiple panels.
          </p>
          <H4>
            <Icon icon={IconNames.APPLICATION} />
            <Link to="/FormSubmittable"> Form Example</Link>
          </H4>
          <p>A form that accepts input.</p>
          <H4>
            <Icon icon={IconNames.PANEL_TABLE} />
            <Link to="/Table"> Table Example</Link>
          </H4>
          <p>Basic introduction to using the Table component.</p>
          <H4>
            <Icon icon={IconNames.TIME} />
            <Link to="/TimePickerExample"> Time Picker Example</Link>
          </H4>
          <p>Inputs date. Requires a valid date string</p>
          <H4>
            <Icon icon={IconNames.MENU} />
            <Link to="/ToolbarExample"> Toolbar Example</Link>
          </H4>
          <p>Resizeable toolbar that inteeligently handles overflow</p>
        </div>
      </div>
    );
  }
}
