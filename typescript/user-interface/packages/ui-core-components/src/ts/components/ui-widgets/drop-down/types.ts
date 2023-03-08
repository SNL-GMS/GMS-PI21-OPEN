export interface DropDownProps {
  value: string;
  dropDownItems: any;
  dropdownText?: any;
  disabledDropdownOptions?: any;
  widthPx?: number;
  disabled?: boolean;
  title?: string;
  custom?: boolean;
  'data-cy'?: string;
  className?: string;
  label?: string;
  displayLabel?: boolean; // defaults to false for legacy reasons. Set to true if you want a label for your dropdown
  onMaybeValue(value: any);
}
