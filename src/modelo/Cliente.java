package modelo;

// Cliente da fila: tem pedido, paciência e posição na tela (para a animação)
public class Cliente {

    private final int id;
    private final Pedido pedido;
    private final double pacienciaMax;
    private double paciencia;

    // posição na tela; alvoX é para onde ele está andando
    private double x;
    private double y;
    private double alvoX;

    public Cliente( int id, Pedido pedido, double pacienciaMax, double xInicial, double y ) {
        this.id = id;
        this.pedido = pedido;
        this.pacienciaMax = pacienciaMax;
        this.paciencia = pacienciaMax;
        this.x = xInicial;
        this.alvoX = xInicial;
        this.y = y;
    }

    // diminui a paciência pelo tempo passado; retorna true quando acaba
    public boolean perderPaciencia( double delta ) {
        paciencia -= delta;
        if ( paciencia < 0 ) {
            paciencia = 0;
        }
        return paciencia <= 0;
    }

    // anda suavemente até o alvoX (usa delta, então independe do FPS)
    public void animar( double delta ) {
        double fator = Math.min( 1.0, 8.0 * delta );
        x += ( alvoX - x ) * fator;
    }

    // animação de saída
    public void subir( double delta ) {
        y -= 120 * delta;
    }

    public int getId() {
        return id;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public double getPaciencia() {
        return paciencia;
    }

    public double getPacienciaMax() {
        return pacienciaMax;
    }

    public double getPercentualPaciencia() {
        return paciencia / pacienciaMax;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public void setAlvoX( double alvoX ) {
        this.alvoX = alvoX;
    }

    @Override
    public String toString() {
        return "#" + id;
    }

}
