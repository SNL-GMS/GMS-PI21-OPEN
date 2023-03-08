/* eslint-disable no-promise-executor-return */
/* eslint-disable jest/no-disabled-tests */
/* eslint-disable @typescript-eslint/no-magic-numbers */
/* eslint-disable react/jsx-props-no-spreading */
import { render } from '@testing-library/react';
import * as Enzyme from 'enzyme';
import * as React from 'react';
import { act } from 'react-dom/test-utils';

import { NumericInput } from '../../../../src/ts/components/ui-widgets/numeric-input';
import { useErrorIntentOnInvalidValue } from '../../../../src/ts/components/ui-widgets/numeric-input/numeric-input';
import type { NumericInputProps } from '../../../../src/ts/components/ui-widgets/numeric-input/types';
import type { MinMax } from '../../../../src/ts/components/ui-widgets/toolbar/types';

describe('numeric input', () => {
  const baseProps: NumericInputProps = {
    onChange: jest.fn(),
    tooltip: 'test numeric input',
    value: 100
  };
  const numericInputMinMax: MinMax = {
    min: 0,
    max: 100
  };
  describe('component', () => {
    it('exists', () => {
      expect(NumericInput).toBeDefined();
    });

    it('matches a snapshot with default values', () => {
      const { container } = render(<NumericInput {...baseProps} />);
      expect(container).toMatchSnapshot();
    });

    it('matches a snapshot when given optional props', () => {
      const { container } = render(
        <NumericInput
          {...baseProps}
          minMax={numericInputMinMax}
          widthPx={100}
          onChangeDebounceMs={200}
          step={2}
          waitToShowErrorMs={1000}
        />
      );
      expect(container).toMatchSnapshot();
    });
    it('matches a snapshot when disabled', () => {
      const { container } = render(<NumericInput {...baseProps} disabled />);
      expect(container).toMatchSnapshot();
    });
  });
  describe('useErrorIntentOnInvalidValue', () => {
    const mockOnChange = jest.fn();
    interface TestComponentProps {
      value: number;
      valueAsString?: string;
    }

    function TestComponent({ value, valueAsString = value.toString() }: TestComponentProps) {
      // start within range
      const { intent, onChangeWithIntent } = useErrorIntentOnInvalidValue(mockOnChange, 1, {
        min: 1,
        max: 100
      });
      // then change to value
      onChangeWithIntent(value, valueAsString);
      // eslint-disable-next-line react/jsx-no-useless-fragment
      return <>{intent}</>;
    }

    afterEach(() => {
      mockOnChange.mockClear();
    });

    const numberTooLow = 0;
    const numberTooHigh = 9999;
    const numberInBounds = 2;

    // TODO Unskip tests and fix
    it.skip('matches returns the "danger" intent when value is out of bounds', async () => {
      const wrapper = Enzyme.mount(<TestComponent value={numberTooLow} />);
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 20));
        wrapper.update();
      });
      expect(wrapper.find(TestComponent).contains('danger')).toBe(true);
    });
    // TODO convert test to none enzyme, test fails in pipeline but passes locally
    it.skip('matches returns the "none" intent when value is in bounds', async () => {
      const wrapper = Enzyme.mount(<TestComponent value={numberInBounds} />);
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 20));
        wrapper.update();
      });
      expect(wrapper.find(TestComponent).contains('none')).toBe(true);
    });

    // TODO convert test to none enzyme, test fails in pipeline but passes locally
    it.skip('calls the onChange function provided to it when its returned function is called with a number in bounds', async () => {
      const wrapper = Enzyme.mount(<TestComponent value={numberInBounds} />);
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 20));
        wrapper.update();
      });
      expect(mockOnChange).toHaveBeenCalledTimes(1);
    });

    // TODO convert test to none enzyme, test fails in pipeline but passes locally
    it.skip('does not call the onChange function provided to it when its returned function is called with a number out of bounds', async () => {
      const wrapper = Enzyme.mount(<TestComponent value={numberTooHigh} />);
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 20));
        wrapper.update();
      });
      expect(mockOnChange).toHaveBeenCalledTimes(0);
    });

    it('does not call the onChange function provided to it when given an empty string', async () => {
      const wrapper = Enzyme.mount(<TestComponent value={0} valueAsString="" />);
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 20));
        wrapper.update();
      });
      expect(mockOnChange).toHaveBeenCalledTimes(0);
    });

    it('matches returns the "none" intent when value is an empty string', async () => {
      const wrapper = Enzyme.mount(<TestComponent value={numberTooLow} valueAsString="" />);
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 20));
        wrapper.update();
      });
      expect(wrapper.find(TestComponent).contains('none')).toBe(true);
    });

    it('clears the timeout to set the error intent if one was set', async () => {
      window.clearTimeout = jest.fn();
      const wrapper = Enzyme.mount(<TestComponent value={0} valueAsString="0" />);
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 1));
        wrapper.update();
      });
      wrapper.setProps({ value: 1 });
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 20));
        wrapper.update();
      });
      expect(window.clearTimeout).toHaveBeenCalled();
    });

    // TODO Unskip tests and fix
    it.skip('clears the timeout on unmount', async () => {
      window.clearTimeout = jest.fn();
      const wrapper = Enzyme.mount(<TestComponent value={0} valueAsString="0" />);
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 1));
        wrapper.update();
      });
      wrapper.unmount();
      await act(async () => {
        await new Promise(resolve => setTimeout(resolve, 20));
        wrapper.update();
      });
      expect(window.clearTimeout).toHaveBeenCalled();
    });
  });
});
