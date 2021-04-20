package com.example.communicare_mobile;

import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.SeekBar;

import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import com.example.communicare_mobile.db.AppDatabase;
import com.example.communicare_mobile.db.Settings;
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
    private boolean showHome;

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
    protected boolean gender;
    protected String patientLang;     // russian_female, russian_male, english, estonian,
    protected String nurseLang;
    protected MainActivity main;
    protected AppDatabase db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize dataset, this data would usually come from a local content provider or
        // remote server.
        main = (MainActivity) getActivity();
        db = Room.databaseBuilder(getContext(),
                AppDatabase.class, "main-db")
                .allowMainThreadQueries()
                .build();
        Settings settings = db.settingsDao().findById(0);
        if (settings == null) {
            settings = createDefaultSettings();
        }
        gender = settings.gender;
        patientLang = settings.textLanguage.toLowerCase();
        nurseLang = settings.speechLanguage.toLowerCase();

        if (patientLang.equals("russian")) {
            patientLang = gender ? patientLang + "_female" : patientLang + "_male";
        }

        if (nurseLang.equals("russian")) {
            nurseLang = gender ? nurseLang + "_female" : nurseLang + "_male";
        }

        initDataset("home", patientLang, nurseLang);
        tts = new TextToSpeech(getContext(), this);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tile_view_fragment, container, false);
        rootView.setTag(TAG);

        // BEGIN_INCLUDE(initializeRecyclerView)
        mRecyclerView = rootView.findViewById(R.id.recyclerView);
        mRecyclerView.setNestedScrollingEnabled(false);

        painOverlay = rootView.findViewById(R.id.painOverlay);
        painBar = rootView.findViewById(R.id.painBar);
        initPainRegions(rootView);

        try {
            initNavigationButtons();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            initYesNoBar();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        boolean columnsLayout = (mDataset.size() % 2 == 0 && mDataset.size() > 2) || mDataset.size() > 5;

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

    private Settings createDefaultSettings() {
        Settings newSettings = new Settings();
        newSettings.id = 0;
        newSettings.gender = true;
        newSettings.speechLanguage = "Estonian";
        newSettings.textLanguage = "Estonian";
        db.settingsDao().insertAll(newSettings);
        return newSettings;
    }

    private void initYesNoBar() throws JSONException {
        yesButton = main.findViewById(R.id.yesButton);
        noButton = main.findViewById(R.id.noButton);
        yesNoBar = main.findViewById(R.id.yes_no_bar);
        showHome = true;
        yesButton.setText(getTranslation("yes", patientLang));
        noButton.setText(getTranslation("no", patientLang));

        yesButton.setOnClickListener(button -> {
            try {
                text = getTranslation("yes", nurseLang);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            speakOut();
        });
        noButton.setOnClickListener(button -> {
            try {
                text = getTranslation("no", nurseLang);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            speakOut();
        });
    }

    private void initNavigationButtons() throws JSONException {
        homeButton = main.findViewById(R.id.homeButton);
        homeButton.setText(getTranslation("home", patientLang));
        settingsButton = main.findViewById(R.id.settingsButton);
        settingsButtonLayout = main.findViewById(R.id.settingsButtonLayout);
        settingsButton.setText(getTranslation("settings", patientLang));
        backButton = main.findViewById(R.id.backButton);
        backButtonLayout = main.findViewById(R.id.backButtonLayout);
        backButton.setText(getTranslation("back", patientLang));

        backButton.setOnClickListener(button -> {
            lastTile = getPreviousScreen(lastTile.getViewCategory());
            if (lastTile == null || lastTile.getId() == 0) {
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
        TileModel homeTile = new TileModel();
        homeTile.setViewRedirect("home");
        changeViewToTile(homeTile);
        backButtonLayout.setVisibility(View.GONE);
        settingsButtonLayout.setVisibility(View.VISIBLE);
        showHome = true;
        painOverlay.setVisibility(View.GONE);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = 0;
            switch (nurseLang) {
                case "english":
                    Voice maleVoice = new Voice("en-us-x-sfg#male_2-local", new Locale("en", "US"), 400, 200, false, null);
                    Voice femaleVoice = new Voice("en-uk-x-sfg#female_2-local", new Locale("en", "UK"), 400, 200, false, null);
                    tts.setVoice(gender ? femaleVoice : maleVoice);
                    break;
                case "russian": {
                    Locale locale = new Locale.Builder().setLanguageTag("ru-RU").build();
                    result = tts.setLanguage(locale);
                    break;
                }
                case "estonian": {
                    Locale locale = new Locale.Builder().setLanguageTag("et-EE").build();
                    result = tts.setLanguage(locale);
                    break;
                }
            }

            if (result == 0) {
                Log.e("TTS", "Language is not selected");
            } else if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
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
    private void initDataset(String category, String patientLanguage, String nurseLanguage) {
        // TODO: Replace with data from json - home screen
        mDataset = (ArrayList<TileModel>) getDataByCategory(category);
    }

    @Override
    public void onAdapterItemClickListener(TileModel tile) {
        Log.d(TAG, "RecyclerView Element " + tile + " clicked.");
        if (showHome) {
            showHome = false;
            settingsButtonLayout.setVisibility(View.GONE);
            backButtonLayout.setVisibility(View.VISIBLE);
        }
        lastTile = tile;
        text = tile.getTtsPhrase();
        speakOut();
        changeViewToTile(tile);

    }

    private void changeViewToTile(TileModel tile) {
        if (tile.getViewRedirect().equals("pain")) {
            displayPainOverlay();
        } else {
            mDataset = (ArrayList<TileModel>) getDataByCategory(tile.getViewRedirect());
            setColumnLayout();
            mAdapter = new TileViewAdapter(mDataset, this, this);
            // Set CustomAdapter as the adapter for RecyclerView.
            mRecyclerView.setAdapter(mAdapter);
            painOverlay.setVisibility(View.GONE);
        }
    }


    private List<TileModel> getDataByCategory(String category) {
        mDataset = new ArrayList<>();
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

                if (viewCategory.equals(category)) {
                    TileModel tile = new TileModel();
                    tile.setId(id);
                    tile.setLabel(getTranslation(label, patientLang));
                    tile.setTtsPhrase(getTranslation(label, nurseLang));
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

    private String getTranslation(String label, String language) throws JSONException {
        JSONObject jsonTransObj = new JSONObject(LoadJsonFromAsset("translations.json")).getJSONObject("translations");
        return jsonTransObj.getJSONObject(label).getString(language);

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
        chestButton.setOnClickListener(button -> {
            displayPainBar((Button) button);
        });

        Button bellyButton = rootView.findViewById(R.id.bellyButton);
        bellyButton.setOnClickListener(button -> {
            displayPainBar((Button) button);
        });

        Button leftShoButton = rootView.findViewById(R.id.leftShoButton);
        leftShoButton.setOnClickListener(button -> {
            displayPainBar((Button) button);
        });

        Button leftArmButton = rootView.findViewById(R.id.leftArmButton);
        leftArmButton.setOnClickListener(button -> {
            displayPainBar((Button) button);
        });

        Button rightShoButton = rootView.findViewById(R.id.rightShoButton);
        rightShoButton.setOnClickListener(button -> {
            displayPainBar((Button) button);
        });

        Button rightThighButton = rootView.findViewById(R.id.rightThighButton);
        rightThighButton.setOnClickListener(button -> {
            displayPainBar((Button) button);
        });

        Button leftThighButton = rootView.findViewById(R.id.leftThighButton);
        leftThighButton.setOnClickListener(button -> {
            displayPainBar((Button) button);
        });

        Button leftShinButton = rootView.findViewById(R.id.leftShinButton);
        leftShinButton.setOnClickListener(button -> {
            displayPainBar((Button) button);
        });

        Button rightShinButton = rootView.findViewById(R.id.rightShinButton);
        rightShinButton.setOnClickListener(button -> {
            displayPainBar((Button) button);
        });


    }


}
