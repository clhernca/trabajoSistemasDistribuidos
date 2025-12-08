package compartido;

import java.util.*;

import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementWrapper;

public class Usuario implements java.io.Serializable {
    private String nombre;
    private String contraseña;
    private double saldo;
    private double saldoBloqueado;
    private List<Puja> historialPujas  = new ArrayList<>();
    private int subastasGanadas;

    public Usuario() {
        // Constructor vacío para XML

    }


    public Usuario(String nombre, String contraseña, double saldoInicial) {
        this.nombre = nombre;
        this.contraseña = contraseña;
        this.saldo = saldoInicial;
        this.saldoBloqueado = 0.0;
        //this.historialPujas = new ArrayList<>();
        this.subastasGanadas = 0;
    }

    @XmlElement
    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @XmlElement
    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    @XmlElement
    public double getSaldo() {
        return saldo;
    }

    public void setSaldo(double saldo) {
        this.saldo = saldo;
    }

    public double getSaldoDisponible() {
        return saldo - saldoBloqueado;
    }
    
    public double getSaldoBloqueado() {
        return saldoBloqueado;
    }
    
    public boolean puedePujar(double cantidad) {
        return getSaldoDisponible() >= cantidad;
    }
    
    public synchronized void bloquearDinero(double cantidad) {
        if (getSaldoDisponible() >= cantidad) {
            saldoBloqueado += cantidad;
        }
    }
    
    public synchronized void liberarDinero(double cantidad) {
        saldoBloqueado -= cantidad;
    }
    
    public synchronized void confirmarGasto(double cantidad) {
        saldoBloqueado -= cantidad;
        saldo -= cantidad;
        if (saldoBloqueado < 0) saldoBloqueado = 0;
    }
    
    public synchronized void cancelarBloqueo(double cantidad) {
        liberarDinero(cantidad);
    }

    @XmlElementWrapper(name = "historialPujas")
    @XmlElement (name = "puja")
    public List<Puja> getHistorialPujas() {
        return historialPujas;
    }

    @XmlElement
    public int getSubastasGanadas() {
        return subastasGanadas;
    }

    public synchronized void restarSaldo(double cantidad) {
        saldo -= cantidad;
    }

    public synchronized void sumarSaldo(double cantidad) {
        saldo += cantidad;
    }

    public synchronized void registrarPuja(Puja puja) {
        historialPujas.add(puja);
    }

    public synchronized void ganarSubasta() {
        subastasGanadas++;
    }

    public String mostrarHistorial() {
    if (historialPujas == null || historialPujas.isEmpty()) {
        return "No has realizado ninguna puja todavía.";
    }
    
    StringBuilder sb = new StringBuilder();
    sb.append("=== HISTORIAL DE PUJAS DE ").append(nombre.toUpperCase()).append(" ===\n");
    
    for (Puja p : historialPujas) {
        sb.append("Subasta ").append(p.getIdSubasta())
          .append(" | Cantidad: ").append(String.format("%.2f", p.getCantidad()))
          .append("€ | Fecha: ").append(p.getFechaFormato())
          .append("\n");
    }
    
    sb.append("Total de pujas realizadas: ").append(historialPujas.size());
    
    return sb.toString();
}

    public String toString() {
        return nombre + " (€" + String.format("%.2f", saldo) + ") - " +
               subastasGanadas + " subastas ganadas";
    }

}
