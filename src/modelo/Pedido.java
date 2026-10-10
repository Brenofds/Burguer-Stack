package modelo;

import br.com.davidbuzatto.jsge.math.MathUtils;
import estruturas.Pilha;

// Pedido do cliente: lista de ingredientes da base até o topo
public class Pedido {

    private final Ingrediente[] itens;

    public Pedido( Ingrediente[] itens ) {
        this.itens = itens;
    }

    // gera pedido aleatório: pão + recheios + pão
    public static Pedido gerar( int maxRecheios ) {
        int recheios = MathUtils.getRandomValue( 1, maxRecheios );
        Ingrediente[] itens = new Ingrediente[recheios + 2];
        Ingrediente[] todos = Ingrediente.values();
        itens[0] = Ingrediente.PAO;
        for ( int i = 1; i <= recheios; i++ ) {
            itens[i] = todos[MathUtils.getRandomValue( 1, todos.length - 1 )];
        }
        itens[itens.length - 1] = Ingrediente.PAO;
        return new Pedido( itens );
    }

    // confere a bandeja com o pedido desempilhando do topo para a base
    // Obs: esvazia a pilha (por isso é chamado só na entrega).
    public boolean confere( Pilha<Ingrediente> bandeja ) {
        if ( bandeja.size() != itens.length ) {
            return false;
        }
        for ( int i = itens.length - 1; i >= 0; i-- ) {
            if ( bandeja.pop() != itens[i] ) {
                return false;
            }
        }
        return true;
    }

    // explica o primeiro erro da bandeja
    public String diagnosticar( Pilha<Ingrediente> bandeja ) {
        Object[] montado = bandeja.toArray();
        int menor = Math.min( montado.length, itens.length );
        for ( int i = 0; i < menor; i++ ) {
            if ( montado[i] != itens[i] ) {
                return "Camada " + ( i + 1 ) + ": esperava " + itens[i].getNome()
                        + ", veio " + ( (Ingrediente) montado[i] ).getNome();
            }
        }
        if ( montado.length < itens.length ) {
            return "Faltou " + ( itens.length - montado.length ) + " ingrediente(s)";
        }
        if ( montado.length > itens.length ) {
            return "Sobrou " + ( montado.length - itens.length ) + " ingrediente(s)";
        }
        return "Correto";
    }

    // diz se a camada (0 = base) está igual ao pedido; usado nas bolinhas verde/vermelha
    public boolean itemCorreto( int camada, Ingrediente ingrediente ) {
        return camada < itens.length && itens[camada] == ingrediente;
    }

    public int getTamanho() {
        return itens.length;
    }

    public Ingrediente[] getItens() {
        return itens;
    }

}
