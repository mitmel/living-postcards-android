package edu.mit.mobile.android.livingpostcards;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import edu.mit.mobile.android.livingpostcards.data.Card;

public class CardListFragment extends ListFragment implements LoaderCallbacks<Cursor>,
		OnItemClickListener {

	private static final String[] FROM = { Card.NAME };
	private static final int[] TO = { android.R.id.text1 };

	private static final String[] PROJECTION = { Card._ID, Card.NAME };

	private SimpleCursorAdapter mAdapter;

	private static final Uri LIST_URI = Card.CONTENT_URI;

	public CardListFragment() {

	}

	@SuppressWarnings("deprecation")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {

		super.onActivityCreated(savedInstanceState);

		mAdapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_2,
				null, FROM, TO);

		setListAdapter(mAdapter);
		getListView().setOnItemClickListener(this);

		getLoaderManager().initLoader(0, null, this);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		return inflater.inflate(R.layout.card_list_fragment, container, false);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new CursorLoader(getActivity(), LIST_URI, PROJECTION, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> arg0, Cursor c) {
		mAdapter.swapCursor(c);

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		final Uri item = ContentUris.withAppendedId(LIST_URI, id);
		startActivity(new Intent(Intent.ACTION_VIEW, item));

	}
}
