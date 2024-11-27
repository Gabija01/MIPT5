package com.example.mipt5;

import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import androidx.appcompat.app.AppCompatActivity;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText editTextFilter;
    private Button btnLoadData;
    private ListView listViewRates;
    private ArrayAdapter<String> adapter;
    private List<String> currencyList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextFilter = findViewById(R.id.editTextFilter);
        btnLoadData = findViewById(R.id.btnLoadData);
        listViewRates = findViewById(R.id.listViewRates);

        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, currencyList);
        listViewRates.setAdapter(adapter);

        editTextFilter.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                adapter.getFilter().filter(s);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnLoadData.setOnClickListener(v -> new DataLoader().execute("https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml"));
    }

    private class DataLoader extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... urls) {
            try {
                URL url = new URL(urls[0]);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                InputStream inputStream = new BufferedInputStream(connection.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder result = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
                return result.toString();
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                parseXML(result);
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void parseXML(String xmlData) {
        try {
            currencyList.clear();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputStream inputStream = new java.io.ByteArrayInputStream(xmlData.getBytes());
            Document doc = builder.parse(inputStream);
            NodeList nodes = doc.getElementsByTagName("Cube");

            for (int i = 0; i < nodes.getLength(); i++) {
                Element element = (Element) nodes.item(i);
                if (element.hasAttribute("currency") && element.hasAttribute("rate")) {
                    String currency = element.getAttribute("currency");
                    String rate = element.getAttribute("rate");
                    currencyList.add(currency + " - " + rate);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
