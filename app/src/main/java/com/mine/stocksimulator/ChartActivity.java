package com.mine.stocksimulator;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class ChartActivity extends AppCompatActivity {

    private static final String TAG = ChartActivity.class.getSimpleName();
    private WebView mChartWebView;
    private ChartProfile mChartProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chart);

        mChartWebView = (WebView) findViewById(R.id.chartWebView);

        Intent intent = getIntent();
        mChartProfile = intent.getParcelableExtra(BuyActivity.CHART_DETAILS);

        Log.i(TAG, mChartProfile.getSizeDates()+"");

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        String jsFunction = createFunction(mChartProfile.getSizeDates());

        String content = "<html>"
                + "  <head>"
                + "    <script type=\"text/javascript\" src=\"jsapi.js\"></script>"
                + "    <script type=\"text/javascript\">"
                + "      google.load(\"visualization\", \"1\", {packages:[\"corechart\", \"line\"]});"
                + "      google.setOnLoadCallback(drawChart);"
                +       jsFunction
                + "    </script>"
                + "  </head>"
                + "  <body>"
                + "    <div id=\"chart_div\" style=\"width: 400px; height: 200px;\"></div>"
                + "    <img style=\"padding: 0; margin: 0 0 0 330px; display: block;\"/>"
                + "  </body>" + "</html>";

        WebSettings webSettings = mChartWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        mChartWebView.requestFocusFromTouch();
        mChartWebView.loadDataWithBaseURL("file:///android_asset/", content, "text/html", "utf-8", null );

        createFunction(8);

    }


    public String createFunction(int size) {
        String xValues = "var xVals = [";
        for (int i = 1; i < size; i++){
            xValues += "'%" + i +""+"$s',";
        }
        xValues += "'%" + size +""+"$s'];";
        xValues = String.format(xValues, mChartProfile.getChartDates().toArray());

        String yValues = "var yVals = [";
        for (int i = 1; i < size; i++){
            yValues += "%" + i +""+"$f,";
        }
        yValues += "%" + size +"" + "$.2f];";
        yValues = String.format(yValues, mChartProfile.getChartValues().toArray());

        String jsFunction =
                "function drawChart(){"
                +   "var data = new google.visualization.DataTable();"
                +   "data.addColumn('string', 'Date');"
                +   "data.addColumn('number', 'Price');"
                +   xValues + yValues
                +   "var dataset = [];"
                +   "for (var i = 0; i < xVals.length; i++){"
                +   "   dataset.push([xVals[i], yVals[i]]);"
                +   "}"
                +   "data.addRows(dataset);"
                +   "var options={ hAxis: { title: 'Date'}, vAxis: {title: 'Price'}};"
                +   "var chart = new google.visualization.LineChart(document.getElementById('chart_div'));"
                +   "chart.draw(data, options);"
                +   "}";

        Log.i(TAG, xValues);
        Log.i(TAG, yValues);
        return jsFunction;

    }

}
