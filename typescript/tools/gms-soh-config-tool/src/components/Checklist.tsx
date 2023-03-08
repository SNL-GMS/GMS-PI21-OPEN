import {
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  Checkbox,
  ListItemText,
} from '@mui/material';
import React from 'react';

/**
 * The type of the props for the {@link Checklist} component
 */
export interface ChecklistProps {
  checkboxes: string[];
  checkedBoxes: string[] | undefined;
  nonIdealState: string | JSX.Element;
  listItemClass?: string;
  disabledCheckboxes?: string[];
  buttonClass?: string;
  alignItems?: 'center' | 'flex-start' | undefined;
  helpText?: (checkbox: string) => string;
  disabledText?: (checkbox: string) => string;
  renderRightElement?: (checkbox: string) => JSX.Element;
  WrapperComponent?: React.FC<
    React.PropsWithChildren<{ checkboxName: string }>
  >;
  handleToggle: (checkedBoxes: string[], checkbox: string) => void;
}

/**
 * This creates a generic checklist component
 */
export const Checklist: React.FC<ChecklistProps> = ({
  checkboxes,
  handleToggle,
  checkedBoxes,
  disabledCheckboxes,
  nonIdealState,
  renderRightElement,
  listItemClass,
  buttonClass,
  alignItems,
  helpText,
  disabledText,
  WrapperComponent,
}: ChecklistProps) => {
  const internalHandleToggle = React.useCallback(
    (checkbox: string) => (event: React.MouseEvent<HTMLDivElement>) => {
      event.stopPropagation();
      if (
        !checkbox ||
        !checkedBoxes ||
        (disabledCheckboxes && disabledCheckboxes.includes(checkbox))
      ) {
        return;
      }
      const currentIndex = checkedBoxes.indexOf(checkbox);
      const newChecked = [...checkedBoxes];

      if (currentIndex === -1) {
        newChecked.push(checkbox);
      } else {
        newChecked.splice(currentIndex, 1);
      }

      handleToggle(newChecked, checkbox);
    },
    [checkedBoxes, disabledCheckboxes, handleToggle]
  );

  const determineText = (
    isDisabled: boolean | undefined,
    checkbox: string
  ): string | undefined => {
    if (isDisabled && disabledText) {
      return disabledText(checkbox);
    }
    if (!isDisabled && helpText) {
      return helpText(checkbox);
    }
    return undefined;
  };
  return (
    <List sx={{ width: '100%', bgcolor: 'background.paper' }}>
      {checkboxes && checkboxes.length > 0
        ? checkboxes.map((checkbox) => {
            const labelId = `checklist-${checkbox}`;
            const isDisabled =
              !checkedBoxes ||
              (disabledCheckboxes && disabledCheckboxes.includes(checkbox));
            const ListContents = (
              <>
                <ListItemButton
                  className={buttonClass}
                  role={undefined}
                  onClick={internalHandleToggle(checkbox)}
                  dense
                  title={determineText(isDisabled, checkbox)}
                >
                  <ListItemIcon>
                    <Checkbox
                      edge='start'
                      checked={
                        !!checkedBoxes && checkedBoxes.indexOf(checkbox) !== -1
                      }
                      tabIndex={-1}
                      disabled={isDisabled}
                      disableRipple
                      inputProps={{ 'aria-labelledby': labelId }}
                    />
                  </ListItemIcon>
                  <ListItemText id={labelId} primary={checkbox} />
                </ListItemButton>
                {renderRightElement && renderRightElement(checkbox)}
              </>
            );
            return (
              <ListItem
                alignItems={alignItems}
                className={listItemClass}
                key={`checklist-checkbox-${checkbox}`}
                disablePadding
              >
                {WrapperComponent ? (
                  <WrapperComponent checkboxName={checkbox}>
                    {ListContents}
                  </WrapperComponent>
                ) : (
                  ListContents
                )}
              </ListItem>
            );
          })
        : nonIdealState}
    </List>
  );
};
