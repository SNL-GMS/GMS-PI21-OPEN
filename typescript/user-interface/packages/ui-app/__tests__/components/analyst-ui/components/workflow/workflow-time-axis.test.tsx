/* eslint-disable react/jsx-props-no-spreading */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { getStore } from '@gms/ui-state';
import { render } from '@testing-library/react';
import * as React from 'react';
import { Provider } from 'react-redux';

import type { WorkflowTimeAxisProps } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-time-axis';
import { WorkflowTimeAxis } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-time-axis';

const store = getStore();
describe('Workflow Time Axis', () => {
  it('is exported', () => {
    expect(WorkflowTimeAxis).toBeDefined();
  });

  it('matches snapshot', () => {
    const { container } = render(
      <Provider store={store}>
        <WorkflowTimeAxis timeRange={undefined} width={undefined} />
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  const props: WorkflowTimeAxisProps = {
    timeRange: {
      startTimeSecs: 50000,
      endTimeSecs: 60000
    },
    width: 500
  };

  it('Workflow time axis shallow renders', () => {
    const { container } = render(
      <Provider store={store}>
        <WorkflowTimeAxis {...props} />{' '}
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });

  const props2: WorkflowTimeAxisProps = {
    timeRange: {
      startTimeSecs: 1610211861,
      endTimeSecs: 1631207062
    },
    width: 500
  };

  it('Workflow time axis hits else case', () => {
    const { container } = render(
      <Provider store={store}>
        <WorkflowTimeAxis {...props2} />{' '}
      </Provider>
    );
    expect(container).toMatchSnapshot();
  });
});
