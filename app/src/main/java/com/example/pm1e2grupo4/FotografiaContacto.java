package com.example.pm1e2grupo4;

import android.graphics.Bitmap;

public class FotografiaContacto {
    String id;
    Bitmap imagen;
    String nombre;
    String latitud;
    String longitud;

    public FotografiaContacto(Bitmap imagen, String nombre)
    {
        this.imagen = imagen;
        this.nombre = nombre;
        this.latitud = "";
        this.longitud = "";
        this.id = "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setImagen(Bitmap imagen) {
        this.imagen = imagen;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Bitmap getImagen()
    {
        return imagen;
    }

    public String getNombre()
    {
        return nombre;
    }

    public String getLatitud() {
        return latitud;
    }

    public void setLatitud(String latitud) {
        this.latitud = latitud;
    }

    public String getLongitud() {
        return longitud;
    }

    public void setLongitud(String longitud) {
        this.longitud = longitud;
    }
}