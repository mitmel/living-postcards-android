package edu.mit.mobile.android.livingpostcards;
/*
 * Copyright (C) 2012-2013  MIT Mobile Experience Lab
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation version 2
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.File;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.VideoView;
import edu.mit.mobile.android.livingpostcards.data.Card;
import edu.mit.mobile.android.locast.Constants;
import edu.mit.mobile.android.net.DownloadLoader;

public class CardViewVideoFragment extends Fragment implements LoaderCallbacks<Uri>,
        OnClickListener {

    /**
     * The card URI
     */
    public static final String ARGUMENT_URI = "uri";
    public static final String ARGUMENT_URI_STRING = "uri_string";

    private VideoView mVideoView;

    public static final String TAG = CardViewVideoFragment.class.getSimpleName();

    public static final int LOADER_VIDEO = 100, LOADER_CARD = 101;

    private class PlaybackHandler implements OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            mp.setLooping(true);
        }
    };

    private final PlaybackHandler mPlaybackHandler = new PlaybackHandler();
    private TextView mErrorMessage;
    protected String mVideo;
    private ViewGroup mErrorBox;

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

        mErrorMessage = ((TextView) view.findViewById(R.id.error_message));
        mErrorBox = ((ViewGroup) view.findViewById(R.id.error_box));

        view.findViewById(R.id.retry).setOnClickListener(this);
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

    @Override
    public Loader<Uri> onCreateLoader(int id, Bundle args) {
        getView().findViewById(R.id.progress).setVisibility(View.VISIBLE);
        showErrorMessage(null);
        return new DownloadLoader(getActivity(), args.getString(ARGUMENT_URI_STRING), new File(
                getActivity().getExternalFilesDir(Environment.DIRECTORY_MOVIES), "livingpostcards"));
    }

    @Override
    public void onLoadFinished(Loader<Uri> loader, Uri uriToPlay) {

        getView().findViewById(R.id.progress).setVisibility(View.GONE);

        if (uriToPlay != null) {
            if (Constants.DEBUG) {
                Log.d(TAG, "playing video...");
            }
            mVideoView.setVisibility(View.VISIBLE);
            mVideoView.setVideoURI(uriToPlay);
            mVideoView.seekTo(0);
            mVideoView.start();

            showErrorMessage(null);
        } else {

            final DownloadLoader dlLoader = (DownloadLoader) loader;
            final Exception e = dlLoader.getException();
            showErrorMessage(getString(R.string.err_postcard_media_could_not_download,
                    e != null ? e.getLocalizedMessage() : "unknown reason"));

        }
    }

    private void showErrorMessage(CharSequence errorMessage) {
        if (errorMessage != null) {
            mErrorMessage.setText(errorMessage);
            mErrorBox.setVisibility(View.VISIBLE);
        } else {
            mErrorMessage.setText(null);
            mErrorBox.setVisibility(View.GONE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Uri> loader) {
        mVideoView.setVisibility(View.INVISIBLE);
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
                    mVideo = video;
                    loadVideo(video);
                } else {
                    if (Constants.DEBUG) {
                        Log.d(TAG, "Card doesn't have a rendered video");
                    }
                }
            }
        }

        @Override
        public void onLoaderReset(Loader<Cursor> arg0) {

        }
    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.retry:
                loadVideo(mVideo);
                break;
        }
    }

    protected void loadVideo(String video) {
        final Bundle args = new Bundle();
        args.putString(ARGUMENT_URI_STRING, video);
        if (Constants.DEBUG) {
            Log.d(TAG, "loading video " + video + " with loader");
        }
        getLoaderManager().restartLoader(LOADER_VIDEO, args, CardViewVideoFragment.this);

    }
}
