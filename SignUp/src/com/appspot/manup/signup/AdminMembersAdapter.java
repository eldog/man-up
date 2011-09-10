package com.appspot.manup.signup;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.Member;
import com.appspot.manup.signup.data.MultiCursorLoader;
import com.appspot.manup.signup.ui.MemberAdapter;
import com.appspot.manup.signup.ui.SectionedListAdapter;

final class AdminMemberAdapter extends SectionedListAdapter implements MemberAdapter
{
    public static final String TAG = AdminMemberAdapter.class.getSimpleName();

    private static final int SECTION_PENDING = 0;
    private static final int SECTION_TO_UPLOAD = 1;
    private static final int SECTION_UPLOADED = 2;
    private static final int SECTION_COUNT = 3;

    private static final String[] COLUMNS = { Member._ID, Member.PERSON_ID, Member.GIVEN_NAME,
            Member.SURNAME, Member.EXTRA_INFO_STATE, Member.SIGNATURE_STATE };

    private static final int COL_PERSON_ID = 1;
    private static final int COL_EXTRA_INFO_STATE = 4;
    private static final int COL_SIGNATURE_STATE = 5;

    private final class MemberLoader extends MultiCursorLoader
    {
        @Override
        protected Cursor[] loadCursors()
        {
            return new Cursor[] {
                    mDataManager.queryMembers(
                            COLUMNS,
                            Member.LATEST_PENDING_SIGNATURE_REQUEST + " IS NOT NULL",
                            null /* selection args */,
                            Member.LATEST_PENDING_SIGNATURE_REQUEST + " DESC"),

                    mDataManager.queryMembers(
                            COLUMNS,
                            Member.SIGNATURE_STATE + "=? AND "
                                    + Member.LATEST_PENDING_SIGNATURE_REQUEST + " IS NULL",
                            new String[] { Member.SIGNATURE_STATE_CAPTURED },
                            null /* order by */),

                    mDataManager.queryMembers(
                            COLUMNS,
                            Member.SIGNATURE_STATE + "=? AND "
                                    + Member.LATEST_PENDING_SIGNATURE_REQUEST + " IS NULL",
                            new String[] { Member.SIGNATURE_STATE_UPLOADED },
                            null /* order by */)
            };
        } // loadCursors()

        @Override
        protected void onCursorsLoaded(Cursor[] cursors)
        {
            changeCursors(cursors);
        } // onCursorsLoaded(Cursor[])

        @Override
        protected void cleanUp()
        {
            mMemberLoader = null;
        } // cleanUp()

    } // class MemberLoader

    private final DataManager mDataManager;
    private final LayoutInflater mInflater;

    private MemberLoader mMemberLoader = null;

    public AdminMemberAdapter(final Context context)
    {
        super(context, SECTION_COUNT);
        mDataManager = DataManager.getDataManager(context);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    } // constructor(Context)

    @Override
    protected View newHeader(final Context context, final ViewGroup parent)
    {
        return mInflater.inflate(R.layout.members_list_header, parent, false);
    } // newHeader(Context, int, ViewGroup)

    @Override
    protected void bindHeader(final Context context, final View headerView, final int sectionIndex)
    {
        final int header;
        switch (sectionIndex)
        {
            case SECTION_PENDING:
                header = R.string.pending_signature_requests;
                break;
            case SECTION_TO_UPLOAD:
                header = R.string.signature_captured;
                break;
            case SECTION_UPLOADED:
                header = R.string.signature_uploaded;
                break;
            default:
                throw new AssertionError();
        } // switch
        ((TextView) headerView.findViewById(R.id.header_title)).setText(header);
    } // bindHeader(Conext, View, int)

    @Override
    protected View newItem(final Context context, final ViewGroup parent)
    {
        return mInflater.inflate(R.layout.member_list_item, parent, false);

    } // newItem(Context, ViewGroup)

    @Override
    protected void bindItem(final Context context, final View itemView, final Cursor item,
            final int sectionIndex)
    {
        final TextView headerView = (TextView) itemView.findViewById(R.id.header);
        final TextView subheaderView = (TextView) itemView.findViewById(R.id.subheader);
        final String name = DataManager.getDisplayName(item);
        final String personId = item.getString(COL_PERSON_ID);

        if (!TextUtils.isEmpty(name))
        {
            headerView.setText(name);
        } // if
        else
        {
            headerView.setText(R.string.unknown_name);
        } // else

        subheaderView.setText(personId);

        final ImageView signatureStateImage = (ImageView) itemView
                .findViewById(R.id.signature_state);
        final String signatureState = item.getString(COL_SIGNATURE_STATE);

        if (Member.SIGNATURE_STATE_CAPTURED.equals(signatureState))
        {
            signatureStateImage.setImageResource(R.drawable.saved);
        } // if
        else if (Member.SIGNATURE_STATE_UPLOADED.equals(signatureState))
        {
            signatureStateImage.setImageResource(R.drawable.uploaded);
        } // else if
        else
        {
            // State must be uncaptured.
            signatureStateImage.setImageResource(R.drawable.uncaptured);
        } // else

        final ImageView extraInfoStateImage = (ImageView) itemView
                .findViewById(R.id.extra_info_state);
        final String extraInfoState = item.getString(COL_EXTRA_INFO_STATE);

        if (Member.EXTRA_INFO_STATE_RETRIEVING.equals(extraInfoState))
        {
            extraInfoStateImage.setImageResource(R.drawable.sync);
        } // if
        else if (Member.EXTRA_INFO_STATE_ERROR.equals(extraInfoState))
        {
            extraInfoStateImage.setImageResource(R.drawable.error);
        } // else if
        else if (Member.EXTRA_INFO_STATE_RETRIEVED.equals(extraInfoState))
        {
            extraInfoStateImage.setImageResource(R.drawable.extra_info_retrieved);
        } // else if
        else
        {
            extraInfoStateImage.setImageResource(R.drawable.extra_info_none);
        } // else
    } // bindItem(Context, View, Cursor, int)

    public void loadCursor()
    {
        if (mMemberLoader != null)
        {
            mMemberLoader.cancel(true);
        } // if
        mMemberLoader = (MemberLoader) new MemberLoader().execute();
    } // loadCursor()

    @Override
    public void closeCursor()
    {
        if (mMemberLoader != null)
        {
            mMemberLoader.cancel(true);
        } // if
        super.close();
    } // closeCursor()

} // class MemberAdapter
