package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import compartido.Subasta;

public class ServidorSubastas {

    private static final int PUERTO = 5000;
    private static final int NUM_HILOS = 10;

    private ServerSocket servidor;
    private ExecutorService pool;
    private GestorSubastas gestor;

    public ServidorSubastas() throws IOException {
        this.servidor = new ServerSocket(PUERTO);
        this.pool = Executors.newFixedThreadPool(NUM_HILOS);
        this.gestor = new GestorSubastas();
    }

    public static void main(String[] args) {
        try {
            ServidorSubastas servidor = new ServidorSubastas();
            servidor.iniciar();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void iniciar() {
        inicializarSubastas();

        System.out.println("SERVIDOR DE SUBASTAS INICIADO");

        /*
         * // Thread para mostrar estado periódicamente
         * Thread mostrador = new Thread(() -> {
         * try {
         * while (true) {
         * Thread.sleep(30000); // Cada 30 segundos
         * gestor.mostrarEstadoSubastas();
         * }
         * } catch (InterruptedException e) {
         * Thread.currentThread().interrupt();
         * }
         * });
         * mostrador.setDaemon(true);
         * mostrador.start();
         */

        while (true) {
            try {
                Socket cliente = servidor.accept();
                System.out.println("Nuevo cliente conectado desde: " + cliente.getInetAddress());

                // Asignar cliente a un hilo del pool
                pool.execute(new ManejadorCliente(cliente, gestor));

            } catch (IOException e) {
                System.err.println("Error al aceptar cliente: " + e.getMessage());
            }
        }

    }

    private void inicializarSubastas() {
        gestor.agregarSubasta(new Subasta(1, "Laptop Dell XPS", 150.00));
        gestor.agregarSubasta(new Subasta(2, "iPhone 15 Pro", 200.00));
        gestor.agregarSubasta(new Subasta(3, "PlayStation 5", 300.00));
        gestor.agregarSubasta(new Subasta(4, "AirPods Pro", 75.00));
        gestor.agregarSubasta(new Subasta(5, "Monitor Samsung 4K", 250.00));

        System.out.println("[SERVIDOR] Se crearon 5 subastas de prueba");
    }

}
