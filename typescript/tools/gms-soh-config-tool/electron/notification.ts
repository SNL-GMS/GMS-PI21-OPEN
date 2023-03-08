
import { Notification } from 'electron';

// display files added notification
export const filesAdded = ( file: number | string ) => {
    const notification = new Notification( {
        title: 'Files added',
        body: `${ file } file(s) has been successfully added.`,
        silent: true
    } );

    notification.show();
};

// display files added notification
export const message = ( text: string ) => {
    const notification = new Notification( {
        title: 'Message',
        body: text,
        silent: true
    } );

    notification.show();
};

// display files added notification
export const error = ( text: string ) => {
    const notification = new Notification( {
        title: 'Error',
        body: text
    } );

    notification.show();
};