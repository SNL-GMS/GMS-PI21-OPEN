// Types for Loading Spinner
export interface LoadingSpinnerProps {
  // How many things are being loading
  itemsToLoad: number;
  // If not provided, the spinner will spin and the number of requested items will be displayed
  itemsLoaded?: number;
  hideTheWordLoading?: boolean;
  hideOutstandingCount?: boolean;
  onlyShowSpinner: boolean;
  // String to display next to loading number
  label: string;
  widthPx?: number;
}
