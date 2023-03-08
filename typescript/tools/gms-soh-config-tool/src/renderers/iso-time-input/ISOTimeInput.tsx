import { Autocomplete, FormControl, TextField } from '@mui/material';
import { uniqueId } from 'lodash';
import parse from 'parse-duration';
import React from 'react';
import {
  useIsStationSelected,
  useUpdateErrorState,
} from '../../util/custom-hooks';
import { isValidDuration } from '../../util/util';

/**
 * The type of the props for the {@link ISOTimeInput} component
 */
export interface ISOTimeInputProps {
  update: (newVal: string) => void;
  data: string;
  canInputBeZero: boolean;
  label?: string;
  options: string[];
  description?: string;
}

/**
 * This creates an ISO time input, which is an autocomplete in free solo mode, with the predefined values available.
 * It performs validation for the ISO 8601 duration format.
 */
export const ISOTimeInput: React.FC<ISOTimeInputProps> = ({
  update,
  data,
  canInputBeZero,
  label,
  options,
  description,
}: ISOTimeInputProps) => {
  const idRef = React.useRef(uniqueId());
  const disabled = !useIsStationSelected();
  const [updateErrorState] = useUpdateErrorState();
  const [errorVal, setError] = React.useState<string>('');
  const handleInput = React.useCallback(
    (_e: any, value: string | null) => {
      if (value) {
        if (
          (isValidDuration(value) && parse(value) !== 0) ||
          (parse(value) === 0 && canInputBeZero)
        ) {
          setError('');
          if (label) {
            updateErrorState(label.replace(/ /g, ''), false, '');
          }
          update(value);
        } else {
          setError(value);
          if (label) {
            updateErrorState(
              label.replace(/ /g, ''),
              true,
              `Invalid duration string for ${label}`
            );
          }
        }
      }
    },
    [update, updateErrorState, label, canInputBeZero]
  );
  return (
    <>
      <FormControl fullWidth>
        <Autocomplete
          disablePortal
          disabled={disabled}
          freeSolo
          id={`ISOTime-${idRef.current}`}
          className={'iso-time-input'}
          value={data ?? null}
          options={options}
          onChange={handleInput}
          onInputChange={handleInput}
          autoSelect
          renderInput={(params) => (
            <TextField
              {...params}
              label={label}
              error={errorVal !== ''}
              aria-describedby={`ISOTime-${idRef.current}`}
              helperText={errorVal ? 'Invalid duration string' : description}
            />
          )}
        />
      </FormControl>
    </>
  );
};
