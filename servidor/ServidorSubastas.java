package servidor;

import compartido.Subasta;
import compartido.Usuario;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.List;

public class ServidorSubastas {

    private static final int PUERTO = 5000;
    // private static final int NUM_HILOS = 10;

    private ServerSocket servidor;
    private ExecutorService pool;
    private GestorSubastas gestorSubastas;
    private GestorUsuarios gestorUsuarios;

    public ServidorSubastas() throws IOException {
        this.servidor = new ServerSocket(PUERTO);
        this.pool = Executors.newCachedThreadPool();
        this.gestorSubastas = new GestorSubastas();
        this.gestorUsuarios = new GestorUsuarios();
    }

    public static void main(String[] args) {
        try {
            ServidorSubastas servidor = new ServidorSubastas();
            System.out.println("[SERVIDOR] Servidor de subastas iniciado en el puerto " + PUERTO);

            servidor.iniciar();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void iniciar() {
        cargarDatos();
        System.out.println("[SERVIDOR] Datos cargados");

        // Hilo que verifica subastas finalizadas cada 5 segundos
        iniciarTemporizador();

        // Hilo para mostrar estado periódicamente
        // iniciarMostrador();

        // Hilo para persistencia periódica
        iniciarPersistencia();

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

    private void iniciarTemporizador() {
        Thread temporizadorThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    while (true) {

                        Thread.sleep(5000); // Cada 5 segundos

                        List<Subasta> finalizadas = gestorSubastas.verificarSubastasFinalizadas();

                        if (!finalizadas.isEmpty()) {
                            System.out.println("[TEMPORIZADOR] Se han finalizado " + finalizadas.size() + " subastas:");
                            for (Subasta s : finalizadas) {
                                String ganador = s.getGanador() != null ? s.getGanador() : "Ninguno"; // Obtiene el
                                                                                                      // ganador
                                                                                                      // o "Ninguno"
                                System.out.println(s.getTitulo() + " → Ganador: " + ganador +
                                        " | Precio: " + String.format("%.2f", s.getPrecioFinal()) + "€");

                                // Registrar victoria del usuario
                                if (s.getGanador() != null && !s.getGanador().equals("Ninguno")) {
                                    Usuario ganadorUser = gestorUsuarios.obtenerUsuario(s.getGanador());
                                    if (ganadorUser != null) {
                                        ganadorUser.confirmarGasto(s.getPrecioFinal());
                                        ganadorUser.ganarSubasta(); // Incrementa el contador de subastas ganadas
                                    }
                                }
                            }
                            System.out.println();

                        }

                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Limpiar estado de interrupción
                    e.printStackTrace();
                }
            }
        });

        temporizadorThread.setDaemon(true);
        temporizadorThread.start();
    }

    private void iniciarPersistencia() {
        Thread persistenciaThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    while (true) {

                        Thread.sleep(10000); // Espera un minuto
                        GestorXML.guardarUsuarios(gestorUsuarios
                                .obtenerTodosUsuarios());
                        GestorXML.guardarSubastas(gestorSubastas.obtenerSubastas());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Restaurar el estado de interrupción
                    e.printStackTrace();
                }
            }
        });

        persistenciaThread.setDaemon(true); // Hilo daemon para que no bloquee el cierre del programa
        persistenciaThread.start();
    }

    private void cargarDatos() {
        System.out.println("[SERVIDOR] Cargando datos...");

        List<Usuario> usuariosGuardados = GestorXML.cargarUsuarios(); // Carga usuarios desde XML
        if (usuariosGuardados.isEmpty()) {
            gestorUsuarios.inicializarUsuariosDemo(); // Si no hay usuarios guardados, crea algunos de demo
        } else {
            for (Usuario u : usuariosGuardados) {
                gestorUsuarios.agregarUsuario(u); // Agrega cada usuario cargado al gestor de usuarios
            }
        }

        List<Subasta> subastasGuardadas = GestorXML.cargarSubastas(); // Lo mismo con las subastas
        if (subastasGuardadas.isEmpty()) {
            gestorSubastas.inicializarSubastasDemo();
        } else {
            for (Subasta s : subastasGuardadas) {
                gestorSubastas.agregarSubasta(s);
            }
        }
    }

}
