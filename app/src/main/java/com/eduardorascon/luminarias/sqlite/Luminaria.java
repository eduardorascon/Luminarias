package com.eduardorascon.luminarias.sqlite;

public class Luminaria {
    private int id;
    private String tipoPoste, tipoLampara, numeroLamparas;
    private String lon, lat;
    private String fechaHora;
    private int respladoDatos, respaldoImagen;

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

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public int getRespladoDatos() {
        return respladoDatos;
    }

    public void setRespladoDatos(int respladoDatos) {
        this.respladoDatos = respladoDatos;
    }

    public int getRespaldoImagen() {
        return respaldoImagen;
    }

    public void setRespaldoImagen(int respaldoImagen) {
        this.respaldoImagen = respaldoImagen;
    }

    public String getNumeroLamparas() {
        return numeroLamparas;
    }

    public void setNumeroLamparas(String numeroLamparas) {
        this.numeroLamparas = numeroLamparas;
    }
}