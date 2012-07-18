package edu.mit.mobile.android.livingpostcards;

import java.util.UUID;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import edu.mit.mobile.android.livingpostcards.data.Card;

public class MainActivity extends FragmentActivity implements OnClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		findViewById(R.id.new_card).setOnClickListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	private void createNewCard() {

		final ContentResolver cr = getContentResolver();

		final ContentValues cv = new ContentValues();

		cv.put(Card.UUID, UUID.randomUUID().toString());

		cv.put(Card.NAME, DateUtils.formatDateTime(this, System.currentTimeMillis(),
				DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME));

		final Uri card = cr.insert(Card.CONTENT_URI, cv);

		final Intent intent = new Intent(CameraActivity.ACTION_ADD_PHOTO, card);

		startActivity(intent);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.new_card:
				createNewCard();

				break;
		}
	}
}
