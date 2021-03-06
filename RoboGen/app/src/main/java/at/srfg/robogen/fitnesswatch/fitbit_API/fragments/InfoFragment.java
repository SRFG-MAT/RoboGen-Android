package at.srfg.robogen.fitnesswatch.fitbit_API.fragments;

import at.srfg.robogen.R;
import at.srfg.robogen.fitnesswatch.fitbit_API.common.loaders.ResourceLoaderResult;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;

/*******************************************************************************
 * InfoFragment Base Class
 ******************************************************************************/
public abstract class InfoFragment<T> extends Fragment
        implements LoaderManager.LoaderCallbacks<ResourceLoaderResult<T>>, SwipeRefreshLayout.OnRefreshListener {

    private TextView m_viewTitleText;
    private WebView m_viewWebContent;
    private ListView m_viewListContent;
    private ArrayAdapter<String> m_cListContentArrayAdapter;
    protected final String TAG = getClass().getSimpleName();

    /*******************************************************************************
     * overrides for Fragment
     ******************************************************************************/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fitbit_fragment_info, container, false);

        m_cListContentArrayAdapter = new ArrayAdapter<String>(rootView.getContext(), R.layout.fitbit_datalist_entry);
        m_viewListContent = rootView.findViewById(R.id.fitbit_listview);
        m_viewListContent.setAdapter(m_cListContentArrayAdapter);

        m_viewWebContent = rootView.findViewById(R.id.layout_webview);
        m_viewWebContent.bringToFront();

        m_viewTitleText = rootView.findViewById(R.id.layout_title_text);
        m_viewTitleText.setText(getActivity().getString(getTitleResourceId()));
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(getLoaderId(), null, this).forceLoad();
    }

    @Override
    public void onLoadFinished(Loader<ResourceLoaderResult<T>> loader, ResourceLoaderResult<T> data) {
        //binding.swipeRefreshLayout.setRefreshing(false);
        //binding.setLoading(false);
        switch (data.getResultType()) {
            case ERROR:
                Toast.makeText(getActivity(), R.string.error_loading_data, Toast.LENGTH_LONG).show();
                break;
            case EXCEPTION:
                Log.e(TAG, "Error loading data", data.getException());
                Toast.makeText(getActivity(), R.string.error_loading_data, Toast.LENGTH_LONG).show();
                break;
        }
    }

    @Override
    public void onLoaderReset(Loader<ResourceLoaderResult<T>> loader) {
        //no-op
    }

    @Override
    public void onRefresh() {
        getLoaderManager().getLoader(getLoaderId()).forceLoad();
    }

    /*******************************************************************************
     * helper functions
     ******************************************************************************/
    public abstract int getTitleResourceId();

    protected abstract int getLoaderId();

    private String formatNumber(Number number) {
        return NumberFormat.getNumberInstance(Locale.getDefault()).format(number);
    }

    private boolean isImageUrl(String string) {
        return (string.startsWith("http") &&
                (string.endsWith("jpg")
                        || string.endsWith("gif")
                        || string.endsWith("png")));
    }

    private void setMainText(String text) {
        m_viewWebContent.loadData(text, "text/html", "UTF-8");
    }

    protected void addTextToList(String text) {
        m_cListContentArrayAdapter.add(text);
    }
    protected void clearList() {m_cListContentArrayAdapter.clear();}

    /*******************************************************************************
     * printKeys function
     * will run through every entry of JsonObject and call print helpers
     ******************************************************************************/
    protected void printKeys(StringBuilder stringBuilder, Object object) {

        try {
            JSONObject jsonObject = new JSONObject(new Gson().toJson(object));
            Iterator<String> keys = jsonObject.keys();

            while (keys.hasNext()) {
                String key = keys.next();
                Object value = jsonObject.get(key);

                if (!(value instanceof JSONObject) && !(value instanceof JSONArray)) {

                    if (isImageUrl(value.toString())) {
                        printSingleImageReversed(key, value);
                    }
                    else if (value instanceof Number) {
                        stringBuilder.append( key + " = " + formatNumber((Number) value)  + "\n");
                    }
                    else {
                        stringBuilder.append(key + " = " + value.toString() + "\n");
                    }
                }
            }

            addTextToList(stringBuilder.toString());

        } catch (JSONException e) { e.printStackTrace(); }
    }

    /*******************************************************************************
     * print an image reversed at the beginning of the stringbuilder
     * (to ensure that images are always printed at the beginning)
     ******************************************************************************/
    private void printSingleImageReversed(String key, Object value)
    {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.insert(0, "\" width=\"150\" height=\"150\"></div>");
        stringBuilder.insert(0, value.toString());
        stringBuilder.insert(0, "<div style=\"display: inline-block;\"><img src=\"");

        setMainText(stringBuilder.toString());
    }
}
