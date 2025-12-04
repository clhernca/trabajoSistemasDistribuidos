package cliente;

import java.net.Socket;

public class ClienteSubastas {

     private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    public static void main(String[] args) {
        try (Socket socket = new Socket(HOST, PUERTO)) {
            System.out.println("Conectado al servidor de subastas");
            // Lógica para interactuar con el servidor de subastas
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
