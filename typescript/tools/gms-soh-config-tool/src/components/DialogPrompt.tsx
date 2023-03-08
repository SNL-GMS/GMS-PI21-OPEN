import { Button, Dialog, DialogActions, DialogTitle } from '@mui/material';
import React from 'react';

interface DialogPromptProps {
  message: string;
  isOpen: boolean;
  onConfirm: (confirmation: boolean) => void;
}

export const DialogPrompt: React.FC<
  React.PropsWithChildren<DialogPromptProps>
> = (props: React.PropsWithChildren<DialogPromptProps>) => {
  const { message, isOpen, onConfirm } = props;

  const [confirmToggle, setConfirmToggle] = React.useState({
    open: false,
  });

  return (
    <Dialog
      open={confirmToggle.open || isOpen}
      onClose={() => setConfirmToggle({ open: false })}
      aria-labelledby='alert-dialog-title'
      aria-describedby='alert-dialog-description'
    >
      {' '}
      <DialogTitle id='alert-dialog-title'>{`${message}`}</DialogTitle>
      <DialogActions>
        <Button
          onClick={() => {
            onConfirm(false);
            setConfirmToggle({
              open: false,
            });
          }}
        >
          Cancel
        </Button>
        <Button
          onClick={() => {
            onConfirm(true);
            setConfirmToggle({
              open: false,
            });
          }}
          autoFocus
        >
          Continue
        </Button>
      </DialogActions>
    </Dialog>
  );
};
