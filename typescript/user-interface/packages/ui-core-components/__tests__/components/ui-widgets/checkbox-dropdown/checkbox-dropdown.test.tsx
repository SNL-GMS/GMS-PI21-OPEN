import { render } from '@testing-library/react';
import Immutable from 'immutable';
import React from 'react';

import { CheckboxList } from '../../../../src/ts/components/ui-widgets/checkbox-list';

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

describe('checkbox dropdown', () => {
  it('exists', () => {
    expect(CheckboxList).toBeDefined();
  });
  enum mockBoxEnum {
    firstBox = '1st',
    secondBox = '2nd',
    thirdBox = '3rd'
  }
  const mockEnumToCheckedMap = Immutable.Map([
    [mockBoxEnum.firstBox, false],
    [mockBoxEnum.secondBox, true],
    [mockBoxEnum.thirdBox, false]
  ]);
  const mockKeysToDisplayStrings = Immutable.Map([
    [mockBoxEnum.firstBox, 'The first checkbox'],
    [mockBoxEnum.secondBox, 'The second checkbox'],
    [mockBoxEnum.thirdBox, 'The third checkbox']
  ]);
  const mockColorMap = Immutable.Map([
    ['firstBox', '#123123'],
    ['secondBox', '#ABC123'],
    ['thirdBox', '#000000']
  ]);
  const mockOnChange = jest.fn() as jest.Mock<Map<any, boolean>>;
  const checkboxProps = {
    checkboxEnum: mockBoxEnum,
    enumToCheckedMap: mockEnumToCheckedMap,
    enumKeysToDisplayStrings: mockKeysToDisplayStrings,
    enumToColorMap: mockColorMap,
    onChange: mockOnChange
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const mockBox = Enzyme.shallow(<CheckboxList {...checkboxProps} />);
  // eslint-disable-next-line react/jsx-props-no-spreading
  const { container } = render(<CheckboxList {...checkboxProps} />);

  it('matches the snapshot', () => {
    expect(container).toMatchSnapshot();
  });

  it('updates state onChange', () => {
    const instance = mockBox.instance();
    const { onChange } = instance;
    expect(instance.state.enumToCheckedMap.get(mockBoxEnum.thirdBox)).toEqual(false);
    onChange('thirdBox');
    expect(instance.state.enumToCheckedMap.get(mockBoxEnum.thirdBox)).toEqual(true);
  });
});
