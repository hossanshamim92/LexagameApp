package com.lexidome.lexagame;

import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.SearchView;

import com.lexidome.lexagame.helper.BaseAppCompat;
import com.lexidome.lexagame.helper.Misc;
import com.lexidome.lexagame.helper.Variables;

import org.mintsoft.mintlib.GetURL;
import org.mintsoft.mintlib.onResponse;
import com.lexidome.lexagame.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class Faq extends BaseAppCompat implements SearchView.OnQueryTextListener, SearchView.OnCloseListener {
    private SearchView searchView;
    private fAdapter adapter;
    private ExpandableListView listView;
    private Dialog conDiag, loadingDiag;
    private ArrayList<HashMap<String, String>> list;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.faq);
        loadingDiag = Misc.loadingDiag(this);
        listView = findViewById(R.id.faq_expandList);
        searchView = findViewById(R.id.faq_searchView);
        findViewById(R.id.faq_back).setOnClickListener(view -> finish());
        list = Variables.getArrayHash("faq_list");
        if (list == null) {
            netCall();
        } else {
            loadingDiag.show();
            listCall();
        }
    }

    @Override
    protected void onDestroy() {
        Variables.setArrayHash("faq_list", list);
        super.onDestroy();
    }

    private void netCall() {
        loadingDiag.show();
        GetURL.getFaq(this, new onResponse() {
            @Override
            public void onSuccessListHashMap(ArrayList<HashMap<String, String>> l) {
                loadingDiag.dismiss();
                list = l;
                listCall();
            }

            @Override
            public void onError(int errorCode, String error) {
                loadingDiag.dismiss();
                if (errorCode == -9) {
                    conDiag = Misc.noConnection(conDiag, Faq.this, () -> {
                        netCall();
                        conDiag.dismiss();
                    });
                } else {
                    Toast.makeText(Faq.this, error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onClose() {
        adapter.filterData("");
        collapseAll();
        return false;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        adapter.filterData(s);
        if (s.isEmpty()) {
            collapseAll();
        } else {
            expandAll();
        }
        return false;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        adapter.filterData(s);
        if (s.isEmpty()) {
            collapseAll();
        } else {
            expandAll();
        }
        return false;
    }

    private void expandAll() {
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            listView.expandGroup(i);
        }
    }

    private void collapseAll() {
        for (int i = 0; i < adapter.getGroupCount(); i++) {
            listView.collapseGroup(i);
        }
    }

    private void listCall() {
        adapter = new fAdapter(this, list);
        listView.setAdapter(adapter);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);
        searchView.setOnQueryTextListener(this);
        searchView.setOnCloseListener(this);
        loadingDiag.dismiss();
    }

    private static class fAdapter extends BaseExpandableListAdapter {
        private final LayoutInflater inflater;
        private final ArrayList<HashMap<String, String>> original, list;

        fAdapter(Context context, ArrayList<HashMap<String, String>> l) {
            inflater = LayoutInflater.from(context);
            this.list = l;
            this.original = new ArrayList<>();
            original.addAll(l);
        }

        @Override
        public int getGroupCount() {
            return list.size();
        }

        @Override
        public int getChildrenCount(int i) {
            return 1;
        }

        @Override
        public Object getGroup(int i) {
            return list.get(i).get("qs");
        }

        @Override
        public Object getChild(int i, int i1) {
            return list.get(i).get("ans");
        }

        @Override
        public long getGroupId(int i) {
            return i;
        }

        @Override
        public long getChildId(int i, int i1) {
            return i1;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int i, boolean b, View view, ViewGroup viewGroup) {
            View v = view;
            if (v == null) v = inflater.inflate(R.layout.faq_list_q, null);
            TextView qsView = v.findViewById(R.id.faq_list_q_text);
            qsView.setText(list.get(i).get("q"));
            return v;
        }

        @Override
        public View getChildView(int i, int i1, boolean b, View view, ViewGroup viewGroup) {
            View v2 = view;
            if (v2 == null) v2 = inflater.inflate(R.layout.faq_list_a, null);
            TextView ansView = v2.findViewById(R.id.faq_list_a_text);
            ansView.setText(list.get(i).get("a"));
            return v2;
        }

        @Override
        public boolean isChildSelectable(int i, int i1) {
            return true;
        }

        public void filterData(String query) {
            query = query.toLowerCase();
            list.clear();
            if (query.isEmpty()) {
                list.addAll(original);
            } else {
                for (HashMap<String, String> hashMap : original) {
                    if (Objects.requireNonNull(hashMap.get("q")).toLowerCase().contains(query))
                        list.add(hashMap);
                }
            }
            notifyDataSetChanged();
        }
    }
}