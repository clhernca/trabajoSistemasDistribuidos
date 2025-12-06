package compartido;

import java.util.*;

import jakarta.xml.bind.annotation.XmlElement;

public class Usuario implements java.io.Serializable {
    private String nombre;
    private String contraseña;
    private double saldo;
    private List<Puja> historialPujas;
    private int subastasGanadas;

    public Usuario() {
        // Constructor vacío para XML

    }


    public Usuario(String nombre, String contraseña, double saldoInicial) {
        this.nombre = nombre;
        this.contraseña = contraseña;
        this.saldo = saldoInicial;
        this.historialPujas = new ArrayList<>();
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

    @XmlElement
    public List<Puja> getHistorialPujas() {
        return historialPujas;
    }

    @XmlElement
    public int getSubastasGanadas() {
        return subastasGanadas;
    }

    public synchronized boolean puedePujar(double cantidad) {
        return saldo >= cantidad;
    }

    public synchronized void restarSaldo(double cantidad) {
        saldo -= cantidad;
    }

    public synchronized void sumarSaldo(double cantidad) {
        saldo += cantidad;
    }

    public synchronized void registrarPuja(Puja puja) {
        System.out.println("AÑADO PUJA");
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
    sb.append("\n═══════════════════════════════════════════════════════\n");
    sb.append("              HISTORIAL DE PUJAS DE ").append(nombre.toUpperCase()).append("\n");
    sb.append("═══════════════════════════════════════════════════════\n\n");
    
    for (Puja p : historialPujas) {
        sb.append("🔹 Subasta #").append(p.getIdSubasta()).append("\n");
        sb.append("   💵 Cantidad: €").append(String.format("%.2f", p.getCantidad())).append("\n");
        sb.append("   🕐 Hora: ").append(p.getFechaFormato()).append("\n\n");
    }
    
    sb.append("═══════════════════════════════════════════════════════\n");
    sb.append("Total de pujas realizadas: ").append(historialPujas.size()).append("\n");
    
    return sb.toString();
}

    public String toString() {
        return nombre + " (€" + String.format("%.2f", saldo) + ") - " +
               subastasGanadas + " subastas ganadas";
    }
}
