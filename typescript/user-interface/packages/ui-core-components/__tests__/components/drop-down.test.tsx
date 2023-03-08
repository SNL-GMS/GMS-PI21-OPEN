import { Logger } from '@gms/common-util';
import { render } from '@testing-library/react';
import React from 'react';

import { DropDown } from '../../src/ts/components/ui-widgets/drop-down';
import type { DropDownProps } from '../../src/ts/components/ui-widgets/drop-down/types';

const logger = Logger.create('GMS_LOG_JEST', process.env.GMS_LOG_JEST);

// eslint-disable-next-line @typescript-eslint/no-var-requires, @typescript-eslint/no-require-imports
const Enzyme = require('enzyme');

// set up window alert and open so we don't see errors
(window as any).alert = jest.fn();
(window as any).open = jest.fn();

async function flushPromises() {
  return new Promise(resolve => {
    setTimeout(resolve, 0);
  });
}

describe('Core drop down', () => {
  enum TEST_ENUM {
    test = 'test',
    foo = 'foo',
    bar = 'bar'
  }
  let testValue = TEST_ENUM.bar;
  const props: DropDownProps = {
    dropDownItems: TEST_ENUM,
    value: testValue,
    displayLabel: false,
    widthPx: 120,
    disabled: false,
    onMaybeValue: value => {
      testValue = value;
    }
  };
  const propsWithLabel: DropDownProps = {
    dropDownItems: TEST_ENUM,
    value: testValue,
    displayLabel: true,
    label: 'best label ever',
    widthPx: 120,
    disabled: false,
    onMaybeValue: value => {
      testValue = value;
    }
  };
  const propsWithLabelNoDisplay: DropDownProps = {
    dropDownItems: TEST_ENUM,
    value: testValue,
    label: 'best label ever',
    displayLabel: false,
    widthPx: 120,
    disabled: false,
    onMaybeValue: value => {
      testValue = value;
    }
  };
  // eslint-disable-next-line react/jsx-props-no-spreading
  const mockDropdown = Enzyme.shallow(<DropDown {...props} />);
  // eslint-disable-next-line react/jsx-props-no-spreading
  const { container: mockDropdownWithBasicProps } = render(<DropDown {...props} />);
  // eslint-disable-next-line react/jsx-props-no-spreading
  const { container: mockDropdownWithLabel } = render(<DropDown {...propsWithLabel} />);
  const { container: mockDropdownWithLabelButNoDisplay } = render(
    // eslint-disable-next-line react/jsx-props-no-spreading
    <DropDown {...propsWithLabelNoDisplay} />
  );

  it('Renders with label', () => {
    expect(mockDropdownWithLabel).toMatchSnapshot();
  });
  it('Renders without label', () => {
    expect(mockDropdownWithLabelButNoDisplay).toMatchSnapshot();
  });
  it('Renders', () => {
    expect(mockDropdownWithBasicProps).toMatchSnapshot();
  });
  it('Can have a value set', () => {
    mockDropdown.find('HTMLSelect').prop('onChange')({
      target: { value: TEST_ENUM.foo }
    });
    flushPromises().catch(logger.warn);
    expect(testValue).toEqual(TEST_ENUM.foo);
  });

  // TODO redo test in RTL
  it('Can render custom value', () => {
    testValue = TEST_ENUM.bar;
    const props2: DropDownProps = {
      dropDownItems: TEST_ENUM,
      value: testValue,
      widthPx: 120,
      disabled: false,
      onMaybeValue: value => {
        testValue = value;
      },
      custom: true,
      dropdownText: {
        [TEST_ENUM.test]: 'Test',
        [TEST_ENUM.foo]: 'Foo',
        [TEST_ENUM.bar]: 'Bar'
      }
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const mockDropdownCustom = Enzyme.shallow(<DropDown {...props2} />);
    expect(mockDropdownCustom).toMatchSnapshot();

    // Ensure callback works
    mockDropdownCustom.find('HTMLSelect').prop('onChange')({
      target: { value: TEST_ENUM.foo }
    });
    flushPromises().catch(logger.warn);
    expect(testValue).toBe(TEST_ENUM.foo);

    // Ensure this sentinel value doesn't trigger an onMaybeValue callback
    mockDropdownCustom.find('HTMLSelect').prop('onChange')({
      target: { value: 'UNSELECTED_CUSTOM_VALUE' }
    });
    flushPromises().catch(logger.warn);
    expect(testValue).toBe(TEST_ENUM.foo);
  });
  // TODO redo test in RTL
  it('Can render a disabled value', () => {
    testValue = TEST_ENUM.bar;
    const props2: DropDownProps = {
      dropDownItems: TEST_ENUM,
      value: testValue,
      widthPx: 120,
      disabled: false,
      onMaybeValue: value => {
        testValue = value;
      },
      custom: true,
      disabledDropdownOptions: [TEST_ENUM.test]
    };
    // eslint-disable-next-line react/jsx-props-no-spreading
    const mockDropdownCustom = Enzyme.shallow(<DropDown {...props2} />);
    expect(mockDropdownCustom).toMatchSnapshot();

    // Ensure disabled is true works
    expect(mockDropdownCustom.find('option').first().prop('disabled')).toEqual(true);
  });
});
