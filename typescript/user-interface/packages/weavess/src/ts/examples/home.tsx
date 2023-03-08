import { H3, H4, Icon } from '@blueprintjs/core';
import { IconNames } from '@blueprintjs/icons';
import React from 'react';
import { Link } from 'react-router-dom';

// eslint-disable-next-line react/prefer-stateless-function
export class Home extends React.Component<unknown, unknown> {
  public render(): JSX.Element {
    return (
      <div>
        <div>
          <H3>Examples</H3>
          <p>
            If this project is cloned or downloaded, these examples can be loaded as files in the
            browser. Otherwise, peruse the source code for ideas on how to use WEAVESS in various
            ways.
          </p>
          <H3>Documentation</H3>
          <p>
            Currently, no formal API Docs exist. Hopefully they will be coming soon. For now, these
            examples and the source code are your only hope.
          </p>
        </div>
        <br />
        <div>
          <H4>
            <Icon icon={IconNames.CHART} />
            <Link to="/WeavessExample"> Weavess Example</Link>
          </H4>
          <p>Basic introduction to using Weavess. Start here first.</p>
          <br />
          <H4>
            <Icon icon={IconNames.CHART} />
            <Link to="/WeavessFlatLineExample"> Weavess Flat Line Example</Link>
          </H4>
          <p>Weavess displaying a flat line with data segments defined by time.</p>
          <br />
          <H4>
            <Icon icon={IconNames.CHART} />
            <Link to="/WeavessLineChartExample"> Weavess Line Chart Example</Link>
          </H4>
          <p>Weavess displaying a line chart example.</p>
          <br />
          <H4>
            <Icon icon={IconNames.MULTI_SELECT} />
            <Link to="/MultipleDisplaysExample"> Multiple Displays Example</Link>
          </H4>
          <p>
            Multiple Weavess displays can be displayed anywhere on the screen and operate completely
            independent of each other
          </p>
          <br />
          <H4>
            <Icon icon={IconNames.TIMELINE_EVENTS} />
            <Link to="/EventsExample"> Events Example</Link>
          </H4>
          <p>Register callbacks for various events triggered by the Weavess display</p>
          <br />
          <H4>
            <Icon icon={IconNames.RECORD} />
            <Link to="/RecordSectionExample"> Record Section Example</Link>
          </H4>
          <p>Record Section-style waveform display</p>
          <br />
        </div>
      </div>
    );
  }
}
