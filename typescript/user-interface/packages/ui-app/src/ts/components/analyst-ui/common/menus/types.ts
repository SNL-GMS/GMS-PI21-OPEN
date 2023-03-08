export interface HideStationMenuItemProps {
  stationName: string;
  hideStationCallback: (stationName: any) => void;
  showHideText?: string;
  disabled?: boolean;
}
