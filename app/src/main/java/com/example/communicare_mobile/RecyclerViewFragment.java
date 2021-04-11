package com.example.communicare_mobile;

import android.content.Context;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.communicare_mobile.model.TileModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Demonstrates the use of {@link RecyclerView} with a {@link LinearLayoutManager} and a
 * {@link GridLayoutManager}.
 */
public class RecyclerViewFragment extends Fragment implements OnAdapterItemClickListener, TextToSpeech.OnInitListener {

    private static final String TAG = "RecyclerViewFragment";
    private static final String KEY_LAYOUT_MANAGER = "layoutManager";
    private static final int SPAN_COUNT = 2;
    private TextToSpeech tts;
    private String text;
    private Button lastPainRegion;
    private TileModel lastTile;
    private boolean showYesNoBar;

    private enum LayoutManagerType {
        GRID_LAYOUT_MANAGER,
        LINEAR_LAYOUT_MANAGER
    }

    protected LayoutManagerType mCurrentLayoutManagerType;
    protected RecyclerView mRecyclerView;
    protected ConstraintLayout painOverlay;
    protected LinearLayout yesNoBar;
    protected LinearLayout backButtonLayout;
    protected LinearLayout settingsButtonLayout;
    protected Button homeButton;
    protected Button settingsButton;
    protected Button backButton;
    protected Button yesButton;
    protected Button noButton;
    protected SeekBar painBar;
    protected TileViewAdapter mAdapter;
    protected RecyclerView.LayoutManager mLayoutManager;
    protected ArrayList<TileModel> mDataset;
    protected String language = "english";
    protected MainActivity main;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
        main = (MainActivity) getActivity();
        initDataset("home", language);
        Context context = getContext();
        tts = new TextToSpeech(context, this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tile_view_fragment, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);

        painOverlay = rootView.findViewById(R.id.painOverlay);
        painBar = rootView.findViewById(R.id.painBar);
        initPainRegions(rootView);

        initNavigationButtons();
        initYesNoBar();

        boolean columnsLayout = (mDataset.size() % 2 == 0 && mDataset.size() > 2)
                || mDataset.size() > 5;

        if (columnsLayout) {
            setRecyclerViewLayoutManager(LayoutManagerType.GRID_LAYOUT_MANAGER);
        } else {
            setRecyclerViewLayoutManager(LayoutManagerType.LINEAR_LAYOUT_MANAGER);
        }

        mAdapter = new TileViewAdapter(mDataset, this, this);
        // Set CustomAdapter as the adapter for RecyclerView.
        mRecyclerView.setAdapter(mAdapter);

        return rootView;
    }

    private void initYesNoBar() {
        yesButton = main.findViewById(R.id.yesButton);
        noButton = main.findViewById(R.id.noButton);
        yesNoBar = main.findViewById(R.id.yes_no_bar);
        showYesNoBar = true;

        yesButton.setOnClickListener(button -> {
            text = "yes";
            speakOut();
        });
        noButton.setOnClickListener(button -> {
            text = "no";
            speakOut();
        });
    }

    private void initNavigationButtons() {
        homeButton = main.findViewById(R.id.homeButton);
        settingsButton = main.findViewById(R.id.settingsButton);
        settingsButtonLayout = main.findViewById(R.id.settingsButtonLayout);
        backButton = main.findViewById(R.id.backButton);
        backButtonLayout = main.findViewById(R.id.backButtonLayout);

        backButton.setOnClickListener(button -> {
            lastTile = getPreviousScreen(lastTile.getViewCategory());
            if (lastTile == null) {
                redirectViewHome();
            } else {
                changeViewToTile(lastTile);
            }

        });
        homeButton.setOnClickListener(button -> {
            redirectViewHome();
        });
    }

    private void redirectViewHome() {
        yesNoBar.setVisibility(View.VISIBLE);
        TileModel homeTile = new TileModel();
        homeTile.setViewRedirect("home");
        changeViewToTile(homeTile);
        backButtonLayout.setVisibility(View.GONE);
        settingsButtonLayout.setVisibility(View.VISIBLE);
        showYesNoBar = true;
    }

    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.UK);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "This language is not supported");
            } else {
                speakOut();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    private void speakOut() {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    public void setColumnLayout() {
        boolean columnsLayout = (mDataset.size() % 2 == 0 && mDataset.size() > 2)
                || mDataset.size() > 5;
        if (columnsLayout) {
            setRecyclerViewLayoutManager(LayoutManagerType.GRID_LAYOUT_MANAGER);
        } else {
            setRecyclerViewLayoutManager(LayoutManagerType.LINEAR_LAYOUT_MANAGER);
        }
    }

    /**
     * Set RecyclerView's LayoutManager to the one given.
     *
     * @param layoutManagerType Type of layout manager to switch to.
     */
    public void setRecyclerViewLayoutManager(LayoutManagerType layoutManagerType) {
        switch (layoutManagerType) {
            case GRID_LAYOUT_MANAGER:
                mLayoutManager = new GridLayoutManager(getActivity(), SPAN_COUNT);
                mCurrentLayoutManagerType = LayoutManagerType.GRID_LAYOUT_MANAGER;
                break;
            default:
                mLayoutManager = new LinearLayoutManager(getActivity());
                mCurrentLayoutManagerType = LayoutManagerType.LINEAR_LAYOUT_MANAGER;
        }

        mRecyclerView.setLayoutManager(mLayoutManager);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Save currently selected layout manager.
        savedInstanceState.putSerializable(KEY_LAYOUT_MANAGER, mCurrentLayoutManagerType);
        super.onSaveInstanceState(savedInstanceState);
    }

    /**
     * Generates Strings for RecyclerView's adapter. This data would usually come
     * from a local content provider or remote server.
     */
    private void initDataset(String category, String language) {
        // TODO: Replace with data from json - home screen
        mDataset = (ArrayList<TileModel>) getDataByCategory(category, language);
    }

    @Override
    public void onAdapterItemClickListener(TileModel tile) {
        Log.d(TAG, "RecyclerView Element " + tile + " clicked.");
        // TODO: this needs to be the correct language label based on the language chosen from translation.
        if (showYesNoBar) {
            showYesNoBar = false;
            yesNoBar.setVisibility(View.GONE);
            settingsButtonLayout.setVisibility(View.GONE);
            backButtonLayout.setVisibility(View.VISIBLE);
        }
        lastTile = tile;
        text = tile.getLabel();
        speakOut();
        changeViewToTile(tile);


    }

    private void changeViewToTile(TileModel tile) {
        if (tile.getViewRedirect().equals("pain")) {
            displayPainOverlay();
        } else {
            mDataset = (ArrayList<TileModel>) getDataByCategory(tile.getViewRedirect(), language);
            setColumnLayout();
            mAdapter = new TileViewAdapter(mDataset, this, this);
            // Set CustomAdapter as the adapter for RecyclerView.
            mRecyclerView.setAdapter(mAdapter);
        }
    }


    private List<TileModel> getDataByCategory(String category, String language) {
        mDataset = new ArrayList<>();
        try {
            JSONArray jsonDataArray = new JSONObject(LoadJsonFromAsset("viewObjects.json")).getJSONArray("elements");
            JSONObject jsonTransObj = new JSONObject(LoadJsonFromAsset("translations.json")).getJSONObject("translations");

            for (int i = 0; i < jsonDataArray.length(); i++) {
                JSONObject itemObj = jsonDataArray.getJSONObject(i);

                long id = itemObj.getLong("id");
                String label = itemObj.getString("label");
                String viewCategory = itemObj.getString("viewCategory");
                String viewRedirect = itemObj.getString("viewRedirect");
                boolean textToSpeech = itemObj.getBoolean("textToSpeech");
                String drawable = itemObj.getString("drawable");

                if (viewCategory.equals(category)) {
                    String translation = jsonTransObj.getJSONObject(label).getString(language);
                    TileModel tile = new TileModel();
                    tile.setId(id);
                    tile.setLabel(translation);
                    tile.setViewCategory(viewCategory);
                    tile.setViewRedirect(viewRedirect);
                    tile.setTextToSpeech(textToSpeech);
                    tile.setDrawable(drawable);
                    mDataset.add(tile);
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "addItemsFromJSON: ", e);
            e.printStackTrace();
        }
        return mDataset;
    }

    private TileModel getPreviousScreen(String category) {
        try {
            JSONArray jsonDataArray = new JSONObject(LoadJsonFromAsset("viewObjects.json")).getJSONArray("elements");

            for (int i = 0; i < jsonDataArray.length(); i++) {
                JSONObject itemObj = jsonDataArray.getJSONObject(i);

                long id = itemObj.getLong("id");
                String label = itemObj.getString("label");
                String viewCategory = itemObj.getString("viewCategory");
                String viewRedirect = itemObj.getString("viewRedirect");
                boolean textToSpeech = itemObj.getBoolean("textToSpeech");
                String drawable = itemObj.getString("drawable");

                if (viewRedirect.equals(category)) {
                    TileModel tile = new TileModel();
                    tile.setId(id);
                    tile.setLabel(label);
                    tile.setViewCategory(viewCategory);
                    tile.setViewRedirect(viewRedirect);
                    tile.setTextToSpeech(textToSpeech);
                    tile.setDrawable(drawable);
                    return tile;
                }
            }
        } catch (JSONException e) {
            Log.d(TAG, "addItemsFromJSON: ", e);
            e.printStackTrace();
        }
        return null;
    }

    public String LoadJsonFromAsset(String fileName) {
        String json = null;
        try {
            InputStream in = getActivity().getAssets().open(fileName);
            int size = in.available();
            byte[] buffer = new byte[size];
            in.read(buffer);
            in.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    private void displayPainOverlay() {
        painOverlay.setVisibility(View.VISIBLE);
    }

    private void displayPainBar(Button painRegionButton) {
        lastPainRegion = painRegionButton;
        painBar.setVisibility(View.VISIBLE);
    }

    private void initPainRegions(View rootView) {
        painBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                                               @Override
                                               public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                                                   String painValue = String.valueOf(progress / 10);
                                                   lastPainRegion.setText(painValue);
                                                   painBar.setVisibility(View.GONE);
                                                   text = lastPainRegion.getTag() + " pain level is " + painValue;
                                                   Log.i("painOverlay", text);
                                                   speakOut();
                                               }

                                               @Override
                                               public void onStartTrackingTouch(SeekBar seekBar) {

                                               }

                                               @Override
                                               public void onStopTrackingTouch(SeekBar seekBar) {

                                               }
                                           }

        );
        Button headButton = rootView.findViewById(R.id.headButton);
        headButton.setOnClickListener(button -> {
            displayPainBar((Button) button);
        });

        Button chestButton = rootView.findViewById(R.id.chestButton);
        chestButton.setOnClickListener(button->{
            displayPainBar((Button) button);
        });

        Button bellyButton = rootView.findViewById(R.id.bellyButton);
        bellyButton.setOnClickListener(button->{
            displayPainBar((Button) button);
        });

        Button leftShoButton = rootView.findViewById(R.id.leftShoButton);
        leftShoButton.setOnClickListener(button->{
            displayPainBar((Button) button);
        });

        Button leftArmButton = rootView.findViewById(R.id.leftArmButton);
        leftArmButton.setOnClickListener(button->{
            displayPainBar((Button) button);
        });

        Button rightShoButton = rootView.findViewById(R.id.rightShoButton);
        rightShoButton.setOnClickListener(button->{
            displayPainBar((Button) button);
        });

        // saan juurde teha
        //        //Button headButton = rootView.findViewById(R.id.headButton);
        //        //headButton.setOnClickListener(button->{
        //        //    displayPainBar((Button) button);
        //        //});
    }
}
