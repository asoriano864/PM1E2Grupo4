package com.example.pm1e2grupo4;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import com.android.volley.*;
import com.android.volley.toolbox.*;

public class ActivityListContactos extends AppCompatActivity {

    ListView lstContactos;
    List<Contacto> cantactoList;
    ArrayList<String> arrayListContactos;
    EditText txtBuscar;
    private String idCont;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_contactos);
        lstContactos = findViewById(R.id.lstContactos);
        cantactoList = new ArrayList<>();
        arrayListContactos = new ArrayList<String>();
        txtBuscar = (EditText) findViewById(R.id.txtBuscar);
        Button btnVolver = (Button) findViewById(R.id.btnVolver2);
        Button btnEditar = (Button) findViewById(R.id.btnEditar);

        //LISTA DE CONTACTOS
        SendRequest();

        btnVolver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pantallaVolver = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(pantallaVolver);
            }
        });

        //BOTON EDITAR CONTACTO
        btnEditar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ActivityEdit.class);
                intent.putExtra("idCont", String.valueOf(idCont));
                startActivity(intent);
            }
        });
    }

    private void SendRequest(){
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = RestApiMethods.ApiGetDatos;

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        // Log.i("Respuesta", "onResponse: " + response.substring(0,500) );
                        try {
                            JSONObject obj = new JSONObject(response);
                            JSONArray ContactoArray = obj.getJSONArray("Contactos");

                            for (int i = 0; i < ContactoArray.length(); i++) {
                                //getting the json object of the particular index inside the array
                                JSONObject contactoObject = ContactoArray.getJSONObject(i);

                                //creating a hero object and giving them the values from json object
                                Contacto contact = new Contacto(contactoObject.getString("ID"),
                                        contactoObject.getString("NOMBRE"),
                                        contactoObject.getString("TELEFONO"),
                                        contactoObject.getString("LATITUD"),
                                        contactoObject.getString("LONGITUD"),
                                        "",
                                        contactoObject.getString("ARCHIVO"));

                                //adding the hero to herolist
                                cantactoList.add(contact);
                                arrayListContactos.add(contact.getNombre());
                            }
                            ArrayAdapter adp = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_list_item_1, arrayListContactos );
                            lstContactos.setAdapter(adp);

                            //BUSCADOR
                            txtBuscar.addTextChangedListener(new TextWatcher() {
                                @Override
                                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                                }
                                @Override
                                public void onTextChanged(CharSequence s, int start, int before, int count) {
                                    adp.getFilter().filter(s);
                                }
                                @Override
                                public void afterTextChanged(Editable s) {
                                }
                            });

                        } catch (JSONException ex) {
                            Toast.makeText(getApplicationContext(), "ERROR CON ESTA MIERDA", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.i("Error en Response", "onResponse: " +  error.getMessage().toString() );
            }
        });
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

}