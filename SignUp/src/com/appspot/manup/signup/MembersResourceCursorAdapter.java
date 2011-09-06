package com.appspot.manup.signup;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.widget.ImageView;
import android.widget.SimpleCursorAdapter;

import com.appspot.manup.signup.data.DataManager.Member;

public class MembersResourceCursorAdapter extends SimpleCursorAdapter
{
    public MembersResourceCursorAdapter(final Context context, final int layout, final Cursor c,
            final String[] from, final int[] to)
    {
        super(context, layout, c, from, to);
    } // MembersResourceCursorAdapter

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor)
    {
        super.bindView(view, context, cursor);
        setExtraInfoImage(view, cursor);
        setSignatureImage(view, cursor);
    } // bindView

    private void setSignatureImage(final View view, final Cursor cursor)
    {
        final ImageView image = (ImageView) view.findViewById(R.id.signature_state);
        final String state = cursor
                .getString(cursor.getColumnIndexOrThrow(Member.SIGNATURE_STATE));
        if (Member.SIGNATURE_STATE_CAPTURED.equals(state))
        {
            image.setImageResource(R.drawable.ic_menu_save);
        }
        else if (Member.SIGNATURE_STATE_UPLOADED.equals(state))
        {
            image.setImageResource(R.drawable.ic_menu_upload);
        }
    } // setSignatureImage

    private void setExtraInfoImage(final View view, final Cursor cursor)
    {
        final ImageView image = (ImageView) view.findViewById(R.id.extra_info_state);
        final String state = cursor
                .getString(cursor.getColumnIndexOrThrow(Member.EXTRA_INFO_STATE));
        if (Member.EXTRA_INFO_STATE_RETRIEVING.equals(state))
        {
            image.setImageResource(R.drawable.ic_popup_sync);
        }
        else if (Member.EXTRA_INFO_STATE_ERROR.equals(state))
        {
            image.setImageResource(R.drawable.ic_delete);
        }
    } // setExtraInfoImage
} // MembersResourceCursorAdapter
