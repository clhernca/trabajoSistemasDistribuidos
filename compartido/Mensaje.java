package compartido;

import java.util.Arrays;

public class Mensaje {
    private String comando;
    private String[] parametros;

    public Mensaje(String comando, String... parametros) {
        this.comando = comando;
        this.parametros = parametros;
    }

    public static Mensaje parsear(String linea) {
        if (linea == null || linea.isEmpty()) {
            return null;
        }

        String[] partes = linea.split(":");
        String comando = partes[0];
        String[] parametros = Arrays.copyOfRange(partes, 1, partes.length);

        return new Mensaje(comando, parametros);
    }

    public String getComando() {
        return comando;
    }

    public String[] getParametros() {
        return parametros;
    }

    public String getParametro(int index) {
        if (index >= 0 && index < parametros.length) {
            return parametros[index];
        }
        return null;
    }

    public int getParametroInt(int index) {
        try {
            return Integer.parseInt(getParametro(index));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public double getParametroDouble(int index) {
        try {
            return Double.parseDouble(getParametro(index));
        } catch (NumberFormatException e) {
            return -1.0;
        }
    }

    @Override
    public String toString() {
        if (parametros == null || parametros.length == 0) {
            return comando;
        }

        StringBuilder sb = new StringBuilder(comando);
        for (String param : parametros) {
            sb.append(":").append(param);
        }
        return sb.toString();
    }
}
