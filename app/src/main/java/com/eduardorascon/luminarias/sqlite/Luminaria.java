package com.eduardorascon.luminarias.sqlite;

public class Luminaria {
    private String imagen;
    private String altura;
    private String tipoPoste, tipoLampara;
    private String lon, lat;
    private String fechaHora;

    public String getImagen() {
        return imagen;
    }

    public void setImagen(String imagen) {
        this.imagen = imagen;
    }

    public String getAltura() {
        return altura;
    }

    public void setAltura(String altura) {
        this.altura = altura;
    }

    public String getTipoPoste() {
        return tipoPoste;
    }

    public void setTipoPoste(String tipoPoste) {
        this.tipoPoste = tipoPoste;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getTipoLampara() {
        return tipoLampara;
    }

    public void setTipoLampara(String tipoLampara) {
        this.tipoLampara = tipoLampara;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }
}
