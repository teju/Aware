package com.szabh.androiddfu.goodix;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class FileSelectionHelper implements View.OnClickListener {

    @Nullable
    private TextView fileNameTv;
    @Nullable
    private View selectBtn;
    @NonNull
    private final Activity host;
    private int requestCode;
    public File defaultPath;
    public String mimeType;
    public String title;
    public Uri selectedFileUri;
    public String selectedFileName;
    public InputStream selectedFileStream;

    public FileSelectionHelper(@NonNull Activity host) {
        this.host = host;
    }

    public FileSelectionHelper(@NonNull Activity host, @Nullable TextView fileNameTv, @Nullable View selectBtn) {
        this(host);
        this.fileNameTv = fileNameTv;
        this.selectBtn = selectBtn;
        if (selectBtn != null) {
            selectBtn.setOnClickListener(this);
        }
    }

    @Nullable
    public String getSelectedFileName() {
        return selectedFileName;
    }

    @Nullable
    public Uri getSelectedFileUri() {
        return selectedFileUri;
    }

    @Nullable
    public InputStream getSelectedFileStream() {
        return selectedFileStream;
    }

    @Nullable
    public InputStream openInputStream() {
        InputStream r = null;
        if (selectedFileUri != null) {
            ContentResolver resolver = host.getContentResolver();
            try {
                r = resolver.openInputStream(selectedFileUri);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (r != null) {
            selectedFileStream = r;
        }
        return selectedFileStream;
    }

    public boolean closeInputStream(InputStream stream) {
        if (stream != null) {
            try {
                stream.close();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void show() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        if (mimeType != null) {
            intent.setType(mimeType);
        } else {
            intent.setType("*/*");
        }
        if (title != null) {
            intent.putExtra(Intent.EXTRA_TITLE, title);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (defaultPath != null) {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.fromFile(defaultPath));
            } else {
                intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, Uri.fromFile(host.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)));
            }
        }
        requestCode = (int) (System.currentTimeMillis() & 0xFFFF) | 0x1511;
        host.startActivityForResult(intent, requestCode, null);
    }

    /**
     * parse result
     *
     * @return true if get file data.
     */
    @SuppressLint("SetTextI18n")
    public boolean onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        boolean ret = false;
        if (this.requestCode != 0 && requestCode == this.requestCode) {
            this.requestCode = 0;
            if (data != null) {
                final Uri uri = data.getData();
                try {
                    InputStream r = null;
                    String name = null;
                    if (uri != null) {
                        ContentResolver resolver = host.getContentResolver();
                        r = resolver.openInputStream(uri);
                        Cursor query = resolver.query(uri, new String[]{DocumentsContract.Document.COLUMN_DISPLAY_NAME}, null, null, null);
                        if (query != null) {
                            if (query.moveToNext()) {
                                name = query.getString(0);
                            }
                            query.close();
                        }
                    }

                    selectedFileName = name;
                    if (fileNameTv != null) {
                        if (name != null) {
                            fileNameTv.setText(name);
                        } else {
                            fileNameTv.setText("Failed to read file name.");
                        }
                    }

                    if (r != null) {
                        selectedFileUri = uri;
                        selectedFileStream = r;
                        ret = true;
                    } else {
                        selectedFileUri = null;
                        if (fileNameTv != null) {
                            fileNameTv.setText("Failed to read file.");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    if (fileNameTv != null) {
                        fileNameTv.setText(e.getMessage());
                    }
                }
            } else {
                if (selectedFileUri == null && fileNameTv != null) {
                    fileNameTv.setText("No File");
                }
            }
        }
        return ret;
    }

    @Override
    public void onClick(View v) {
        if (v == selectBtn) {
            show();
        }
    }
}
