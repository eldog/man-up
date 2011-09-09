package com.appspot.manup.signup;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.appspot.manup.signup.data.DataManager;
import com.appspot.manup.signup.data.DataManager.Member;
import com.appspot.manup.signup.ui.SectionedListAdapter;

final class MemberAdapter extends SectionedListAdapter
{
    public static final String TAG = MemberAdapter.class.getSimpleName();

    private static final int SECTION_PENDING = 0;
    private static final int SECTION_TO_UPLOAD = 1;
    private static final int SECTION_UPLOADED = 2;
    private static final int SECTION_COUNT = 3;

    private final class MemberLoader extends AsyncTask<Void, Void, Cursor[]>
    {
        private volatile Cursor[] mCursors = null;

        @Override
        protected Cursor[] doInBackground(final Void... noParams)
        {
            mCursors = new Cursor[] {
                    mDataManager.getMembersWithPendingRequests(),
                    mDataManager.getMembersWithSignaturesAndNoRequests(),
                    mDataManager.getMembersWithUploadedSignaturesAndNoRequests() };
            return mCursors;
        } // doInBackground(Void)

        @Override
        protected void onCancelled()
        {
            mMemberLoader = null;
            if (mCursors != null)
            {
                for (final Cursor cursor : mCursors)
                {
                    if (cursor != null)
                    {
                        cursor.close();
                    } // if
                } // for
            } // if
        } // onCancelled

        @Override
        protected void onPostExecute(final Cursor[] cursors)
        {
            mMemberLoader = null;
            final Cursor c = cursors[0];
            mPersonIdCol = c.getColumnIndexOrThrow(Member.PERSON_ID);
            mExtraInfoStateCol = c.getColumnIndexOrThrow(Member.EXTRA_INFO_STATE);
            mSignatureStateCol = c.getColumnIndexOrThrow(Member.SIGNATURE_STATE);
            changeCursors(cursors);
        } // onPostExecute(Cursor)

    } // class MemberLoader

    private final DataManager mDataManager;
    private final LayoutInflater mInflater;

    private MemberLoader mMemberLoader = null;
    private int mPersonIdCol = Integer.MIN_VALUE;
    private int mExtraInfoStateCol = Integer.MIN_VALUE;
    private int mSignatureStateCol = Integer.MIN_VALUE;

    public MemberAdapter(final Context context)
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
        final String personId = item.getString(mPersonIdCol);
        if (!TextUtils.isEmpty(name))
        {
            headerView.setText(name);
            subheaderView.setText(personId);
            subheaderView.setVisibility(View.VISIBLE);
        } // if
        else
        {
            headerView.setText(item.getString(mPersonIdCol));
            subheaderView.setVisibility(View.GONE);
        } // else

        final ImageView signatureStateImage = (ImageView) itemView
                .findViewById(R.id.signature_state);
        final String signatureState = item.getString(mSignatureStateCol);

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
        final String extraInfoState = item.getString(mExtraInfoStateCol);

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
    public void close()
    {
        if (mMemberLoader != null)
        {
            mMemberLoader.cancel(true);
        } // if
        super.close();
    } // closeCursor()

} // class MemberAdapter
