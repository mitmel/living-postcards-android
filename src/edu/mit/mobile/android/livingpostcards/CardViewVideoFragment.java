package edu.mit.mobile.android.livingpostcards;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;
import edu.mit.mobile.android.utils.StreamUtils;

public class CardViewVideoFragment extends Fragment implements LoaderCallbacks<Uri> {

    private VideoView mVideoView;

    public static final String ARG_URL = "url";

    public static final String TAG = CardViewVideoFragment.class.getSimpleName();

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
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);

        final Bundle args = new Bundle();
        args.putString(ARG_URL, "http://mobile-server.mit.edu/~stevep/postcard-68.3gp");
        getLoaderManager().initLoader(0, args, this);
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

            if (outfile.exists()) {
                Log.d(TAG, fname + " has already been downloaded");
                return Uri.fromFile(outfile);
            }

            try {
                final HttpURLConnection hc = (HttpURLConnection) new URL(mUrl).openConnection();

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

        return new DownloadTask(getActivity(), args.getString(ARG_URL));
    }

    @Override
    public void onLoadFinished(Loader<Uri> loader, Uri uriToPlay) {
        mVideoView.setVideoURI(uriToPlay);
        mVideoView.start();

    }

    @Override
    public void onLoaderReset(Loader<Uri> loader) {

    }
}
