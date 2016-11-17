package org.qstuff.qplayer.ui.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import org.qstuff.qplayer.R;


public class StoragePermissionStatementDialogFragment extends DialogFragment {

    public interface DialogListener {
        void onPositiveButtonClicked();
    }

    private DialogListener dialogListener;

    public static StoragePermissionStatementDialogFragment newInstance() {
        return new StoragePermissionStatementDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setMessage(R.string.storage_permission_request_message)
                .setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogListener.onPositiveButtonClicked();
                        dialogInterface.dismiss();
                    }
                })
                .create();
    }

    public void setDialogListener(DialogListener dialogListener) {
        this.dialogListener = dialogListener;
    }

}
