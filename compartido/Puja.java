package compartido;

import java.time.*;
import java.time.format.DateTimeFormatter;

public class Puja {
    private String usuario;
    private double cantidad;
    private long timestamp;

    public Puja(String usuario, double cantidad) {
        this.usuario = usuario;
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

    public String getFechaFormato() {
        LocalDateTime dateTime = java.time.Instant.ofEpochMilli(timestamp)
                .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    public String toString() {
        return usuario + " - €" + String.format("%.2f", cantidad) + " (" + getFechaFormato() + ")";
    }
}
