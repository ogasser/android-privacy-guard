/*
 * Copyright (C) 2010 Thialfihar <thi@thialfihar.org>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thialfihar.android.apg.ui.widget;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Vector;

import org.bouncycastle2.openpgp.PGPSecretKey;
import org.thialfihar.android.apg.Apg;
import org.thialfihar.android.apg.R;
import org.thialfihar.android.apg.utils.Choice;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class KeyEditor extends LinearLayout implements Editor, OnClickListener {
    private PGPSecretKey mKey;

    private EditorListener mEditorListener = null;

    private boolean mIsMasterKey;
    ImageButton mDeleteButton;
    TextView mAlgorithm;
    TextView mKeyId;
    Spinner mUsage;
    TextView mCreationDate;
    Button mExpiryDateButton;
    GregorianCalendar mExpiryDate;

    private DatePickerDialog.OnDateSetListener mExpiryDateSetListener =
            new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    GregorianCalendar date = new GregorianCalendar(year, monthOfYear, dayOfMonth);
                    setExpiryDate(date);
                }
            };

    public static class AlgorithmChoice extends Choice {
        public static final int DSA = 1;
        public static final int ELGAMAL = 2;
        public static final int RSA = 3;

        public AlgorithmChoice(int id, String name) {
            super(id, name);
        }
    }

    public static class UsageChoice extends Choice {
        public static final int SIGN_ONLY = 1;
        public static final int ENCRYPT_ONLY = 2;
        public static final int SIGN_AND_ENCRYPT = 3;

        public UsageChoice(int id, String name) {
            super(id, name);
        }
    }

    public KeyEditor(Context context) {
        super(context);
    }

    public KeyEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onFinishInflate() {
        setDrawingCacheEnabled(true);
        setAlwaysDrawnWithCacheEnabled(true);

        mAlgorithm = (TextView) findViewById(R.id.algorithm);
        mKeyId = (TextView) findViewById(R.id.key_id);
        mCreationDate = (TextView) findViewById(R.id.creation);
        mExpiryDateButton = (Button) findViewById(R.id.expiry);
        mUsage = (Spinner) findViewById(R.id.usage);
        KeyEditor.UsageChoice choices[] = {
                new KeyEditor.UsageChoice(KeyEditor.UsageChoice.SIGN_ONLY,
                                          getResources().getString(R.string.sign_only)),
                new KeyEditor.UsageChoice(KeyEditor.UsageChoice.ENCRYPT_ONLY,
                                          getResources().getString(R.string.encrypt_only)),
                new KeyEditor.UsageChoice(KeyEditor.UsageChoice.SIGN_AND_ENCRYPT,
                                          getResources().getString(R.string.sign_and_encrypt)),
        };
        ArrayAdapter<KeyEditor.UsageChoice> adapter =
                new ArrayAdapter<KeyEditor.UsageChoice>(getContext(),
                                                        android.R.layout.simple_spinner_item,
                                                        choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUsage.setAdapter(adapter);

        mDeleteButton = (ImageButton) findViewById(R.id.edit_delete);
        mDeleteButton.setOnClickListener(this);

        setExpiryDate(null);

        mExpiryDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                GregorianCalendar date = mExpiryDate;
                if (date == null) {
                    date = new GregorianCalendar();
                }

                DatePickerDialog dialog =
                        new DatePickerDialog(getContext(), mExpiryDateSetListener,
                                             date.get(Calendar.YEAR),
                                             date.get(Calendar.MONTH),
                                             date.get(Calendar.DAY_OF_MONTH));
                dialog.setCancelable(true);
                dialog.setButton(Dialog.BUTTON_NEGATIVE, "None",
                                 new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        setExpiryDate(null);
                    }
                });
                dialog.show();
            }
        });

        super.onFinishInflate();
    }

    public void setValue(PGPSecretKey key, boolean isMasterKey) {
        mKey = key;

        mIsMasterKey = isMasterKey;
        if (mIsMasterKey) {
            mDeleteButton.setVisibility(View.INVISIBLE);
        }

        mAlgorithm.setText(Apg.getAlgorithmInfo(key));
        String keyId1Str = Long.toHexString((key.getKeyID() >> 32) & 0xffffffffL);
        while (keyId1Str.length() < 8) {
            keyId1Str = "0" + keyId1Str;
        }
        String keyId2Str = Long.toHexString(key.getKeyID() & 0xffffffffL);
        while (keyId2Str.length() < 8) {
            keyId2Str = "0" + keyId2Str;
        }
        mKeyId.setText(keyId1Str + " " + keyId2Str);

        Vector<KeyEditor.UsageChoice> choices = new Vector<KeyEditor.UsageChoice>();
        choices.add(new KeyEditor.UsageChoice(KeyEditor.UsageChoice.SIGN_ONLY,
                                              getResources().getString(R.string.sign_only)));
        if (!mIsMasterKey) {
            choices.add(new KeyEditor.UsageChoice(KeyEditor.UsageChoice.ENCRYPT_ONLY,
                                                  getResources().getString(R.string.encrypt_only)));
        }
        choices.add(new KeyEditor.UsageChoice(KeyEditor.UsageChoice.SIGN_AND_ENCRYPT,
                                              getResources().getString(R.string.sign_and_encrypt)));

        ArrayAdapter<KeyEditor.UsageChoice> adapter =
                new ArrayAdapter<KeyEditor.UsageChoice>(getContext(),
                                                        android.R.layout.simple_spinner_item,
                                                        choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mUsage.setAdapter(adapter);

        if (Apg.isEncryptionKey(key)) {
            if (Apg.isSigningKey(key)) {
                mUsage.setSelection(2);
            } else {
                mUsage.setSelection(1);
            }
        } else {
            mUsage.setSelection(0);
        }

        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(Apg.getCreationDate(key));
        mCreationDate.setText(DateFormat.getDateInstance().format(cal.getTime()));
        cal = new GregorianCalendar();
        Date date = Apg.getExpiryDate(key);
        if (date == null) {
            setExpiryDate(null);
        } else {
            cal.setTime(Apg.getExpiryDate(key));
            setExpiryDate(cal);
        }
    }

    public PGPSecretKey getValue() {
        return mKey;
    }

    @Override
    public void onClick(View v) {
        final ViewGroup parent = (ViewGroup)getParent();
        if (v == mDeleteButton) {
            parent.removeView(this);
            if (mEditorListener != null) {
                mEditorListener.onDeleted(this);
            }
        }
    }

    @Override
    public void setEditorListener(EditorListener listener) {
        mEditorListener = listener;
    }

    private void setExpiryDate(GregorianCalendar date) {
        mExpiryDate = date;
        if (date == null) {
            mExpiryDateButton.setText(R.string.none);
        } else {
            mExpiryDateButton.setText(DateFormat.getDateInstance().format(date.getTime()));
        }
    }

    public GregorianCalendar getExpiryDate() {
     return mExpiryDate;
    }

    public UsageChoice getUsage() {
        return (UsageChoice) mUsage.getSelectedItem();
    }
}
