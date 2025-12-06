package servidor;

import compartido.Subasta;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServidorSubastas {

    private static final int PUERTO = 5000;
    private static final int NUM_HILOS = 10; //POR QUÉ FIXED??

    private ServerSocket servidor;
    private ExecutorService pool;
    private GestorSubastas gestorSubastas;
    private GestorUsuarios gestorUsuarios;

    public ServidorSubastas() throws IOException {
        this.servidor = new ServerSocket(PUERTO);
        this.pool = Executors.newFixedThreadPool(NUM_HILOS);
        this.gestorSubastas = new GestorSubastas();
        this.gestorUsuarios = new GestorUsuarios();
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
        inicializarUsuarios();
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
                pool.execute(new ManejadorCliente(cliente, gestorSubastas, gestorUsuarios));

            } catch (IOException e) {
                System.err.println("Error al aceptar cliente: " + e.getMessage());
            }
        }

    }

    private void inicializarSubastas() {
        gestorSubastas.agregarSubasta(new Subasta(1, "Laptop Dell XPS", 150.00));
        gestorSubastas.agregarSubasta(new Subasta(2, "iPhone 15 Pro", 200.00));
        gestorSubastas.agregarSubasta(new Subasta(3, "PlayStation 5", 300.00));
        gestorSubastas.agregarSubasta(new Subasta(4, "AirPods Pro", 75.00));
        gestorSubastas.agregarSubasta(new Subasta(5, "Monitor Samsung 4K", 250.00));

        System.out.println("[SERVIDOR] Se crearon 5 subastas de prueba");
    }

    private void inicializarUsuarios(){
        gestorUsuarios.crearUsuario("pepe", "pass123", 500.00);
        gestorUsuarios.crearUsuario("luisa", "pass456", 400.00);
        gestorUsuarios.crearUsuario("juan", "pass789", 600.00);
    }

}
