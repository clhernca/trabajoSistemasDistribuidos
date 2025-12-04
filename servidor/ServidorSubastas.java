package servidor;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
        try (ServerSocket servidor = new ServerSocket(PUERTO)) {
            System.out.println("Servidor de subastas iniciado en el puerto 12345");
            while (true) {
                try {

                    // Manejar clientes
                }

                
            }
        } catch (Exception e) {
            e.printStackTrace();
    }
}
