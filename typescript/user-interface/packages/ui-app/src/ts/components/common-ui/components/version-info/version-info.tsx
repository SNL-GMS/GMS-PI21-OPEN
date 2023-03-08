import { VERSION_INFO } from '@gms/common-util';
import { CopyContents } from '@gms/ui-core-components';
import React from 'react';

/**
 * Creates a version hash that can be clicked and copied
 */
export function VersionInfo({ showBranch = true }: { showBranch?: boolean }) {
  return (
    <CopyContents
      className="version-info"
      clipboardText={`${VERSION_INFO.versionNumber}${process.env.GIT_COMMITHASH}`}
      tooltipLabel="Copy version and full hash"
    >
      {showBranch ? `${VERSION_INFO.versionNumber} \u2014 ` : ''}
      {VERSION_INFO.commitSHA}
    </CopyContents>
  );
}
