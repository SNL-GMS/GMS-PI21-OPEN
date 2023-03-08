import { render } from '@testing-library/react';
import * as enzyme from 'enzyme';
import * as React from 'react';

import { IanMapMultiSelectComponent } from '../../../../../src/ts/components/analyst-ui/components/map/ian-map-multi-select-component';

jest.mock('@gms/ui-state', () => {
  const actualRedux = jest.requireActual('@gms/ui-state');
  return {
    ...actualRedux,
    useAppDispatch: jest.fn(() => jest.fn())
  };
});

describe('map multi select component', () => {
  test('is defined', () => {
    expect(IanMapMultiSelectComponent).toBeDefined();
  });
  // TODO redo this test in RTL
  test('takes populated viewer', () => {
    const viewer: any = {
      scene: {
        // eslint-disable-next-line @typescript-eslint/no-unused-vars
        pickPosition: jest.fn(endPosition => {
          'myPosition';
        })
      }
    };
    const wrapper = enzyme.mount(<IanMapMultiSelectComponent viewer={viewer} />);
    expect(wrapper.find('IanMapMultiSelectComponent')).toMatchSnapshot();
  });

  test('can handle an undefined viewer', () => {
    const viewer: any = undefined;
    const { container } = render(<IanMapMultiSelectComponent viewer={viewer} />);
    expect(container).toMatchSnapshot();
  });
});
