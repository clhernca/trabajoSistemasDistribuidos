package cliente;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class ClienteSubastas {

    private static final String HOST = "localhost";
    private static final int PUERTO = 5000;

    private static Socket socket;
    private static BufferedReader in;
    private static PrintWriter out;
    private Scanner scanner;
    private String nombreUsuario;

    

    public static void main(String[] args) {
        try {

            socket = new Socket(HOST, PUERTO);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);

            System.out.println("Conectado al servidor de subastas en " + HOST + ":" + PUERTO);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
