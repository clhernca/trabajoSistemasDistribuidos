package servidor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import compartido.Subasta;

public class GestorSubastas {
    private List<Subasta> subastas;

    public GestorSubastas() {
        this.subastas = Collections.synchronizedList(new ArrayList<>());
    }

    public void agregarSubasta(Subasta subasta) {
        synchronized (subastas) {
            subastas.add(subasta);
        }
    }

    public List<Subasta> obtenerSubastas() {
        synchronized (subastas) {
            return new ArrayList<>(subastas);
        }
    }

    public Subasta buscarSubasta(int id) {
        synchronized (subastas) {
            for (Subasta s : subastas) {
                if (s.getId() == id) {
                    return s;
                }
            }
        }
        return null;

    }

    public boolean procesarPuja(int id, String usuario, double cantidad) {
        Subasta subasta = buscarSubasta(id);
        if (subasta == null) {
            return false;
        }
        return subasta.pujar(usuario, cantidad);
    }

    public String obtenerListaSubastas() {
        StringBuilder sb = new StringBuilder();
        synchronized (subastas) {
            for (Subasta s : subastas) {
                sb.append(s.toString()).append("|");
            }
        }

        // Eliminar último separador
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }

        return "LISTA:" + sb.toString();
    }

    public void mostrarEstadoSubastas() {
        System.out.println("\n=== ESTADO ACTUAL DE SUBASTAS ===");
        synchronized (subastas) {
            for (Subasta s : subastas) {
                System.out.println(s.toStringDetallado());
                System.out.println();
            }
        }
    }

}
