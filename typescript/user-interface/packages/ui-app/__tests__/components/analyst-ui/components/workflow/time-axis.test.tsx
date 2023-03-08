import { render } from '@testing-library/react';
import React from 'react';
import * as util from 'util';

import type { WorkflowTimeAxisProps } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-time-axis';
import { WorkflowTimeAxis } from '../../../../../src/ts/components/analyst-ui/components/workflow/workflow-time-axis';

// set up window alert and open so we don't see errors
window.alert = jest.fn();
window.open = jest.fn();

Object.defineProperty(window, 'TextEncoder', {
  writable: true,
  value: util.TextEncoder
});
Object.defineProperty(window, 'TextDecoder', {
  writable: true,
  value: util.TextDecoder
});
Object.defineProperty(global, 'TextEncoder', {
  writable: true,
  value: util.TextEncoder
});
Object.defineProperty(global, 'TextDecoder', {
  writable: true,
  value: util.TextDecoder
});

const mockWorkflowTimeAxisProps: WorkflowTimeAxisProps = {
  width: 500,
  timeRange: {
    startTimeSecs: 3000,
    endTimeSecs: 3600
  }
};

describe('workflow time axis tests', () => {
  it('renders a snapshot', () => {
    // eslint-disable-next-line react/jsx-props-no-spreading
    const { container } = render(<WorkflowTimeAxis {...mockWorkflowTimeAxisProps} />);
    expect(container).toMatchSnapshot('component');
  });
});
