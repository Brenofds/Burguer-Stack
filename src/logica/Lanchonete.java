package logica;

import estruturas.Fila;
import estruturas.Pilha;
import modelo.Acao;
import modelo.Cliente;
import modelo.Ingrediente;
import modelo.Pedido;

public class Lanchonete {

    // telas do jogo
    public enum Estado {
        INICIO, JOGANDO, PAUSADO, GAME_OVER
    }

    public static final int CAPACIDADE_FILA = 6;
    public static final int MAX_BANDEJA = 8;
    public static final int VIDAS_INICIAIS = 3;
    public static final int PONTOS_POR_NIVEL = 100;

    // posição da fila na tela (usada na animação dos clientes)
    public static final double FILA_X = 40;
    public static final double FILA_Y = 80;
    public static final double ESPACO_CLIENTE = 155;

    private static final double DURACAO_SAIDA = 0.8;
    private static final double DURACAO_MENSAGEM = 2.5;

    // FILA: atendimento por ordem de chegada
    private Fila<Cliente> fila;

    // PILHA: hamburguer sendo montado, só mexe no topo
    private Pilha<Ingrediente> bandeja;

    // PILHA: acoes feitas na bandeja, o Z desfaz a ultima
    private Pilha<Acao> historico;

    private Estado estado;
    private int pontos;
    private int vidas;
    private int nivel;
    private double tempoJogo;
    private double timerChegada;
    private int contadorClientes;

    // estatisticas da partida
    private int combo;
    private int maiorCombo;
    private int atendidos;
    private int erros;
    private int desistencias;

    // recorde não zera ao reiniciar
    private int recorde;
    private boolean novoRecorde;

    // cliente que acabou de sair (só para a animação)
    private Cliente clienteSaindo;
    private boolean saidaComSucesso;
    private double timerSaida;

    // mensagem que aparece na tela por alguns segundos
    private String mensagem;
    private boolean mensagemPositiva;
    private double timerMensagem;

    public Lanchonete() {
        recorde = 0;
        reiniciar();
        estado = Estado.INICIO; // começa na tela inicial
    }

    // começa uma partida nova
    public void reiniciar() {
        fila = new Fila<>();
        bandeja = new Pilha<>();
        historico = new Pilha<>();
        estado = Estado.JOGANDO;
        pontos = 0;
        vidas = VIDAS_INICIAIS;
        nivel = 1;
        tempoJogo = 0;
        timerChegada = 0;
        contadorClientes = 0;
        combo = 0;
        maiorCombo = 0;
        atendidos = 0;
        erros = 0;
        desistencias = 0;
        novoRecorde = false;
        clienteSaindo = null;
        timerSaida = 0;
        mensagem = "";
        timerMensagem = 0;
        chegarCliente();
    }

    public void iniciar() {
        if ( estado == Estado.INICIO ) {
            estado = Estado.JOGANDO;
        }
    }

    public void alternarPausa() {
        if ( estado == Estado.JOGANDO ) {
            estado = Estado.PAUSADO;
        } else if ( estado == Estado.PAUSADO ) {
            estado = Estado.JOGANDO;
        }
    }

    public void atualizar( double delta ) {

        // na tela inicial e na pausa nada anda
        if ( estado == Estado.INICIO || estado == Estado.PAUSADO ) {
            return;
        }

        // animacoes
        Object[] clientes = fila.toArray();
        for ( Object o : clientes ) {
            ( (Cliente) o ).animar( delta );
        }
        if ( clienteSaindo != null ) {
            clienteSaindo.animar( delta );
            clienteSaindo.subir( delta );
            timerSaida -= delta;
            if ( timerSaida <= 0 ) {
                clienteSaindo = null;
            }
        }
        if ( timerMensagem > 0 ) {
            timerMensagem -= delta;
        }

        if ( estado == Estado.GAME_OVER ) {
            return;
        }

        tempoJogo += delta;

        // de tempos em tempos chega um cliente novo
        timerChegada += delta;
        double intervalo = getIntervaloChegada();
        if ( fila.isEmpty() && timerChegada < intervalo - 1.0 ) {
            timerChegada = intervalo - 1.0; // fila vazia: o próximo chega logo
        }
        if ( timerChegada >= intervalo ) {
            timerChegada = 0;
            chegarCliente();
        }

        // só o cliente da frente perde paciência, se acabar, ele sai (dequeue) e perde vida
        if ( estado == Estado.JOGANDO && !fila.isEmpty() ) {
            Cliente frente = fila.peek();
            if ( frente.perderPaciencia( delta ) ) {
                fila.dequeue();
                desistencias++;
                iniciarSaida( frente, false );
                limparMontagem();
                perderVida( "Cliente #" + frente.getId() + " cansou de esperar!" );
            }
        }

    }

    // PUSH na bandeja e registra no histórico
    public void adicionarIngrediente( Ingrediente ingrediente ) {
        if ( estado != Estado.JOGANDO ) {
            return;
        }
        if ( bandeja.size() >= MAX_BANDEJA ) {
            mostrarMensagem( "Bandeja cheia! (máx. " + MAX_BANDEJA + ")", false );
            return;
        }
        bandeja.push( ingrediente );
        historico.push( new Acao( Acao.Tipo.PUSH, ingrediente ) );
    }

    // POP na bandeja (só sai o topo) e registra no histórico
    public void removerTopo() {
        if ( estado != Estado.JOGANDO ) {
            return;
        }
        if ( bandeja.isEmpty() ) {
            mostrarMensagem( "Bandeja vazia: nada para remover.", false );
            return;
        }
        Ingrediente removido = bandeja.pop();
        historico.push( new Acao( Acao.Tipo.POP, removido ) );
        mostrarMensagem( "POP! " + removido.getNome() + " saiu do topo da bandeja", true );
    }

    // tira a última açao do histórico e faz o contrário na bandeja
    public void desfazer() {
        if ( estado != Estado.JOGANDO ) {
            return;
        }
        if ( historico.isEmpty() ) {
            mostrarMensagem( "Nada para desfazer.", false );
            return;
        }
        Acao ultima = historico.pop();
        String nome = ultima.getIngrediente().getNome();
        if ( ultima.getTipo() == Acao.Tipo.PUSH ) {
            bandeja.pop(); // desfaz um PUSH
            mostrarMensagem( "POP! " + nome + " saiu do topo da bandeja (desfez PUSH)", true );
        } else {
            bandeja.push( ultima.getIngrediente() ); // desfaz um POP
            mostrarMensagem( "PUSH! " + nome + " voltou ao topo da bandeja (desfez POP)", true );
        }
    }

    // entrega para o cliente da frente (dequeue) e confere o pedido
    public void entregar() {
        if ( estado != Estado.JOGANDO ) {
            return;
        }
        if ( fila.isEmpty() ) {
            mostrarMensagem( "Nenhum cliente na fila.", false );
            return;
        }
        if ( bandeja.isEmpty() ) {
            mostrarMensagem( "Monte o hambúrguer antes de entregar!", false );
            return;
        }

        Cliente cliente = fila.dequeue();
        Pedido pedido = cliente.getPedido();
        String diagnostico = pedido.diagnosticar( bandeja ); // antes do confere, que esvazia a pilha
        boolean correto = pedido.confere( bandeja );

        if ( correto ) {
            atendidos++;
            combo++;
            maiorCombo = Math.max( maiorCombo, combo );
            // pontos: 10 por ingrediente + bônus de rapidez + bônus de combo
            int bonusRapidez = (int) ( 50 * cliente.getPercentualPaciencia() );
            int bonusCombo = Math.min( 50, 10 * ( combo - 1 ) );
            int ganho = 10 * pedido.getTamanho() + bonusRapidez + bonusCombo;
            int nivelAntes = nivel;
            pontos += ganho;
            nivel = 1 + pontos / PONTOS_POR_NIVEL;
            if ( pontos > recorde ) {
                recorde = pontos;
                novoRecorde = true;
            }
            String texto = "Pedido certo! +" + ganho;
            if ( combo >= 2 ) {
                texto += "  COMBO x" + combo;
            }
            if ( nivel > nivelAntes ) {
                texto += "  |  NÍVEL " + nivel + "!";
            }
            mostrarMensagem( texto, true );
        } else {
            erros++;
            perderVida( "Pedido errado! " + diagnostico );
        }

        iniciarSaida( cliente, correto );
        limparMontagem();
    }

    // novo cliente entra no FIM da fila, se a fila estiver cheia, perde vida
    private void chegarCliente() {
        if ( fila.size() >= CAPACIDADE_FILA ) {
            desistencias++;
            perderVida( "Fila cheia! Um cliente desistiu." );
            return;
        }
        contadorClientes++;
        Pedido pedido = Pedido.gerar( getMaxRecheios() );
        double pacienciaMax = Math.max( 8.0, 10.0 + 3.0 * pedido.getTamanho() - nivel );
        double xEntrada = FILA_X + CAPACIDADE_FILA * ESPACO_CLIENTE + 60;
        Cliente novo = new Cliente( contadorClientes, pedido, pacienciaMax, xEntrada, FILA_Y );
        fila.enqueue( novo );
        reposicionarFila();
    }

    // define para onde cada cliente anda conforme a posição dele na fila
    private void reposicionarFila() {
        Object[] clientes = fila.toArray();
        for ( int i = 0; i < clientes.length; i++ ) {
            ( (Cliente) clientes[i] ).setAlvoX( FILA_X + i * ESPACO_CLIENTE );
        }
    }

    private void iniciarSaida( Cliente cliente, boolean sucesso ) {
        clienteSaindo = cliente;
        saidaComSucesso = sucesso;
        timerSaida = DURACAO_SAIDA;
        cliente.setAlvoX( FILA_X - 120 );
        reposicionarFila();
    }

    // depois que o cliente sai, a bandeja e o histórico zeram
    private void limparMontagem() {
        bandeja.limpar();
        historico.limpar();
    }

    private void perderVida( String motivo ) {
        vidas--;
        combo = 0;
        mostrarMensagem( motivo, false );
        if ( vidas <= 0 ) {
            vidas = 0;
            estado = Estado.GAME_OVER;
        }
    }

    private void mostrarMensagem( String texto, boolean positiva ) {
        mensagem = texto;
        mensagemPositiva = positiva;
        timerMensagem = DURACAO_MENSAGEM;
    }

    // dificuldade: clientes chegam mais rápido a cada nível (mínimo 3 s)
    public double getIntervaloChegada() {
        return Math.max( 3.0, 7.0 - ( nivel - 1 ) * 0.6 );
    }

    // dificuldade: pedidos maiores a cada nível
    private int getMaxRecheios() {
        return Math.min( 4, 1 + nivel / 2 );
    }

    // getters
    public Fila<Cliente> getFila() {
        return fila;
    }

    public Pilha<Ingrediente> getBandeja() {
        return bandeja;
    }

    public Pilha<Acao> getHistorico() {
        return historico;
    }

    public Estado getEstado() {
        return estado;
    }

    public boolean isInicio() {
        return estado == Estado.INICIO;
    }

    public boolean isPausado() {
        return estado == Estado.PAUSADO;
    }

    public boolean isGameOver() {
        return estado == Estado.GAME_OVER;
    }

    public int getPontos() {
        return pontos;
    }

    public int getVidas() {
        return vidas;
    }

    public int getNivel() {
        return nivel;
    }

    public double getTempoJogo() {
        return tempoJogo;
    }

    public int getCombo() {
        return combo;
    }

    public int getMaiorCombo() {
        return maiorCombo;
    }

    public int getAtendidos() {
        return atendidos;
    }

    public int getErros() {
        return erros;
    }

    public int getDesistencias() {
        return desistencias;
    }

    public int getRecorde() {
        return recorde;
    }

    public boolean isNovoRecorde() {
        return novoRecorde;
    }

    public Cliente getClienteSaindo() {
        return clienteSaindo;
    }

    public boolean isSaidaComSucesso() {
        return saidaComSucesso;
    }

    public double getAlphaSaida() {
        return Math.max( 0, timerSaida / DURACAO_SAIDA );
    }

    public String getMensagem() {
        return timerMensagem > 0 ? mensagem : "";
    }

    public boolean isMensagemPositiva() {
        return mensagemPositiva;
    }

}
