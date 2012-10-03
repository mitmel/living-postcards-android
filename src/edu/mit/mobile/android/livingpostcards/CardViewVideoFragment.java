package edu.mit.mobile.android.livingpostcards;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.utils.StreamUtils;

public class CardViewVideoFragment extends Fragment implements LoaderCallbacks<Uri> {

    /**
     * The card URI
     */
    public static final String ARGUMENT_URI = "uri";
    public static final String ARGUMENT_URI_STRING = "uri_string";

    private VideoView mVideoView;

    public static final String TAG = CardViewVideoFragment.class.getSimpleName();

    public static final int LOADER_VIDEO = 100, LOADER_CARD = 101;

    private class PlaybackHandler implements OnPreparedListener, OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.setLooping(true);
        }
    };

    private final PlaybackHandler mPlaybackHandler = new PlaybackHandler();

    public static CardViewVideoFragment newInstance(Uri card) {
        final CardViewVideoFragment cmf = new CardViewVideoFragment();

        final Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_URI, card);
        cmf.setArguments(args);
        return cmf;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.card_view_video_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        mVideoView = (VideoView) view.findViewById(R.id.video);
        mVideoView.setOnPreparedListener(mPlaybackHandler);
        mVideoView.setOnCompletionListener(mPlaybackHandler);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        final Bundle args = new Bundle();
        final Bundle fragmentArgs = getArguments();
        if (fragmentArgs != null) {
            args.putParcelable(ARGUMENT_URI, fragmentArgs.getParcelable(ARGUMENT_URI));
        }

        getLoaderManager().initLoader(LOADER_CARD, args, mCursorCallbacks);
    }

    static class DownloadTask extends AsyncTaskLoader<Uri> {

        private final String mUrl;

        public DownloadTask(Context context, String url) {
            super(context);
            mUrl = url;
        }

        @Override
        protected void onStartLoading() {
            super.onStartLoading();

            forceLoad();
        }

        @Override
        public Uri loadInBackground() {

            final File outdir = new File(getContext().getExternalFilesDir(null), "livingpostcards");

            if (!outdir.exists() && !outdir.mkdirs()) {
                throw new RuntimeException("could not mkdirs: " + outdir.getAbsolutePath());
            }
            final String fname = mUrl.substring(mUrl.lastIndexOf('/'));
            final File outfile = new File(outdir, fname);

            if (outfile.exists() && outfile.length() > 1024) {
                Log.d(TAG, fname + " has already been downloaded");
                return Uri.fromFile(outfile);
            }

            try {
                final HttpURLConnection hc = (HttpURLConnection) new URL(mUrl).openConnection();
                if (hc.getResponseCode() != 200) {
                    Log.e(TAG, "got a non-200 response from server");
                    return null;
                }
                if (hc.getContentLength() < 1024) { // this is probably not a video
                    Log.e(TAG,
                            "got a very small response from server of length "
                                    + hc.getContentLength());
                    return null;
                }
                try {
                    final BufferedInputStream bis = new BufferedInputStream(hc.getInputStream());
                    final FileOutputStream fos = new FileOutputStream(outfile);

                    Log.d(TAG, "downloading...");
                    StreamUtils.inputStreamToOutputStream(bis, fos);

                    fos.close();

                    Log.d(TAG, "done! Saved to " + outfile);
                    return Uri.fromFile(outfile);

                } finally {
                    hc.disconnect();
                }
            } catch (final IOException e) {
                Log.e(TAG, "error downloading video", e);
                return null;
            }
        }
    }

    @Override
    public Loader<Uri> onCreateLoader(int id, Bundle args) {

        return new DownloadTask(getActivity(), args.getString(ARGUMENT_URI_STRING));
    }

    @Override
    public void onLoadFinished(Loader<Uri> loader, Uri uriToPlay) {
        if (uriToPlay != null) {
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.setVideoURI(uriToPlay);
            mVideoView.seekTo(0);
            mVideoView.start();
        }
    }

    @Override
    public void onLoaderReset(Loader<Uri> loader) {
        mVideoView.setVisibility(View.GONE);
    }

    private final LoaderCallbacks<Cursor> mCursorCallbacks = new LoaderCallbacks<Cursor>() {

        @Override
        public Loader<Cursor> onCreateLoader(int loader, Bundle args) {
            final Uri card = args.getParcelable(ARGUMENT_URI);
            return new CursorLoader(getActivity(), card, new String[] { Card._ID,
                    Card.COL_VIDEO_RENDER, Card.COL_VIDEO_RENDER_TYPE }, null, null, null);
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor c) {
            if (c.moveToFirst()) {
                final String video = c.getString(c.getColumnIndexOrThrow(Card.COL_VIDEO_RENDER));
                if (video != null) {
                    final Bundle args = new Bundle();
                    args.putString(ARGUMENT_URI_STRING, video);
                    Log.d(TAG, "loading video " + video);
                    getLoaderManager().initLoader(LOADER_VIDEO, args, CardViewVideoFragment.this);
                } else {
                    Log.d(TAG, "Card doesn't have a rendered video");
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {
            // TODO Auto-generated method stub

        }
    };
}
