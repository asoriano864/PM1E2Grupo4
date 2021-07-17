package com.example.pm1e2grupo4;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.List;

public class ActivityEdit extends AppCompatActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final int PETICION_ACCESO_PERMISOS = 100;
    byte[] byteArray;

    private String id;
    EditText txtNombre, txtTelefono;
    TextView txtLatitud, txtLongitud;
    ImageView imgFoto2;
    Contacto contactoBuscado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Button btnVolver2 = (Button) findViewById(R.id.btnListarContactos2);
        Button btnActualizarFoto = (Button) findViewById(R.id.btnTomarFoto2);
        Button btnActualizar = (Button) findViewById(R.id.btnGuardar2);
        Button btnEliminar = (Button) findViewById(R.id.btnEliminar);
        txtNombre = (EditText) findViewById(R.id.txtNombreContacto2);
        txtTelefono = (EditText) findViewById(R.id.txtTelefonoContacto2);
        imgFoto2 = (ImageView) findViewById(R.id.imgFoto2);
        txtLongitud = (TextView) findViewById(R.id.txtLongitud2);
        txtLatitud = (TextView) findViewById(R.id.txtLatitud2);

        Intent intent = getIntent();
        id = intent.getStringExtra("idCont");
        buscarContacto(id);

        btnActualizarFoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                permisos();
            }
        });

        btnActualizar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                actualizarContacto();
            }
        });

        btnEliminar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                eliminarContacto();
            }
        });

        btnVolver2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent pantallaVolver = new Intent(getApplicationContext(), ActivityListContactos.class);
                startActivity(pantallaVolver);
            }
        });
    }

    private void buscarContacto(String id) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = RestApiMethods.ApiGetID + id;

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject obj = new JSONObject(response);
                    JSONArray ContactoArray = obj.getJSONArray("Contactos");

                    for (int i = 0; i < ContactoArray.length(); i++) {
                        //getting the json object of the particular index inside the array
                        JSONObject contactoObject = ContactoArray.getJSONObject(i);

                        //creating a hero object and giving them the values from json object
                        contactoBuscado = new Contacto(contactoObject.getString("ID"),
                                contactoObject.getString("NOMBRE"),
                                contactoObject.getString("TELEFONO"),
                                contactoObject.getString("LATITUD"),
                                contactoObject.getString("LONGITUD"),
                                contactoObject.getString("FOTO"),
                                contactoObject.getString("ARCHIVO"));

                    }

                    byte[] foto = Base64.decode(contactoBuscado.getFoto().getBytes(), Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(foto, 0, foto.length);
                    imgFoto2.setImageBitmap(bitmap);

                    txtNombre.setText(contactoBuscado.getNombre());
                    txtTelefono.setText(contactoBuscado.getTelefono());
                    txtLongitud.setText(contactoBuscado.getLongitud());
                    txtLatitud.setText(contactoBuscado.getLatitud());

                } catch (JSONException ex) {
                    Toast.makeText(getApplicationContext(), "ERROR", Toast.LENGTH_LONG).show();
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

    //FUNCIONES REALCIONADAS A LA TOMA DE LA FOTOGRAFIA
    private void permisos() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(ActivityEdit.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PETICION_ACCESO_PERMISOS);
        } else {
            tomarFoto();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PETICION_ACCESO_PERMISOS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                tomarFoto();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Se necesitan permisos de acceso", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            getBytes(data);
        }
    }

    private void getBytes(Intent data) {
        Bitmap photo = (Bitmap) data.getExtras().get("data");
        imgFoto2.setImageBitmap(photo);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byteArray = stream.toByteArray();

        //SETEO DE DATOS EN EL OBJETO (FOTO BASE64 Y NOMBRE DEL ARCHIVO)
        String encode = Base64.encodeToString(byteArray, Base64.DEFAULT);
        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date());
        contactoBuscado.setFoto(encode);
        contactoBuscado.setArchivo(currentDateTimeString);
        obtenerLocalizacion();
    }

    private void tomarFoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void obtenerLocalizacion() {
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        encontrarUbicacion(getApplicationContext(), lm);
    }

    public void encontrarUbicacion(Context contexto, LocationManager locationManager) {
        String location_context = Context.LOCATION_SERVICE;
        locationManager = (LocationManager) contexto.getSystemService(location_context);
        List<String> providers = locationManager.getProviders(true);
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            locationManager.requestLocationUpdates(provider, 1000, 0,
                    new LocationListener() {

                        public void onLocationChanged(Location location) {
                            String longitud = String.valueOf(location.getLongitude());
                            String latitud = String.valueOf(location.getLatitude());
                            contactoBuscado.setLongitud(longitud);
                            contactoBuscado.setLatitud(latitud);
                            txtLongitud.setText(longitud);
                            txtLatitud.setText(latitud);
                        }

                        public void onProviderDisabled(String provider) {
                        }

                        public void onProviderEnabled(String provider) {
                        }

                        public void onStatusChanged(String provider, int status,
                                                    Bundle extras) {
                        }
                    });
            Location location = locationManager.getLastKnownLocation(provider);
            if (location != null) {
                String longitud = String.valueOf(location.getLongitude());
                String latitud = String.valueOf(location.getLatitude());
                contactoBuscado.setLongitud(longitud);
                contactoBuscado.setLatitud(latitud);
                txtLongitud.setText(longitud);
                txtLatitud.setText(latitud);
            }
        }
    }


    //FUNCIONES RELACIONADAS AL GUARDADO DEL CONTACTO Y MENSAJES DE ERROR
    private void actualizarContacto() {
        int comprobaciones = 0;
        int numeros = 0;
        if(txtNombre.getText().toString().isEmpty() || txtTelefono.getText().toString().isEmpty()) {
            mostrarDialogoVacios();
            comprobaciones = 1;
        }

        if(contactoBuscado.getFoto() == "" && comprobaciones == 0) {
            mostrarDialogoImagenNoTomada();
            comprobaciones = 1;
        }

        if((contactoBuscado.getLatitud() == "" || contactoBuscado.getLongitud() == "") && comprobaciones == 0) {
            mostrarDialogoLocalizacionNoEncontrada();
            comprobaciones = 1;
        }

        if(comprobaciones == 0) {
            for (int i = 0; i < txtNombre.getText().toString().length(); i++) {
                if (Character.isDigit(txtNombre.getText().toString().charAt(i))) {
                    mostrarDialogoNumeros();
                    numeros = 1;
                    break;
                }
            }

            if (numeros == 0) {
                contactoBuscado.setNombre(txtNombre.getText().toString());
                contactoBuscado.setTelefono(txtTelefono.getText().toString());
                JSONObject object = new JSONObject();
                String url = RestApiMethods.ApiUpdateUrl;
                try
                {
                    object.put("id",contactoBuscado.getId());
                    object.put("nombre",contactoBuscado.getNombre());
                    object.putOpt("telefono",contactoBuscado.getTelefono());
                    object.putOpt("latitud",contactoBuscado.getLatitud());
                    object.putOpt("longitud",contactoBuscado.getLongitud());
                    object.putOpt("foto",contactoBuscado.getFoto());
                    object.putOpt("archivo",contactoBuscado.getArchivo());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, object,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    Log.d("JSON", String.valueOf(response));
                                    String Error = response.getString("httpStatus");
                                    if (Error.equals("")||Error.equals(null)){
                                    }else if(Error.equals("OK")){
                                        JSONObject body = response.getJSONObject("body");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                Toast.makeText(getApplicationContext(),"Contacto Actualizado",Toast.LENGTH_LONG).show();
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        VolleyLog.d("Error", "Error: " + error.getMessage());
                        Toast.makeText(ActivityEdit.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
                RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                requestQueue.add(jsonObjectRequest);
            }
        }
    }

    private void mostrarDialogoVacios() {
        new AlertDialog.Builder(this)
                .setTitle("Alerta de Vacíos")
                .setMessage("No puede dejar ningún campo vacío")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    private void mostrarDialogoImagenNoTomada() {
        new AlertDialog.Builder(this)
                .setTitle("Alerta de Fotografía")
                .setMessage("No se ha tomado ninguna fotografía")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    private void mostrarDialogoLocalizacionNoEncontrada() {
        new AlertDialog.Builder(this)
                .setTitle("Alerta de Localización")
                .setMessage("No se ha encontrado su localización")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    private void mostrarDialogoNumeros() {
        new AlertDialog.Builder(this)
                .setTitle("Alerta de Números")
                .setMessage("No puede ingresar números en el campo de Nombre")
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                    }
                }).show();
    }

    private void eliminarContacto() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmación de Eliminación")
                .setMessage("¿Desea eliminar el contacto de " + txtNombre.getText() + "?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        RequestQueue queue = Volley.newRequestQueue(ActivityEdit.this);
                        String url = RestApiMethods.ApiDeleteUrl + id;
                        StringRequest stringRequest = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            @Override
                            public void onResponse(String response) {

                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                Log.i("Error en Response", "onResponse: " +  error.getMessage().toString() );
                            }
                        });
                        // Add the request to the RequestQueue.
                        queue.add(stringRequest);

                        Toast.makeText(getApplicationContext(), "Dato Eliminado", Toast.LENGTH_LONG).show();
                        Intent pantallaRegresoList = new Intent(getApplicationContext(), ActivityListContactos.class);
                        startActivity(pantallaRegresoList);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(getApplicationContext(), "Se canceló la eliminación", Toast.LENGTH_LONG).show();
                    }
                }).show();
    }

}