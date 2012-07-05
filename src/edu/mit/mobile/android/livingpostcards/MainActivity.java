package edu.mit.mobile.android.livingpostcards;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class MainActivity extends Activity implements OnClickListener {

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

		final Intent intent = new Intent(this, CameraActivity.class);

		final File mostRecentPicture = StorageUtils.getMostRecentPicture();
		if (mostRecentPicture != null) {
			intent.setAction(Intent.ACTION_INSERT);
			intent.setData(Uri.fromFile(mostRecentPicture));
		}
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
