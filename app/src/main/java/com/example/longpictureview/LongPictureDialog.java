package com.example.longpictureview;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.target.Target;

import java.io.File;

public class LongPictureDialog extends DialogFragment {
    private static final String TAG = LongPictureDialog.class.getSimpleName();

    private String mPictureUrl;

    public static LongPictureDialog newInstance() {
        return new LongPictureDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_long_picture, container, false);

        setDialogStyle();
        setCloseListener(view.findViewById(R.id.icon_close));
        loadPicture((LongPictureView) view.findViewById(R.id.view_long_picture));

        return view;
    }

    private void setDialogStyle() {
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.setCancelable(false);
            dialog.setCanceledOnTouchOutside(true);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
    }

    private void setCloseListener(View view) {
        if (view == null) {
            return;
        }
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

    private void loadPicture(final LongPictureView view) {
        if (view == null) {
            return;
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    FutureTarget<File> target = Glide.with(view.getContext())
                            .asFile()
                            .load(mPictureUrl)
                            .submit(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
                    final String fileUrl = target.get().getPath();
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.setImage(fileUrl);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public LongPictureDialog setPictureUrl(String url) {
        this.mPictureUrl = url;
        return this;
    }

    public void show(FragmentManager fm) {
        if (fm == null || TextUtils.isEmpty(mPictureUrl)) {
            return;
        }

        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }
        ft.addToBackStack(null);

        show(ft, TAG);
    }
}
