package com.gmail.deluramichal.aukletnewsreader;

import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import org.htmlparser.Parser;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import nl.matshofman.saxrssreader.RssFeed;
import nl.matshofman.saxrssreader.RssItem;
import nl.matshofman.saxrssreader.RssReader;


/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.web_view, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        URL url = null;
        try {
            url = new URL("http://feeds.reuters.com/news/artsculture");
        } catch (MalformedURLException e) {
            Log.d(e.getClass().getSimpleName(), e.getMessage());
        }
        RssFeed feed = null;
        try {
            feed = RssReader.read(url);
        } catch (SAXException e) {
            Log.d(e.getClass().getSimpleName(), e.getMessage());
        } catch (IOException e) {
            Log.d(e.getClass().getSimpleName(), e.getMessage());
        }

        ArrayList<RssItem> rssItems = feed.getRssItems();
        for(RssItem rssItem : rssItems) {
            Log.d("RSS Reader", "Title: " + rssItem.getTitle());
            Log.d("RSS Reader", "Content: " + rssItem.getContent());
            Log.d("RSS Reader", "Description: " + rssItem.getDescription());
            Log.d("RSS Reader", "Link: " + rssItem.getLink());
            Log.d("RSS Reader", "PubDate: " + rssItem.getPubDate());
        }

        WebView webView =(WebView) rootView.findViewById(R.id.web_view);
        String summary = rssItems.get(0).getDescription();
        webView.loadData(summary, "text/html", null);

        try
        {
            Parser parser = new Parser (rssItems.get(0).getDescription());
            NodeList list = parser.parse (null);
            Log.d("HTML Parser: ", list.toHtml());
        }
        catch (ParserException pe)
        {
            pe.printStackTrace ();
        }



        return rootView;


    }
}
