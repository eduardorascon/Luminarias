package com.eduardorascon.luminarias.sqlite;

public class Imagen {
    private int id, luminaria;
    private byte[] imagen;
    private String nombreImagen;

    public void setLuminaria(int luminaria) {
        this.luminaria = luminaria;
    }

    public int getLuminaria() {
        return luminaria;
    }

    public byte[] getImagen() {
        return imagen;
    }

    public void setImagen(byte[] imagen) {
        this.imagen = imagen;
    }

    public String getNombreImagen() {
        return nombreImagen;
    }

    public void setNombreImagen(String nombreImagen) {
        this.nombreImagen = nombreImagen;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
