import { Autocomplete, FormControl, TextField } from '@mui/material';
import React from 'react';
import { getStationNames } from '../../coi-types/get-stations';
import { DialogPrompt } from '../../components/DialogPrompt';
import { configApi } from '../../state/api-slice';
import { useAppDispatch, useAppSelector } from '../../state/react-redux-hooks';
import { useAppContext } from '../../state/state';
import {
  clearStationErrors,
  resetLoadedData,
  setStationName,
} from '../../state/station-controls-slice';

interface StationNameInputProps {
  className?: string;
  updateStationName: (name: string) => void;
  formData: string;
  label?: string;
  description?: string;
}

export const StationNameInput: React.FC<StationNameInputProps> = ({
  className,
  label,
  formData,
  description,
}: StationNameInputProps) => {
  const { data: appData } = useAppContext();
  const stationList: string[] =
    getStationNames(appData?.processingStationGroups) ?? [];
  const stationName = useAppSelector(
    (state) => state.stationControls.stationName
  );
  const [confirmToggle, setConfirmToggle] = React.useState({
    open: false,
    stationName: '',
  });
  const dispatch = useAppDispatch();

  const updateData = React.useCallback(
    (newStationName: string) => {
      dispatch(setStationName(newStationName));
      dispatch(configApi.util.resetApiState()); // ensures a fresh query and not from cache
      dispatch(clearStationErrors());
      dispatch(resetLoadedData());
    },
    [dispatch]
  );

  const onConfirm = React.useCallback(
    (confirmation: boolean) => {
      if (confirmation) {
        updateData(confirmToggle.stationName);
      }
      setConfirmToggle({ open: false, stationName: '' });
    },
    [confirmToggle.stationName, updateData]
  );
  return (
    <>
      <DialogPrompt
        isOpen={confirmToggle.open}
        onConfirm={onConfirm}
        message={
          'Warning: changing stations will cause unsaved changes to be lost'
        }
      />
      <FormControl fullWidth>
        <Autocomplete
          autoComplete
          disablePortal
          disabled={stationList.length === 0}
          id='#/properties/station-name'
          className={className ?? 'station-name-input'}
          value={stationName ?? null}
          options={stationList}
          onChange={(_e, value) => {
            if (value) {
              if (stationName) {
                setConfirmToggle({ open: true, stationName: value });
              } else {
                updateData(value);
              }
            }
          }}
          renderInput={(params) => (
            <TextField
              {...params}
              label={label}
              aria-describedby='station-name-help-text'
              helperText={description}
            />
          )}
        />
      </FormControl>
    </>
  );
};
