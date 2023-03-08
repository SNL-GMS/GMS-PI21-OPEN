import { uuid } from '@gms/common-util';
import { render } from '@testing-library/react';
import * as React from 'react';

import { NewMessageIndicator } from '../../../../src/ts/components/common-ui/components/system-message/new-message-indicator';
import type { NewMessageIndicatorProps } from '../../../../src/ts/components/common-ui/components/system-message/types';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

// eslint-disable-next-line import/no-deprecated
const lodash = jest.requireActual('lodash');
lodash.uniqueId = () => '1';

let idCount = 0;
// eslint-disable-next-line no-plusplus
uuid.asString = jest.fn().mockImplementation(() => ++idCount);

describe('New Messages Indicator', () => {
  it('is defined', () => {
    expect(NewMessageIndicator).toBeDefined();
  });

  const props: NewMessageIndicatorProps = {
    isVisible: true,
    handleNewMessageIndicatorClick: jest.fn()
  };

  // eslint-disable-next-line react/jsx-props-no-spreading
  const wrapper = Enzyme.mount(<NewMessageIndicator {...props} />);

  it('matches its snapshot', () => {
    const { container } = render(
      // eslint-disable-next-line react/jsx-props-no-spreading
      <NewMessageIndicator {...props} />
    );
    expect(container).toMatchSnapshot();
  });

  it('scrolls to latest on click', () => {
    wrapper.find('[data-cy="new-messages-button"]').first().simulate('click');
    // eslint-disable-next-line @typescript-eslint/unbound-method
    expect(props.handleNewMessageIndicatorClick).toHaveBeenCalledTimes(1);
  });
});
