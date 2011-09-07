package com.appspot.manup.signup;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.manup.signup.data.DataManager.Member;

public class MembersCursorAdapter extends CursorAdapter
{
    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_MEMBER = 1;
    private static final int VIEW_TYPE_NUM = 2;
    private static final int HEADER_PENDING = 0;
    private static final int HEADER_TO_UPLOAD = 1;
    private static final int HEADER_UPLOADED = 2;

    private final Context mContext;

    private final boolean[] mHeaderVisible = { false, false, false };
    private final int[] mHeaderListPos =
    { Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE };

    public MembersCursorAdapter(final Context context)
    {
        super(context, null);
        mContext = context;
    } // MembersResourceCursorAdapter

    @Override
    public void changeCursor(final Cursor cursor)
    {
        configHeaderPending(cursor);
        configHeaderToUpload(cursor);
        configHeaderUploaded(cursor);
        super.changeCursor(cursor);
    } // changeCursor

    private void configHeaderPending(final Cursor cursor)
    {
        final boolean visible = cursor != null &&
                cursor.moveToFirst() && cursor.isNull(cursor.getColumnIndexOrThrow(
                        Member.LATEST_PENDING_SIGNATURE_REQUEST));
        mHeaderVisible[HEADER_PENDING] = visible;
        if (visible)
        {
            mHeaderListPos[HEADER_PENDING] = 0;
        }
    }// configHeaderPending(Cursor)

    private void configHeaderToUpload(final Cursor cursor)
    {
        boolean visible = false;
        if (cursor != null && cursor.moveToFirst())
        {
            final int pendingCol = cursor.getColumnIndexOrThrow(
                    Member.LATEST_PENDING_SIGNATURE_REQUEST);
            final int sigState = cursor.getColumnIndexOrThrow(
                    Member.SIGNATURE_STATE);

            for (boolean prevIsNull = true; cursor.moveToNext();)
            {
                final boolean isNull = cursor.isNull(pendingCol);
                if (prevIsNull && isNull
                        && Member.SIGNATURE_STATE_CAPTURED.equals(cursor.getString(sigState)))
                {
                    visible = true;
                    break;
                } // if
                prevIsNull = isNull;
            } // for
        } // if
        mHeaderVisible[HEADER_TO_UPLOAD] = visible;
        if (visible)
        {
            mHeaderListPos[HEADER_TO_UPLOAD] = cursor.getPosition()
                    + (mHeaderVisible[HEADER_PENDING] ? 1 : 0);
        } // if
    } // configHeaderToUpload(Cursor)

    private void configHeaderUploaded(final Cursor cursor)
    {
        boolean visible = false;
        if (cursor != null && cursor.moveToFirst())
        {
            final int sigState = cursor.getColumnIndexOrThrow(
                    Member.SIGNATURE_STATE);

            if (mHeaderVisible[HEADER_TO_UPLOAD])
            {
                for (boolean preIsCaptured = true; cursor.moveToNext();)
                {
                    if (preIsCaptured
                            && Member.SIGNATURE_STATE_UPLOADED.equals(cursor.getString(sigState)))
                    {
                        visible = true;
                        break;
                    } // if
                    preIsCaptured = Member.SIGNATURE_STATE_CAPTURED.equals(
                            cursor.getString(sigState));
                } // for
            } // if
            else if (mHeaderVisible[HEADER_PENDING])
            {
                final int pendingCol = cursor.getColumnIndexOrThrow(
                        Member.LATEST_PENDING_SIGNATURE_REQUEST);

                for (boolean prevIsNull = true; cursor.moveToNext();)
                {
                    final boolean isNull = cursor.isNull(pendingCol);
                    if (prevIsNull && isNull
                            && Member.SIGNATURE_STATE_UPLOADED.equals(cursor.getString(sigState)))
                    {
                        visible = true;
                        break;
                    } // if
                    prevIsNull = isNull;
                } // for
            } // if
            else
            {
                visible = cursor != null && cursor.moveToFirst()
                        && Member.SIGNATURE_STATE_UPLOADED.equals(cursor.getString(sigState));
            } // else

            mHeaderVisible[HEADER_UPLOADED] = visible;
            if (visible)
            {
                int headerCount = 0;
                for (int i = HEADER_PENDING; i < HEADER_UPLOADED; i++)
                {
                    if (mHeaderVisible[i])
                    {
                        headerCount++;
                    } // if
                } // for
                mHeaderListPos[HEADER_UPLOADED] = cursor.getPosition() + headerCount;
            } // if
        } // if
    } // configHeaderUploaded(Cursor)

    private int getCursorPositionInList(int listPosition)
    {
        for (int i = 0; i < mHeaderVisible.length; i++)
        {
            if (mHeaderVisible[i])
            {
                if (listPosition > mHeaderListPos[i])
                {
                    listPosition--;
                } // if
            } // if
        } // for

        return listPosition;
    } // getCursorPositionInList

    @Override
    public Object getItem(final int position)
    {
        for (int i = 0; i < mHeaderListPos.length; i++){
            if (position == mHeaderListPos[i]){
                return null;
            } // if
        } // for
        return super.getItem(position);
    } // getItem

    @Override
    public View getView(final int listPosition, final View convertView, final ViewGroup parent)
    {
        final Cursor c = getCursor();
        if (c == null)
        {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        } // if

        final int cursorPosition = getCursorPositionInList(listPosition);
        if (!c.moveToPosition(cursorPosition))
        {
            throw new IllegalStateException("couldn't move cursor to position " +
                    cursorPosition);
        } // if

        final int viewType = getItemViewType(listPosition);
        final View v;
        if (convertView == null)
        {
            switch (viewType)
            {
                case VIEW_TYPE_HEADER:
                    v = newHeaderView(mContext, parent, listPosition);
                    break;
                case VIEW_TYPE_MEMBER:
                    v = newView(mContext, c, parent);
                    break;
                default:
                    return null;
            } // switch
        } // if
        else
        {
            v = convertView;
        } // else

        if (viewType == VIEW_TYPE_MEMBER)
        {
            bindView(v, mContext, c);
        } // if
        return v;
    } // getView

    @Override
    public int getItemViewType(final int position)
    {
        for (int i = 0; i < mHeaderListPos.length; i++)
        {
            if (position == mHeaderListPos[i])
            {
                return VIEW_TYPE_HEADER;
            } // if
        } // for
        return VIEW_TYPE_MEMBER;
    } // getItemViewType

    @Override
    public int getViewTypeCount()
    {
        return VIEW_TYPE_NUM;
    } // getViewTypeCount

    @Override
    public int getCount()
    {
        final int count = super.getCount();
        if (count == 0)
        {
            return count;
        } // if
        else
        {
            int headerCount = 0;
            for (int i = 0; i < mHeaderVisible.length; i++)
            {
                if (mHeaderVisible[i])
                {
                    headerCount++;
                } // if
            } // for
            return count + headerCount;
        } // else
    } // getCount

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

    public View newHeaderView(final Context context, final ViewGroup parent, final int position)
    {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.members_list_header, parent, false);

        if (position == mHeaderListPos[HEADER_PENDING])
        {
            ((TextView) v.findViewById(R.id.header_title)).setText("Pending");
        }
        else if (position == mHeaderListPos[HEADER_TO_UPLOAD])
        {
            ((TextView) v.findViewById(R.id.header_title)).setText("Ready to Upload");
        }
        else if (position == mHeaderListPos[HEADER_UPLOADED])
        {
            ((TextView) v.findViewById(R.id.header_title)).setText("Uploaded");
        }
        return v;
    } // newHeaderView

    @Override
    public View newView(final Context context, final Cursor cursor, final ViewGroup parent)
    {
        final LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View v = inflater.inflate(R.layout.member_list_item, parent, false);
        ((TextView) v.findViewById(R.id.person_id)).setText(cursor.getString(cursor
                .getColumnIndexOrThrow(Member.PERSON_ID)));
        final TextView givenName = (TextView) v.findViewById(R.id.given_name);
        givenName.setText(cursor.getString(cursor.getColumnIndexOrThrow(Member.GIVEN_NAME)));
        setFieldVisibility(givenName);
        final TextView surname = (TextView) v.findViewById(R.id.surname);
        surname.setText(cursor.getString(cursor.getColumnIndexOrThrow(Member.SURNAME)));
        setFieldVisibility(surname);
        return v;
    } // newView

    private void setFieldVisibility(final TextView view)
    {
        if (TextUtils.isEmpty(view.getText()))
        {
            view.setVisibility(View.GONE);
        } // if
    } // setFieldVisibility

    @Override
    public void bindView(final View view, final Context context, final Cursor cursor)
    {
        setExtraInfoImage(view, cursor);
        setSignatureImage(view, cursor);
    } // bindView
} // MembersResourceCursorAdapter
