package template;

import br.com.davidbuzatto.jsge.core.engine.EngineFrame;
import br.com.davidbuzatto.jsge.core.utils.ColorUtils;
import java.awt.Color;
import logica.Lanchonete;
import modelo.Acao;
import modelo.Cliente;
import modelo.Ingrediente;
import modelo.Pedido;

public class Main extends EngineFrame {

    // tamanhos e posições da tela
    private static final int LARGURA = 1000;
    private static final int ALTURA = 600;

    private static final double CARTAO_L = 145;
    private static final double CARTAO_A = 120;

    private static final double BOTAO_X = 30;
    private static final double BOTAO_Y = 258;
    private static final double BOTAO_L = 210;
    private static final double BOTAO_A = 38;
    private static final double BOTAO_ESPACO = 44;

    private static final double ENTREGAR_X = 30;
    private static final double ENTREGAR_Y = 526;
    private static final double ENTREGAR_L = 210;
    private static final double ENTREGAR_A = 40;

    private static final double BANDEJA_BASE_Y = 550;
    private static final double BLOCO_X = 300;
    private static final double BLOCO_L = 180;
    private static final double BLOCO_A = 30;

    private static final double HIST_X = 690;
    private static final double HIST_Y = 232;
    private static final int HIST_MAX_LINHAS = 10;

    // o que está embaixo do mouse
    private static final int NENHUM = -1;
    private static final int BOTAO_ENTREGAR = 6;
    private static final int BLOCO_TOPO = 7;

    // limita o delta para o jogo não "pular" se a janela travar
    private static final double DELTA_MAXIMO = 0.1;

    // cores
    private static final Color FUNDO = new Color( 250, 243, 230 );
    private static final Color MARROM = new Color( 70, 40, 25 );
    private static final Color CINZA = new Color( 150, 150, 150 );
    private static final Color CINZA_CLARO = new Color( 225, 225, 225 );
    private static final Color CINZA_VAGA = new Color( 210, 205, 195 );
    private static final Color VERDE = new Color( 40, 160, 70 );
    private static final Color AMARELO = new Color( 230, 180, 30 );
    private static final Color VERMELHO = new Color( 210, 50, 50 );
    private static final Color LARANJA = new Color( 190, 130, 0 );
    private static final Color PELE = new Color( 240, 200, 160 );
    private static final Color PAINEL = new Color( 255, 252, 245 );

    private static final int[] TECLAS_INGREDIENTES = {
        KEY_ONE, KEY_TWO, KEY_THREE, KEY_FOUR, KEY_FIVE, KEY_SIX
    };

    private Lanchonete jogo;
    private int botaoSobMouse;

    public Main() {

        super(
            LARGURA,
            ALTURA,
            "Burger Stack",
            60,
            true,
            false,
            false,
            false,
            false,
            false
        );

    }

    @Override
    public void create() {
        setExitKey( KEY_NULL ); // ESC  pausa
        jogo = new Lanchonete();
        botaoSobMouse = NENHUM;
    }

    @Override
    public void update( double delta ) {

        delta = Math.min( delta, DELTA_MAXIMO );
        botaoSobMouse = NENHUM;

        // tela inicial
        if ( jogo.isInicio() ) {
            if ( isKeyPressed( KEY_ENTER ) || isKeyPressed( KEY_SPACE )
                    || isMouseButtonPressed( MOUSE_BUTTON_LEFT ) ) {
                jogo.iniciar();
            }
            return;
        }

        // fim de jogo: R reinicia
        if ( jogo.isGameOver() ) {
            if ( isKeyPressed( KEY_R ) ) {
                jogo.reiniciar();
            }
            jogo.atualizar( delta );
            return;
        }

        // pausa
        if ( isKeyPressed( KEY_ESCAPE ) ) {
            jogo.alternarPausa();
        }
        if ( jogo.isPausado() ) {
            return;
        }

        // teclado
        Ingrediente[] ingredientes = Ingrediente.values();
        for ( int i = 0; i < ingredientes.length; i++ ) {
            if ( isKeyPressed( TECLAS_INGREDIENTES[i] ) ) {
                jogo.adicionarIngrediente( ingredientes[i] ); // push
            }
        }
        if ( isKeyPressed( KEY_BACKSPACE ) ) {
            jogo.removerTopo(); // pop
        }
        if ( isKeyPressed( KEY_Z ) ) {
            jogo.desfazer();
        }
        if ( isKeyPressed( KEY_ENTER ) || isKeyPressed( KEY_SPACE ) ) {
            jogo.entregar(); // dequeue
        }

        // mouse: ve o que está embaixo do cursor
        int mx = getMouseX();
        int my = getMouseY();
        for ( int i = 0; i < ingredientes.length; i++ ) {
            if ( dentro( mx, my, BOTAO_X, BOTAO_Y + i * BOTAO_ESPACO, BOTAO_L, BOTAO_A ) ) {
                botaoSobMouse = i;
            }
        }
        if ( dentro( mx, my, ENTREGAR_X, ENTREGAR_Y, ENTREGAR_L, ENTREGAR_A ) ) {
            botaoSobMouse = BOTAO_ENTREGAR;
        }
        int tamanhoBandeja = jogo.getBandeja().size();
        if ( tamanhoBandeja > 0
                && dentro( mx, my, BLOCO_X, yBloco( tamanhoBandeja - 1 ), BLOCO_L, BLOCO_A ) ) {
            botaoSobMouse = BLOCO_TOPO; // só o topo da pilha é clicável
        }

        if ( isMouseButtonPressed( MOUSE_BUTTON_LEFT ) ) {
            if ( botaoSobMouse >= 0 && botaoSobMouse < ingredientes.length ) {
                jogo.adicionarIngrediente( ingredientes[botaoSobMouse] );
            } else if ( botaoSobMouse == BOTAO_ENTREGAR ) {
                jogo.entregar();
            } else if ( botaoSobMouse == BLOCO_TOPO ) {
                jogo.removerTopo();
            }
        }

        jogo.atualizar( delta );

    }

    @Override
    public void draw() {

        clearBackground( FUNDO );
        setFontStyle( FONT_BOLD );

        desenharHud();
        desenharFila();
        desenharMensagem();
        desenharPrateleira();
        desenharBandeja();
        desenharPedidoDaFrente();
        desenharHistorico();

        String ajuda = "1-6/clique: PUSH | Backspace/clique no topo: POP | Z: desfazer"
                + " | Enter/Espaço: entregar | Esc: pausa";
        drawText( ajuda, LARGURA / 2 - measureText( ajuda, 12 ) / 2, 580, 12, DARKGRAY );

        if ( jogo.isInicio() ) {
            desenharInicio();
        } else if ( jogo.isPausado() ) {
            desenharPausa();
        } else if ( jogo.isGameOver() ) {
            desenharGameOver();
        }

    }

    private void desenharHud() {
        fillRectangle( 0, 0, LARGURA, 40, MARROM );
        drawText( "BURGER STACK", 15, 11, 22, GOLD );
        drawText( "Pontos: " + jogo.getPontos(), 220, 13, 18, WHITE );
        drawText( "Recorde: " + jogo.getRecorde(), 375, 13, 18, CINZA_CLARO );
        drawText( "Nível: " + jogo.getNivel(), 540, 13, 18, WHITE );
        drawText( "Tempo: " + formatarTempo( jogo.getTempoJogo() ), 645, 13, 18, WHITE );
        drawText( "Vidas:", 800, 13, 18, WHITE );
        for ( int i = 0; i < Lanchonete.VIDAS_INICIAIS; i++ ) {
            Color cor = i < jogo.getVidas() ? VERMELHO : DARKGRAY;
            fillCircle( 880 + i * 26, 20, 9, cor );
        }
    }

    // FILA na horizontal
    private void desenharFila() {

        String titulo = "FILA DE CLIENTES (FIFO)   size() = " + jogo.getFila().size()
                + "/" + Lanchonete.CAPACIDADE_FILA
                + "   - sai pela FRENTE, entra pelo FIM";
        drawText( titulo, Lanchonete.FILA_X, 47, 14, MARROM );

        for ( int i = 0; i < Lanchonete.CAPACIDADE_FILA; i++ ) {
            double x = Lanchonete.FILA_X + i * Lanchonete.ESPACO_CLIENTE;
            drawRoundRectangle( x, Lanchonete.FILA_Y, CARTAO_L, CARTAO_A, 15, CINZA_VAGA ); // vagas (capacidade)
            String vaga = "vaga " + ( i + 1 );
            drawText( vaga, x + CARTAO_L / 2 - measureText( vaga, 12 ) / 2, Lanchonete.FILA_Y + 54, 12, CINZA_VAGA );
        }

        Object[] clientes = jogo.getFila().toArray();

        for ( int i = 0; i < clientes.length; i++ ) {
            Cliente c = (Cliente) clientes[i];
            desenharCartao( c, c.getX(), c.getY(), 1.0, i == 0 );
        }

        if ( clientes.length > 0 ) {
            Cliente frente = (Cliente) clientes[0];
            drawText( "FRENTE (próximo)", frente.getX(), 64, 12, LARANJA );
            Cliente fim = (Cliente) clientes[clientes.length - 1];
            double xFim = fim.getX() + CARTAO_L - measureText( "FIM", 12 );
            drawText( "FIM", xFim, 64, 12, DARKGRAY );
        }

        Cliente saindo = jogo.getClienteSaindo();
        if ( saindo != null ) {
            double a = jogo.getAlphaSaida();
            desenharCartao( saindo, saindo.getX(), saindo.getY(), a, false );
            String txt = jogo.isSaidaComSucesso() ? "OK!" : "X";
            Color cor = jogo.isSaidaComSucesso() ? VERDE : VERMELHO;
            drawText( txt, saindo.getX() + CARTAO_L / 2 - measureText( txt, 40 ) / 2,
                    saindo.getY() + 35, 40, ColorUtils.fade( cor, a ) );
        }

    }

    // cartao do cliente: rosto, barra de paciência e o pedido dele
    private void desenharCartao( Cliente c, double x, double y, double alpha, boolean frente ) {

        double p = c.getPercentualPaciencia();

        fillRoundRectangle( x, y, CARTAO_L, CARTAO_A, 15, ColorUtils.fade( WHITE, alpha ) );
        if ( frente ) {
            // pisca vermelho quando a paciência está acabando
            boolean piscar = p < 0.25 && ( (int) ( jogo.getTempoJogo() * 4 ) ) % 2 == 0;
            Color borda = piscar ? VERMELHO : GOLD;
            drawRoundRectangle( x, y, CARTAO_L, CARTAO_A, 15, borda );
            drawRoundRectangle( x + 1, y + 1, CARTAO_L - 2, CARTAO_A - 2, 15, borda );
            drawRoundRectangle( x + 2, y + 2, CARTAO_L - 4, CARTAO_A - 4, 15, borda );
        } else {
            drawRoundRectangle( x, y, CARTAO_L, CARTAO_A, 15, ColorUtils.fade( CINZA, alpha ) );
        }

        fillCircle( x + 25, y + 27, 14, ColorUtils.fade( PELE, alpha ) );
        fillCircle( x + 20, y + 24, 2, ColorUtils.fade( BLACK, alpha ) );
        fillCircle( x + 30, y + 24, 2, ColorUtils.fade( BLACK, alpha ) );
        drawText( "Cliente #" + c.getId(), x + 46, y + 20, 13, ColorUtils.fade( MARROM, alpha ) );

        Color corBarra = p > 0.5 ? VERDE : ( p > 0.25 ? AMARELO : VERMELHO );
        fillRectangle( x + 10, y + 48, CARTAO_L - 20, 10, ColorUtils.fade( CINZA_CLARO, alpha ) );
        fillRectangle( x + 10, y + 48, ( CARTAO_L - 20 ) * p, 10, ColorUtils.fade( corBarra, alpha ) );
        drawRectangle( x + 10, y + 48, CARTAO_L - 20, 10, ColorUtils.fade( CINZA, alpha ) );

        Ingrediente[] itens = c.getPedido().getItens();
        for ( int i = 0; i < itens.length; i++ ) {
            double ix = x + 10 + i * 21;
            double iy = y + 68;
            fillRoundRectangle( ix, iy, 19, 19, 5, ColorUtils.fade( itens[i].getCor(), alpha ) );
            String letra = String.valueOf( itens[i].getLetra() );
            drawText( letra, ix + 10 - measureText( letra, 12 ) / 2, iy + 5, 12,
                    ColorUtils.fade( itens[i].getCorTexto(), alpha ) );
        }
        drawText( "base -> topo", x + 10, y + 96, 10, ColorUtils.fade( CINZA, alpha ) );

    }

    private void desenharMensagem() {
        String msg = jogo.getMensagem();
        if ( msg.isEmpty() ) {
            return;
        }
        double l = measureText( msg, 15 ) + 30;
        double x = LARGURA / 2 - l / 2;
        Color cor = jogo.isMensagemPositiva() ? VERDE : VERMELHO;
        fillRoundRectangle( x, 205, l, 24, 12, cor );
        drawText( msg, x + 15, 210, 15, WHITE );
    }

    private void desenharPrateleira() {
        drawText( "PRATELEIRA", BOTAO_X, 236, 14, MARROM );
        Ingrediente[] ingredientes = Ingrediente.values();
        for ( int i = 0; i < ingredientes.length; i++ ) {
            Ingrediente ing = ingredientes[i];
            double y = BOTAO_Y + i * BOTAO_ESPACO;
            fillRoundRectangle( BOTAO_X, y, BOTAO_L, BOTAO_A, 12, ing.getCor() );
            if ( botaoSobMouse == i ) {
                desenharBordaDestaque( BOTAO_X, y, BOTAO_L, BOTAO_A );
            }
            drawText( "[" + ( i + 1 ) + "]  " + ing.getNome(), BOTAO_X + 14, y + 12, 16, ing.getCorTexto() );
        }

        Color corEntregar = botaoSobMouse == BOTAO_ENTREGAR ? new Color( 30, 130, 55 ) : VERDE;
        fillRoundRectangle( ENTREGAR_X, ENTREGAR_Y, ENTREGAR_L, ENTREGAR_A, 12, corEntregar );
        String txt = "ENTREGAR [Enter/Espaço]";
        drawText( txt, ENTREGAR_X + ENTREGAR_L / 2 - measureText( txt, 14 ) / 2, ENTREGAR_Y + 14, 14, WHITE );
    }

    // pilha da bandeja desenhada de baixo (base) para cima (topo)
    private void desenharBandeja() {

        Object[] itens = jogo.getBandeja().toArray();

        drawText( "BANDEJA (PILHA - LIFO)", 280, 236, 14, MARROM );
        drawText( "size() = " + itens.length + "/" + Lanchonete.MAX_BANDEJA, 280, 254, 13, DARKGRAY );
        drawText( "clique no topo = POP", 280, 270, 11, CINZA );

        fillRoundRectangle( 280, BANDEJA_BASE_Y, 220, 12, 10, CINZA );

        if ( itens.length == 0 ) {
            drawText( "(vazia)", 360, BANDEJA_BASE_Y - 30, 16, CINZA );
            return;
        }

        Pedido alvo = jogo.getFila().isEmpty() ? null : jogo.getFila().peek().getPedido();

        for ( int i = 0; i < itens.length; i++ ) {
            Ingrediente ing = (Ingrediente) itens[i];
            double y = yBloco( i );
            fillRoundRectangle( BLOCO_X, y, BLOCO_L, BLOCO_A, 12, ing.getCor() );
            String txt = ing.getLetra() + " - " + ing.getNome();
            drawText( txt, BLOCO_X + BLOCO_L / 2 - measureText( txt, 14 ) / 2, y + 9, 14, ing.getCorTexto() );

            // bolinha verde = camada igual ao pedido, vermelha = diferente
            if ( alvo != null ) {
                Color marca = alvo.itemCorreto( i, ing ) ? VERDE : VERMELHO;
                fillCircle( BLOCO_X - 11, y + BLOCO_A / 2, 6, marca );
            }

            if ( i == itens.length - 1 ) {
                if ( botaoSobMouse == BLOCO_TOPO ) {
                    desenharBordaDestaque( BLOCO_X, y, BLOCO_L, BLOCO_A );
                }
                drawText( "<- TOPO", BLOCO_X + BLOCO_L + 6, y + 9, 14, VERMELHO );
            }
        }

    }

    // pedido do cliente da frente, para comparar com a bandeja
    private void desenharPedidoDaFrente() {

        drawText( "PEDIDO DA FRENTE", 545, 236, 13, MARROM );

        if ( jogo.getFila().isEmpty() ) {
            drawText( "-", 600, 260, 14, CINZA );
            return;
        }

        Cliente frente = jogo.getFila().peek();
        drawText( "Cliente #" + frente.getId(), 560, 254, 12, DARKGRAY );
        if ( jogo.getCombo() >= 2 ) {
            drawText( "COMBO x" + jogo.getCombo(), 565, 272, 13, LARANJA );
        }

        Ingrediente[] itens = frente.getPedido().getItens();
        for ( int i = 0; i < itens.length; i++ ) {
            double y = BANDEJA_BASE_Y - ( i + 1 ) * 24;
            fillRoundRectangle( 560, y, 100, 22, 8, itens[i].getCor() );
            String txt = itens[i].getLetra() + " " + itens[i].getNome();
            drawText( txt, 610 - measureText( txt, 12 ) / 2, y + 6, 12, itens[i].getCorTexto() );
        }

    }

    // PILHA do historico: a ação mais recente (topo) fica em cima
    private void desenharHistorico() {

        Object[] acoes = jogo.getHistorico().toArray();

        fillRoundRectangle( HIST_X, HIST_Y, 290, 334, 12, PAINEL );
        drawRoundRectangle( HIST_X, HIST_Y, 290, 334, 12, CINZA );
        drawText( "HISTÓRICO (PILHA)", HIST_X + 10, HIST_Y + 8, 14, MARROM );
        drawText( "size() = " + acoes.length + "    [Z] desfaz o topo", HIST_X + 10, HIST_Y + 27, 12, DARKGRAY );

        if ( acoes.length == 0 ) {
            drawText( "(vazio)", HIST_X + 110, HIST_Y + 60, 14, CINZA );
            return;
        }

        int linhas = Math.min( HIST_MAX_LINHAS, acoes.length );
        for ( int k = 0; k < linhas; k++ ) {
            Acao acao = (Acao) acoes[acoes.length - 1 - k]; // percorre do topo para baixo
            double y = HIST_Y + 48 + k * 27;
            Color fundoLinha = k == 0 ? new Color( 255, 235, 160 ) : CINZA_CLARO;
            fillRoundRectangle( HIST_X + 10, y, 270, 24, 8, fundoLinha );
            fillRoundRectangle( HIST_X + 16, y + 4, 16, 16, 4, acao.getIngrediente().getCor() );
            Color corTipo = acao.getTipo() == Acao.Tipo.PUSH ? VERDE : VERMELHO;
            drawText( acao.toString(), HIST_X + 40, y + 6, 14, corTipo );
            if ( k == 0 ) {
                drawText( "<- TOPO", HIST_X + 215, y + 6, 12, VERMELHO );
            }
        }
        if ( acoes.length > HIST_MAX_LINHAS ) {
            drawText( "... +" + ( acoes.length - HIST_MAX_LINHAS ) + " mais antigas",
                    HIST_X + 10, HIST_Y + 48 + HIST_MAX_LINHAS * 27, 11, CINZA );
        }

    }

    private void desenharInicio() {
        fillRectangle( 0, 0, LARGURA, ALTURA, ColorUtils.fade( BLACK, 0.88 ) );
        centralizar( "BURGER STACK", 60, 56, GOLD );
        centralizar( "Monte os hambúrgueres na ordem certa e atenda a fila!", 140, 18, WHITE );

        centralizar( "FILA (FIFO): clientes entram pelo FIM e são atendidos pela FRENTE.", 200, 15, CINZA_CLARO );
        centralizar( "PILHA (LIFO) da bandeja: ingredientes empilhados; só o TOPO sai.", 228, 15, CINZA_CLARO );
        centralizar( "PILHA do histórico: Z desfaz sempre a última ação.", 256, 15, CINZA_CLARO );

        centralizar( "1-6 ou clique: empilhar (PUSH)    Backspace ou clique no topo: remover (POP)", 320, 14, WHITE );
        centralizar( "Z: desfazer    Enter/Espaço: entregar    Esc: pausa", 345, 14, WHITE );
        centralizar( "Bolinha verde ao lado da camada = igual ao pedido; vermelha = diferente.", 370, 14, WHITE );

        if ( ( (int) ( getTime() * 2 ) ) % 2 == 0 ) {
            centralizar( "Pressione ENTER ou ESPAÇO para começar", 440, 24, GOLD );
        }
    }

    private void desenharPausa() {
        fillRectangle( 0, 0, LARGURA, ALTURA, ColorUtils.fade( BLACK, 0.6 ) );
        centralizar( "PAUSADO", 220, 60, GOLD );
        centralizar( "Pressione ESC para continuar", 310, 22, WHITE );
    }

    private void desenharGameOver() {
        fillRectangle( 0, 0, LARGURA, ALTURA, ColorUtils.fade( BLACK, 0.8 ) );
        centralizar( "GAME OVER", 120, 60, VERMELHO );
        centralizar( "Pontuação final: " + jogo.getPontos(), 210, 28, WHITE );
        if ( jogo.isNovoRecorde() ) {
            centralizar( "NOVO RECORDE!", 252, 22, GOLD );
        } else {
            centralizar( "Recorde: " + jogo.getRecorde(), 252, 20, CINZA_CLARO );
        }
        centralizar( "Nível " + jogo.getNivel() + "   -   Tempo " + formatarTempo( jogo.getTempoJogo() ), 300, 20, CINZA_CLARO );
        centralizar( "Atendidos: " + jogo.getAtendidos() + "   Erros: " + jogo.getErros()
                + "   Desistências: " + jogo.getDesistencias() + "   Maior combo: " + jogo.getMaiorCombo(),
                340, 18, CINZA_CLARO );
        centralizar( "Pressione R para jogar de novo", 410, 22, GOLD );
    }

    // posição Y de cada camada da bandeja (0 = base)
    private double yBloco( int camada ) {
        return BANDEJA_BASE_Y - ( camada + 1 ) * ( BLOCO_A + 2 );
    }

    private void desenharBordaDestaque( double x, double y, double l, double a ) {
        drawRoundRectangle( x, y, l, a, 12, BLACK );
        drawRoundRectangle( x + 1, y + 1, l - 2, a - 2, 12, BLACK );
    }

    private void centralizar( String texto, double y, int tamanho, Color cor ) {
        drawText( texto, LARGURA / 2 - measureText( texto, tamanho ) / 2, y, tamanho, cor );
    }

    private boolean dentro( int mx, int my, double x, double y, double l, double a ) {
        return mx >= x && mx <= x + l && my >= y && my <= y + a;
    }

    private String formatarTempo( double segundos ) {
        int total = (int) segundos;
        return String.format( "%02d:%02d", total / 60, total % 60 );
    }

    public static void main( String[] args ) {
        new Main();
    }

}
