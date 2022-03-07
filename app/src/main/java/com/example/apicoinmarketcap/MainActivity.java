package com.example.apicoinmarketcap;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private RecyclerView currencyRV;
    private EditText searchEdt;
    private ArrayList<CurrencyModal> currencyModalArrayList;
    private CurrencyRVAdapter currencyRVAdapter;
    private ProgressBar loadingPB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchEdt = findViewById(R.id.idEdtCurrency);

        loadingPB = findViewById(R.id.idPBLoading);
        currencyRV = findViewById(R.id.idRVcurrency);
        currencyModalArrayList = new ArrayList<>();

        currencyRVAdapter = new CurrencyRVAdapter(currencyModalArrayList, this);

        currencyRV.setLayoutManager(new LinearLayoutManager(this));

        currencyRV.setAdapter(currencyRVAdapter);

        //Obtencion de los datos de la API
        getData();

        //Metodo para realizar la busqueda de la crypto insertada en la barra de busqueda
        searchEdt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                filter(s.toString());
            }
        });
    }

    //Metodo que crea un arraylist con las crypto filtradas por busqueda
    private void filter(String filter) {
        ArrayList<CurrencyModal> filteredlist = new ArrayList<>();

        for (CurrencyModal item : currencyModalArrayList) {

            if (item.getName().toLowerCase().contains(filter.toLowerCase())) {
                filteredlist.add(item);
            }
        }

        if (filteredlist.isEmpty()) {
            Toast.makeText(this, "No currency found..", Toast.LENGTH_SHORT).show();

        } else {
            currencyRVAdapter.filterList(filteredlist);
        }
    }

    //Metodo para obtener los datos de la API
    private void getData() {
        // Variable que llama a la API
        String url = "https://pro-api.coinmarketcap.com/v1/cryptocurrency/listings/latest";
        //Variable que solicita los datos a la API
        RequestQueue queue = Volley.newRequestQueue(this);
        // Objeto JSON que obtendra la data de la API
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                /*
                 * Metodo que hara que mientras extraemos la info de la API
                 * aparezca la barra de progreso.
                 */
                loadingPB.setVisibility(View.GONE);
                try {

                    JSONArray dataArray = response.getJSONArray("data");

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject dataObj = dataArray.getJSONObject(i);
                        String symbol = dataObj.getString("symbol");
                        String name = dataObj.getString("name");
                        JSONObject quote = dataObj.getJSONObject("quote");
                        JSONObject USD = quote.getJSONObject("USD");
                        double price = USD.getDouble("price");

                        currencyModalArrayList.add(new CurrencyModal(name, symbol, price));
                    }
                    currencyRVAdapter.notifyDataSetChanged();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, "Something went amiss. Please try again later", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Toast.makeText(MainActivity.this, "Something went amiss. Please try again later", Toast.LENGTH_SHORT).show();
            }
        }) {
            @Override
            public Map<String, String> getHeaders() {
                //Metodo en el que coge la API Key
                HashMap<String, String> headers = new HashMap<>();
                headers.put("X-CMC_PRO_API_KEY", "b0a0f3f2-700c-4f13-8423-76dc72f695ea");
                return headers;
            }
        };
        //Añadimos el archivo JSON a la cola
        queue.add(jsonObjectRequest);
    }
}
