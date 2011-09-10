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
    private static final int PERSON_IS_COL = 1;

    private final class MemberLoader extends CursorLoader
    {
        MemberLoader()
        {
            super();
        } // constructor()

        @Override
        protected Cursor loadCursor()
        {
            return DataManager.getDataManager(mContext).queryMembers(
                    new String[] {
                            Member._ID,
                            Member.PERSON_ID,
                            Member.GIVEN_NAME,
                            Member.SURNAME },
                    null /* selection */,
                    null /* selection args */,
                    Member.LATEST_PENDING_SIGNATURE_REQUEST + " DESC");
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
                member.getString(PERSON_IS_COL));
    } // bindView(View, Context, Cursor)

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
