package com.appspot.manup.signup.ui;

import android.widget.ListAdapter;

public interface MemberAdapter extends ListAdapter
{
    void loadCursor();
    void closeCursor();
}
