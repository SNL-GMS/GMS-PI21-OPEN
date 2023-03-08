import { Intent, NumericInput as BlueprintNumericInput } from '@blueprintjs/core';
import debounce from 'lodash/debounce';
import * as React from 'react';

import type { MinMax } from '../toolbar/types';
import type { NumericInputProps } from './types';

/**
 * Creates an onChangeWithIntent function that checks the value passed in. If the value is
 * valid, it calls the provided onChange function. If out of range, it returns the DANGER intent
 * and does not call the onChange function.
 *
 * @param onChange the onChange function that should be called if the value is valid
 * @param waitToShowErrorMs how long to wait in milliseconds before returning the error intent
 * @param minMax the min and max values allowed. If undefined,
 * @returns an intent, either danger or none, and a onChangeWithIntent function that should be used as the onChange handler
 */
export const useErrorIntentOnInvalidValue = (
  onChange: (val: number) => void,
  waitToShowErrorMs: number,
  minMax?: MinMax
): {
  onChangeWithIntent: (valueAsNumber: number, valueAsString: string) => void;
  intent: typeof Intent.DANGER | typeof Intent.NONE;
} => {
  const [intent, setIntent] = React.useState<typeof Intent.DANGER | typeof Intent.NONE>(
    Intent.NONE
  );
  const timeoutRef = React.useRef<number>();

  React.useEffect(() => {
    return () => {
      window.clearTimeout(timeoutRef.current);
    };
  }, []);

  const onChangeWithIntent = React.useCallback(
    (valueAsNumber: number, valueAsString: string) => {
      if (valueAsString !== '' && (valueAsNumber < minMax?.min || valueAsNumber > minMax?.max)) {
        timeoutRef.current = window.setTimeout(() => {
          setIntent(Intent.DANGER);
        }, waitToShowErrorMs);
      } else {
        if (timeoutRef.current) {
          window.clearTimeout(timeoutRef.current);
        }
        if (intent !== Intent.NONE) {
          setIntent(Intent.NONE);
        }
        if (valueAsString !== '') {
          onChange(valueAsNumber);
        }
      }
    },
    [intent, minMax?.max, minMax?.min, onChange, waitToShowErrorMs]
  );
  return { onChangeWithIntent, intent };
};

/**
 * A wrapper around the Blueprint NumericInput component.
 * Adds error handling (turns red if a bad input is given)
 */
// eslint-disable-next-line react/function-component-definition
export const NumericInput: React.FC<NumericInputProps> = ({
  tooltip,
  widthPx,
  disabled,
  minMax,
  onChangeDebounceMs = 0,
  onChange,
  step,
  value,
  waitToShowErrorMs = 500,
  cyData
}: NumericInputProps) => {
  const style = React.useMemo(() => ({ width: `${widthPx}px` }), [widthPx]);
  const { intent, onChangeWithIntent } = useErrorIntentOnInvalidValue(
    onChange,
    waitToShowErrorMs,
    minMax
  );
  return (
    <div style={style} className="numeric-input__wrapper">
      <BlueprintNumericInput
        asyncControl
        clampValueOnBlur
        className="numeric-input"
        defaultValue={value}
        value={value}
        disabled={disabled}
        intent={intent}
        max={minMax?.max}
        min={minMax?.min}
        onValueChange={debounce(onChangeWithIntent, onChangeDebounceMs)}
        placeholder="Enter a number..."
        title={tooltip}
        stepSize={step}
        width={widthPx}
        fill
        data-cy={cyData}
      />
    </div>
  );
};
