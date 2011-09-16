package com.appspot.manup.signup;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.Toast;

import com.appspot.manup.signup.ui.BaseActivity;

public final class CaptureSignatureActivity extends BaseActivity
{
    @SuppressWarnings("unused")
    private static final String TAG = CaptureSignatureActivity.class.getSimpleName();

    public static final String ACTION_CAPTURE =
            CaptureSignatureActivity.class.getName() + ".CAPTURE";
    public static final String EXTRA_ID = CaptureSignatureActivity.class.getName() + ".ID";

    private ImageView mSubmit = null;
    private ImageView mBoard = null;
    private volatile SignatureView mSignatureView = null;
    private volatile long mId = Long.MIN_VALUE;

    public CaptureSignatureActivity()
    {
        super();
    } // CaptureSignatureActivity

    @Override
    public void onCreate(final Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.capture_signature);

        final Intent intent = getIntent();
        mId = intent.getLongExtra(EXTRA_ID, mId);

        mSignatureView = (SignatureView) findViewById(R.id.signature);

        mSubmit = (ImageView) findViewById(R.id.submit);
        final Animation flyIn = AnimationUtils.loadAnimation(this, R.anim.fly_in);
        mSubmit.setAnimation(flyIn);
        flyIn.start();

        mBoard = (ImageView) findViewById(R.id.boarder);
    } // onCreate

    @Override
    protected void onPause()
    {
        mSignatureView.cancelAnimation();
        super.onPause();
    } // onPause

    public void onSubmit(final View view)
    {
        if (mSignatureView.isClear())
        {
            Toast.makeText(this, R.string.please_sign, Toast.LENGTH_SHORT).show();
            return;
        } // if

        if (mId != Long.MIN_VALUE)
        {
            WriteSignatureService.writeSignature(this, mId, mSignatureView.getDoodle());
        } // if

        mSignatureView.setEnabled(false);

        mSubmit.clearAnimation();
        final Animation flyOut = AnimationUtils.loadAnimation(this, R.anim.fly_out);
        mSubmit.setAnimation(flyOut);
        flyOut.setAnimationListener(new AnimationListener()
        {

            @Override
            public void onAnimationStart(Animation animation)
            {
            }

            @Override
            public void onAnimationRepeat(Animation animation)
            {
            }

            @Override
            public void onAnimationEnd(Animation animation)
            {
                findViewById(R.id.name).setVisibility(View.VISIBLE);
                final Animation flyIn2 = AnimationUtils.loadAnimation(
                        CaptureSignatureActivity.this, R.anim.flay_in_2);
                flyIn2.setAnimationListener(new AnimationListener()
                {

                    @Override
                    public void onAnimationStart(Animation animation)
                    {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation)
                    {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void onAnimationEnd(Animation animation)
                    {
                        new Thread()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    sleep(1000);
                                }
                                catch (InterruptedException e)
                                {
                                }
                                finish();
                            }
                        }.start();
                    }
                });
                mBoard.setAnimation(flyIn2);
                mBoard.setVisibility(View.VISIBLE);
                flyIn2.start();
            }
        });
        flyOut.start();
        mSignatureView.animate(null);
    } // onSubmit

} // class CaptureSignatureActivity

