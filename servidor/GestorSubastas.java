package servidor;

import compartido.Subasta;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    public synchronized boolean procesarPuja(int id, String usuario, double cantidad) {
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

    public List<Subasta> verificarSubastasFinalizadas() { // Devuelve las subastas finalizadas 
        List<Subasta> finalizadas = new ArrayList<>();
        synchronized (subastas) {
            for (Subasta s : subastas) {
                if (s.isActiva() && !s.estaActiva()) { // Si está activa pero ya ha pasado el tiempo
                    s.cerrar(s.getPujadorLider()); // La cierra y registra el ganador y precio final
                    finalizadas.add(s);
                }
            }
        }
        return finalizadas;
    }

    public List<Subasta> obtenerSubastasFinalizadas() {
        List<Subasta> finalizadas = new ArrayList<>();
        synchronized (subastas) {
            for (Subasta s : subastas) {
                if (!s.isActiva()) {
                    finalizadas.add(s);
                }
            }
        }
        return finalizadas;
    }

    public void inicializarSubastasDemo() {
        agregarSubasta(new Subasta(1, "Laptop Dell XPS", 150.00));
        agregarSubasta(new Subasta(2, "iPhone 15 Pro", 200.00));
        agregarSubasta(new Subasta(3, "PlayStation 5", 300.00));
        agregarSubasta(new Subasta(4, "AirPods Pro", 75.00));
        agregarSubasta(new Subasta(5, "Monitor Samsung 4K", 250.00));

        System.out.println("[SERVIDOR] Se crearon 5 subastas de prueba");
    
    }
}
