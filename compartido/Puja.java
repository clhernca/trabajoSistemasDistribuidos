package compartido;

import java.time.*;
import java.time.format.DateTimeFormatter;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Puja {
    private String usuario;
    private int idSubasta;
    private double cantidad;
    private long timestamp;

    public Puja() {
        // Dejar para lo de XML
    }

    public Puja(String usuario, int id, double cantidad) {
        this.usuario = usuario;
        this.idSubasta = id;
        this.cantidad = cantidad;
        this.timestamp = System.currentTimeMillis();
    }

     public String getUsuario() {
        return usuario;
    }

    public double getCantidad() {
        return cantidad;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getIdSubasta(){
        return this.idSubasta;
    }

    public String getFechaFormato() {
        LocalDateTime dateTime = java.time.Instant.ofEpochMilli(timestamp)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public String toString() {
        return usuario + " - €" + String.format("%.2f", cantidad) + " (" + getFechaFormato() + ")";
    }
}
