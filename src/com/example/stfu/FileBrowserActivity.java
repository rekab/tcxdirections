package com.example.stfu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

public class FileBrowserActivity extends Activity {

    public static final String FILE_RESULT = "picked_file";
	private static final String TAG = "FileBrowser";
    private CardScrollView mCardScrollView;
    List<Card> mCards;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        createCards(Filesystem.getStorageDirectory());
        GpxFileCardScrollAdapter adapter = new GpxFileCardScrollAdapter();
        mCardScrollView = new CardScrollView(this);
        mCardScrollView.setAdapter(adapter);
        mCardScrollView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
				Log.i(TAG, "clicked: " + mCards.get(pos).getText());
				// TODO: fire an intent
			}
        });
        mCardScrollView.activate();
        setContentView(mCardScrollView);
    }

    private void createCards(File storageDirectory) {
        mCards = new ArrayList<Card>();
        for (File file : storageDirectory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".gpx")) {
                Card card = new Card(this);
                // TODO: gpx parser should extract the title
                Log.i(TAG, "creating card for " + file.getName());
                card.setText(file.getName());
                mCards.add(card);
            }
        }
    }
    
    private class GpxFileCardScrollAdapter extends CardScrollAdapter {
    	@Override
        public int getPosition(Object item) {
            return mCards.indexOf(item);
        }

        @Override
        public int getCount() {
            return mCards.size();
        }

        @Override
        public Object getItem(int position) {
            return mCards.get(position);
        }

        /**
         * Returns the amount of view types.
         */
        @Override
        public int getViewTypeCount() {
            return Card.getViewTypeCount();
        }

        /**
         * Returns the view type of this card so the system can figure out
         * if it can be recycled.
         */
        @Override
        public int getItemViewType(int position){
            return mCards.get(position).getItemViewType();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return  mCards.get(position).getView(convertView, parent);
        }


    }
}
