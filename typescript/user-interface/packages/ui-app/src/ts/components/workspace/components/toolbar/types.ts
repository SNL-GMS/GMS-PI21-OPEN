import type { CommonTypes, UserProfileTypes } from '@gms/common-model';
import type { SetLayoutArgs } from '@gms/ui-state';

import type { GLMap } from '../golden-layout/types';

export interface ToolbarBaseProps {
  components: GLMap;
  logo: any;
  userName: string;
  isAboutDialogOpen: boolean;
  isSaveWorkspaceAsDialogOpen: boolean;
  openLayoutName: string;
  versionInfo: CommonTypes.VersionInfo;
  userProfile?: UserProfileTypes.UserProfile;
  saveDialog: JSX.Element;
  setLayout(args: SetLayoutArgs): Promise<void>;
  getOpenDisplays(): string[];
  clearLayout(): void;
  logout(): void;
  openDisplay(displayKey: string): void;
  openWorkspace(layout: UserProfileTypes.UserLayout): void;
  toggleSaveWorkspaceAsDialog(): void;
  showLogPopup(): void;
  showAboutDialog(): void;
  setOpenLayoutName(name: string);
}

export type DeprecatedToolbarProps = ToolbarBaseProps;
