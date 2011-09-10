package com.appspot.manup.signup;

import com.appspot.manup.signup.data.CursorLoader;
import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.Member;
import com.appspot.manup.signup.ui.MemberAdapter;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

final class UserMemberAdapter extends CursorAdapter implements MemberAdapter
{
    private final class MemberLoader extends CursorLoader
    {
        MemberLoader()
        {
            super();
        } // constructor()

        @Override
        protected Cursor loadCursor()
        {
            return DataManager.getDataManager(mContext).getMembers();
        } // loadCursor()

        @Override
        protected void onCursorLoaded(final Cursor cursor)
        {
            mLoader = null;
            changeCursor(cursor);
        } // onCursorLoaded(Cursor)

    } // class MemberLoader

    private final Context mContext;
    private final LayoutInflater mInflater;

    private MemberLoader mLoader = null;
    private int mPersonIdCol = Integer.MIN_VALUE;

    public UserMemberAdapter(final Context context)
    {
        super(context, null /* no cursor */);
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    } // constructor(Context)

    @Override
    public View newView(final Context context, final Cursor member, final ViewGroup parent)
    {
        return mInflater.inflate(R.layout.member_list_item, parent, false);
    } // newView(Context, Cursor, ViewGroup)

    @Override
    public void bindView(final View memberView, final Context context, final Cursor member)
    {
        final String name = DataManager.getDisplayName(member);
        final TextView nameView = (TextView) memberView.findViewById(R.id.header);
        if (TextUtils.isEmpty(name))
        {
            nameView.setText(R.string.unknown_name);
            nameView.setEnabled(false);
        } // if
        else
        {
            nameView.setText(name);
            nameView.setEnabled(true);
        } // else
        ((TextView) memberView.findViewById(R.id.subheader)).setText(
                member.getString(mPersonIdCol));
    } // bindView(View, Context, Cursor)

    @Override
    public void changeCursor(final Cursor cursor)
    {
        super.changeCursor(cursor);
        if (cursor != null)
        {
            mPersonIdCol = cursor.getColumnIndexOrThrow(Member.PERSON_ID);
        } // if
    } // changeCursor(Cursor)

    public void loadCursor()
    {
        closeCursor();
        mLoader = (MemberLoader) new MemberLoader().execute();
    } // loadCursor()

    public void closeCursor()
    {
        if (mLoader != null)
        {
            mLoader.cancel(true);
            mLoader = null;
        } // if
        final Cursor current = getCursor();
        if (current != null)
        {
            current.close();
        } // if
    } // closeCursor()

} // class UserMemberAdapter
