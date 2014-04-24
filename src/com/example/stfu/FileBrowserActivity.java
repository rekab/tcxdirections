package com.example.stfu;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.google.android.glass.app.Card;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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
    List<GpxFileCard> mCards;

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
				String filename = mCards.get(pos).getFilename();
				Log.i(TAG, "clicked: " + filename);
				Intent returnIntent = new Intent();
				returnIntent.putExtra(FILE_RESULT, filename);
				setResult(RESULT_OK, returnIntent);
				Log.i(TAG, "set result, calling finish()");
				finish();
			}
        });
        mCardScrollView.activate();
        setContentView(mCardScrollView);
    }

    private void createCards(File storageDirectory) {
        mCards = new ArrayList<GpxFileCard>();
        for (File file : storageDirectory.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".gpx")) {
                GpxFileCard card = new GpxFileCard(this);
                // TODO: gpx parser should extract the title
                Log.i(TAG, "creating card for " + file.getName());
                card.setFile(file);
                mCards.add(card);
            }
        }
    }
    
    private class GpxFileCard extends Card {

		private String filename;
		
		public GpxFileCard(Context context) {
			super(context);
		}
    	public void setFile(File file) {
    		this.filename = file.getAbsolutePath();
    		setText(file.getName());
    	}
    	public String getFilename() {
    		return filename;
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
