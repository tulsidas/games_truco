package truco.common.model;

public enum Numero {
    ANCHO(1), DOS(2), TRES(3), CUATRO(4), CINCO(5), SEIS(6), SIETE(7), SOTA(0), CABALLO(
            0), REY(0);

    private int valorEnvido;

    private Numero(int valorEnvido) {
        this.valorEnvido = valorEnvido;
    }

    public int getValorEnvido() {
        return valorEnvido;
    }
}
