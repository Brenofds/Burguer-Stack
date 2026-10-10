package modelo;

import java.awt.Color;

// Ingredientes do jogo, cada um com nome, letra e cor
// A ordem aqui é a das teclas 1 a 6
public enum Ingrediente {

    PAO(    "Pão",    'P', new Color( 222, 170,  90 ) ),
    CARNE(  "Carne",  'C', new Color( 110,  60,  30 ) ),
    QUEIJO( "Queijo", 'Q', new Color( 250, 200,  40 ) ),
    ALFACE( "Alface", 'A', new Color(  80, 180,  60 ) ),
    TOMATE( "Tomate", 'T', new Color( 220,  50,  40 ) ),
    BACON(  "Bacon",  'B', new Color( 200, 100, 110 ) );

    private final String nome;
    private final char letra;
    private final Color cor;

    Ingrediente( String nome, char letra, Color cor ) {
        this.nome = nome;
        this.letra = letra;
        this.cor = cor;
    }

    public String getNome() {
        return nome;
    }

    public char getLetra() {
        return letra;
    }

    public Color getCor() {
        return cor;
    }

    // escolhe texto preto ou branco conforme a cor do ingrediente (para dar leitura)
    public Color getCorTexto() {
        double luminancia = 0.299 * cor.getRed() + 0.587 * cor.getGreen() + 0.114 * cor.getBlue();
        return luminancia > 150 ? Color.BLACK : Color.WHITE;
    }

}
