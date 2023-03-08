/* eslint-disable complexity */
/* eslint-disable @typescript-eslint/no-magic-numbers */
import { Button, Classes, Dialog, Icon, Intent } from '@blueprintjs/core';
import type { DateRange } from '@blueprintjs/datetime';
import {
  Classes as DateTimeClasses,
  DateInput,
  DateRangePicker,
  TimePicker
} from '@blueprintjs/datetime';
import { IconNames } from '@blueprintjs/icons';
import {
  convertDateToUTCDate,
  convertUTCDateToDate,
  DATE_FORMAT,
  DATE_TIME_FORMAT_WITH_SECOND_PRECISION,
  MILLISECONDS_IN_DAY,
  MILLISECONDS_IN_HOUR,
  MILLISECONDS_IN_MINUTE,
  MINUTES_IN_HOUR
} from '@gms/common-util';
import { convertTimeFormatToTimePrecision } from '@gms/ui-util';
import debounce from 'lodash/debounce';
import moment from 'moment';
import React, { useEffect, useState } from 'react';

import type { DateRangePopupProps, DurationOption } from './types';

const DELAY_MS = 200;

/**
 * ! blueprint specific workaround for an open issue they have see
 * https://github.com/palantir/blueprint/issues/3338
 * this work around is makes it that when clicking a shortcut doesn't dismiss the popover
 * once ticket is closed, should be removed
 * This maybe able to be removed in BP 3.48.
 */
export const removePopoverDismiss = (): void => {
  // eslint-disable-next-line @blueprintjs/classes-constants
  const shortcuts = document.querySelectorAll(
    `.${DateTimeClasses.DATERANGEPICKER_SHORTCUTS} li .${Classes.MENU_ITEM}`
  );
  shortcuts.forEach(shortcut => {
    shortcut.classList.remove(Classes.POPOVER_DISMISS);
  });
};

export const DateRangePopup: React.FunctionComponent<React.PropsWithChildren<
  DateRangePopupProps
  // eslint-disable-next-line react/function-component-definition
>> = (props: React.PropsWithChildren<DateRangePopupProps>) => {
  // eslint-disable-next-line @typescript-eslint/unbound-method
  const {
    startTimeMs,
    endTimeMs,
    format,
    durations,
    minStartTimeMs,
    maxEndTimeMs,
    maxSelectedRangeMs,
    isOpen,
    resetOnClose,
    title,
    children,
    applyText,
    cancelText,
    onNewInterval,
    onApply,
    onClose
  } = props;

  const formatDate = (date: Date): string => moment(date).format(DATE_FORMAT);
  const parseDate = (str: string): Date | false => {
    if (str) {
      // validate against JS date since it wont parse an invalid format
      const date = new Date(str);
      if (Number.isNaN(date.valueOf())) {
        return false;
      }
      return moment(str, format).toDate(); // return the moment parse instead since it handles UTC better;
    }
    return false;
  };
  // eslint-disable-next-line no-nested-ternary
  const timePrecision = convertTimeFormatToTimePrecision(format);

  // convert incoming dates to UTC.  We are ignoring timezones in this file and converting between UTC on enter and exit to ensure UTC display
  // Need the null check because new Date(null) is the beginning on unix time
  const [selectedDateRange, setSelectedDateRange] = useState([
    startTimeMs !== null ? convertDateToUTCDate(new Date(startTimeMs)) : null,
    endTimeMs !== null ? convertDateToUTCDate(new Date(endTimeMs)) : null
  ] as [Date, Date]);

  // Set up now with UTC offset.
  const now = Date.now();
  const utcNow = now + new Date(now).getTimezoneOffset() * MILLISECONDS_IN_MINUTE;

  // Convert min and max with UTC offset.
  const utcMin = minStartTimeMs ? convertDateToUTCDate(new Date(minStartTimeMs)).getTime() : 0;
  const utcMax = maxEndTimeMs
    ? convertDateToUTCDate(new Date(maxEndTimeMs)).getTime()
    : utcNow + MILLISECONDS_IN_DAY;

  let isSingleMonth = false;
  if (
    new Date(utcMax).getFullYear() === new Date(utcMin).getFullYear() &&
    new Date(utcMax).getMonth() === new Date(utcMin).getMonth()
  ) {
    isSingleMonth = true;
  }

  const [errorMessage, setErrorMessage] = useState('');
  const [isApplyDisabled, setIsApplyDisabled] = useState(false);
  const [isStartDateInvalid, setIsStartDateInvalid] = useState(false);
  const [isEndDateInvalid, setIsEndDateInvalid] = useState(false);

  // Set up values for startTime and EndTime
  // This is required for snapshot testing otherwise the component is filled in with a default value that changes
  const minStartTime = new Date(new Date(0));
  const maxEndTime = new Date(
    1970,
    0,
    1,
    23 - minStartTime.getTimezoneOffset() / MINUTES_IN_HOUR,
    59,
    59
  );

  useEffect(() => {
    if (isStartDateInvalid) {
      setErrorMessage(`Invalid Start Date`);
      setIsApplyDisabled(true);
    } else if (isEndDateInvalid) {
      setErrorMessage(`Invalid End Date`);
      setIsApplyDisabled(true);
    } else if (selectedDateRange[0] == null || selectedDateRange[1] == null) {
      setErrorMessage('');
      setIsApplyDisabled(true);
    } else if (
      maxSelectedRangeMs &&
      selectedDateRange[0].getTime() + maxSelectedRangeMs < selectedDateRange[1].getTime()
    ) {
      const numHours = maxSelectedRangeMs / MILLISECONDS_IN_HOUR;
      setErrorMessage(`Time Range exceeds maximum range of ${numHours} hours`);
      setIsApplyDisabled(true);
    } else if (selectedDateRange[0].getTime() >= selectedDateRange[1].getTime()) {
      setErrorMessage('Start date overlaps end date');
      setIsApplyDisabled(true);
    } else if (selectedDateRange[0].getTime() < utcMin) {
      setErrorMessage(
        `Start date is before minimum start date ${moment(new Date(utcMin)).format(format)}`
      );
      setIsApplyDisabled(true);
    } else if (selectedDateRange[1].getTime() > utcMax) {
      setErrorMessage(
        `End date is after maximum end date ${moment(new Date(utcMax)).format(format)}`
      );
      setIsApplyDisabled(true);
    } else {
      setErrorMessage('');
      setIsApplyDisabled(false);
    }
  }, [
    selectedDateRange,
    format,
    maxSelectedRangeMs,
    utcMax,
    utcMin,
    isStartDateInvalid,
    isEndDateInvalid
  ]);

  useEffect(() => {
    // reset values when the popup closes
    if (!isOpen && resetOnClose) {
      setSelectedDateRange([
        convertDateToUTCDate(new Date(startTimeMs)),
        convertDateToUTCDate(new Date(endTimeMs))
      ] as [Date, Date]);
    }
  }, [endTimeMs, isOpen, resetOnClose, startTimeMs]);

  const handleDateRangeChange = (dateRange: DateRange) => {
    setSelectedDateRange(dateRange);
    setIsStartDateInvalid(false);
    setIsEndDateInvalid(false);
    if (onNewInterval && dateRange[0] && dateRange[1])
      onNewInterval(
        convertUTCDateToDate(dateRange[0]).getTime(),
        convertUTCDateToDate(dateRange[1]).getTime()
      );
  };

  const handleStartDateChange = (startDate: Date) => {
    if (startDate) {
      if (parseDate(formatDate(startDate))) {
        setSelectedDateRange([startDate, selectedDateRange[1]]);
        if (onNewInterval && selectedDateRange[1]) {
          onNewInterval(
            convertUTCDateToDate(startDate).getTime(),
            convertUTCDateToDate(selectedDateRange[1]).getTime()
          );
        }
        setIsStartDateInvalid(false);
      } else {
        setIsStartDateInvalid(true);
        setSelectedDateRange([null, selectedDateRange[1]]);
      }
    }
  };

  const handleEndDateChange = (endDate: Date) => {
    if (endDate) {
      if (parseDate(endDate.toString())) {
        setSelectedDateRange([selectedDateRange[0], endDate]);
        if (onNewInterval && selectedDateRange[0])
          onNewInterval(
            convertUTCDateToDate(selectedDateRange[0]).getTime(),
            convertUTCDateToDate(endDate).getTime()
          );
        setIsEndDateInvalid(false);
      } else {
        setIsEndDateInvalid(true);
        setSelectedDateRange([selectedDateRange[0], null]);
      }
    }
  };

  const handleStartTimeChange = (startDate: Date) => {
    if (startDate) {
      startDate.setFullYear(
        selectedDateRange[0].getFullYear(),
        selectedDateRange[0].getMonth(),
        selectedDateRange[0].getDate()
      );
      if (parseDate(formatDate(startDate))) {
        setSelectedDateRange([startDate, selectedDateRange[1]]);
        if (onNewInterval && selectedDateRange[1]) {
          onNewInterval(
            convertUTCDateToDate(startDate).getTime(),
            convertUTCDateToDate(selectedDateRange[1]).getTime()
          );
        }
      }
    }
  };

  const handleEndTimeChange = (endDate: Date) => {
    if (endDate) {
      endDate.setFullYear(
        selectedDateRange[1].getFullYear(),
        selectedDateRange[1].getMonth(),
        selectedDateRange[1].getDate()
      );
      if (parseDate(endDate.toString())) {
        setSelectedDateRange([selectedDateRange[0], endDate]);
        if (onNewInterval && selectedDateRange[0]) {
          onNewInterval(
            convertUTCDateToDate(selectedDateRange[0]).getTime(),
            convertUTCDateToDate(endDate).getTime()
          );
        }
      }
    }
  };

  const handleApplyButton = () => {
    const convertedStartTimeMs = convertUTCDateToDate(selectedDateRange[0]).getTime();
    const convertedEndTimeMs = convertUTCDateToDate(selectedDateRange[1]).getTime();
    onNewInterval(convertedStartTimeMs, convertedEndTimeMs);
    onApply(convertedStartTimeMs, convertedEndTimeMs);
  };

  const getDateRangeInputProps = () => {
    return {
      className: 'date-range-popup__range-input',
      formatDate,
      parseDate,
      value: selectedDateRange,
      onChange: debounce(handleDateRangeChange, DELAY_MS),
      shortcuts: durations
        ? durations.map((item: DurationOption) => ({
            dateRange: [new Date(utcNow - item.value), new Date(utcNow)] as [Date, Date],
            includeTime: true,
            label: item.description
          }))
        : false,
      placeholder: format,
      contiguousCalendarMonths: true,
      closeOnSelection: false,
      allowSingleDayRange: true,
      minDate: new Date(utcMin),
      maxDate: new Date(utcMax),
      dayPickerProps: {
        className: 'date-range-picker--column'
      }
    };
  };

  return (
    <Dialog
      className="date-range-dialog dialog_parent dialog_parent--wide"
      isOpen={isOpen}
      title={title}
      onClose={onClose}
      onOpened={removePopoverDismiss}
    >
      <div
        className={
          durations ? 'date-range-picker__selectors-durations' : 'date-range-picker__selectors'
        }
      >
        <div className="date-input-groups">
          <div
            className="date-input-group"
            data-cy="start-date-input"
            // new Date??
            data-start-time={new Date(selectedDateRange[0]).getTime()}
            data-start-date={moment(selectedDateRange[0]).format(
              DATE_TIME_FORMAT_WITH_SECOND_PRECISION
            )}
          >
            <span className="date-input-label">Start</span>
            <DateInput
              className="date-input"
              data-cy="start_date"
              value={selectedDateRange[0]}
              onChange={debounce(handleStartDateChange, DELAY_MS)}
              popoverProps={{ disabled: true }}
              formatDate={formatDate}
              parseDate={parseDate}
              minDate={new Date(utcMin)}
              maxDate={new Date(utcMax)}
            />
            <TimePicker
              className="time-input"
              data-cy="start_time"
              showArrowButtons
              precision={timePrecision}
              value={selectedDateRange[0]}
              onChange={handleStartTimeChange}
              minTime={minStartTime}
              maxTime={maxEndTime}
            />
          </div>
          <div
            className="date-input-group"
            data-cy="end-date-input"
            data-end-time={new Date(selectedDateRange[1]).getTime()}
            data-end-date={moment(selectedDateRange[1]).format(
              DATE_TIME_FORMAT_WITH_SECOND_PRECISION
            )}
          >
            <span className="date-input-label">End</span>
            <DateInput
              className="date-input"
              data-cy="end_date"
              value={selectedDateRange[1]}
              formatDate={formatDate}
              parseDate={parseDate}
              popoverProps={{ disabled: true }}
              onChange={debounce(handleEndDateChange, DELAY_MS)}
              minDate={new Date(utcMin)}
              maxDate={new Date(utcMax)}
            />
            <TimePicker
              className="time-input"
              data-cy="end_time"
              showArrowButtons
              precision={timePrecision}
              value={selectedDateRange[1]}
              onChange={handleEndTimeChange}
              minTime={minStartTime}
              maxTime={maxEndTime}
            />
          </div>
        </div>
        <div className="calendar-group">
          <DateRangePicker
            // eslint-disable-next-line react/jsx-props-no-spreading
            {...getDateRangeInputProps()}
          />
          {isSingleMonth ? <div className="calendar-place-holder">Month out of range </div> : ''}
        </div>
        {children}
      </div>
      <div className={durations ? 'date-input-error-durations' : 'date-input-error'}>
        <Icon
          icon={errorMessage ? IconNames.ERROR : null}
          className="date-input-error-icon"
          iconSize={16}
        />
        <div className="date-input-error-text"> {errorMessage} </div>
      </div>
      <div className="date-input-apply-cancel">
        <Button
          text={applyText || 'Apply'}
          data-cy="date-picker-apply-button"
          intent={Intent.PRIMARY}
          onClick={handleApplyButton}
          disabled={isApplyDisabled}
        />
        <Button
          text={cancelText || 'Cancel'}
          data-cy="date-picker-cancel-button"
          onClick={() => onClose()}
        />
      </div>
    </Dialog>
  );
};
