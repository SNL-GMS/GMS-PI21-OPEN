import * as React from 'react';
import renderer from 'react-test-renderer';

import { BaseDisplay } from '../../../../../src/ts/components/common-ui/components/base-display';

describe('System Messages Display', () => {
  it('should be defined', () => {
    expect(BaseDisplay).toBeDefined();
  });

  it('matches snapshot', () => {
    const component = renderer.create(
      <BaseDisplay
        glContainer={
          {
            onContextMenu: jest.fn(),
            width: 1,
            height: 150,
            on: jest.fn()
          } as any
        }
        className="mock-display"
      />
    );
    const tree = component.toJSON();
    expect(tree).toMatchSnapshot();
  });
});
