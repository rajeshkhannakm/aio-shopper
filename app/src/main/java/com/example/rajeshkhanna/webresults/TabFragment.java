package com.example.rajeshkhanna.webresults;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

public class TabFragment extends WebFragment {

    String url;
    WebView webView;
    public void setUrl(String url){
        this.url = url;
    }

    public void load(String url){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tab_fragment1, container, false);

        webView = (WebView) view.findViewById(R.id.web_view_1);

        setWebsite(webView, url);

        return view;
    }

}