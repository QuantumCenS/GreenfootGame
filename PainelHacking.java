import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Claude (Anthropic) aqui.
 * Âmbito: classe inteira (lógica do painel e código de desenho/gráficos)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Ecrã do mini-jogo de hacking (estilo terminal de fósforo verde).
 * O Zag usa as SUAS teclas: setas movem o cursor, ENTER roda a peça, BACKSPACE sai.
 * O jogo continua a correr por trás — os inimigos não esperam! — por isso o Zig
 * deve vigiar enquanto o Zag trabalha. Se o tempo acabar, o terminal dispara o alarme.
 *
 * Hierarquia: PainelHacking estende ElementoEcra, que estende Actor (fica fixo no ecrã durante o scroll).
 * Conceitos de POO: herança (usa fonte() e escreverCentrado() de ElementoEcra), enum público interno
 * (Resultado), composição (PuzzleCircuito com a lógica; o painel só desenha e lê o teclado),
 * overriding de act().
 *
 * Nota: desenvolvido com apoio de IA (Claude, Anthropic). (Foi um desafio desenvolver este puzzle).
 */
public class PainelHacking extends ElementoEcra
{
    /** Resultado de uma tentativa de hacking. */
    public enum Resultado {
        /** O circuito foi ligado a tempo. */
        SUCESSO,
        /** O tempo acabou. */
        FALHOU,
        /** O jogador saiu, ou o Zag foi atingido ou caiu. */
        CANCELADO
    }

    /** Lado de cada peça da grelha, em píxeis (46). */
    private static final int PECA = 46;
    /** Altura do cabeçalho do painel, em píxeis (64). */
    private static final int CABECALHO = 64;
    /** Altura do rodapé com as teclas, em píxeis (46). */
    private static final int RODAPE = 46;
    /** Largura do painel, em píxeis (440). */
    private static final int LARGURA = 440;
    /** Ciclos durante os quais o resultado fica visível antes de fechar (50). */
    private static final int FRAMES_RESULTADO = 50;
    /** Cor de fundo, verde muito escuro quase opaco. */
    private static final Color FUNDO = new Color(6, 20, 12, 235);
    /** Verde vivo do texto e das peças ligadas. */
    private static final Color VERDE = new Color(110, 255, 160);
    /** Verde escuro das linhas de varrimento e contornos. */
    private static final Color VERDE_ESCURO = new Color(40, 100, 64);
    /** Amarelo do cursor. */
    private static final Color AMARELO = new Color(255, 220, 90);

    /** Terminal que abriu o painel (é avisado no fim). */
    private final TerminalHacking terminal;
    /** Agente que está a hackear (as suas teclas controlam o cursor). */
    private final AgenteInvestigador hacker;
    /** Lógica do puzzle. */
    private final PuzzleCircuito puzzle;
    /** Tempo total, em ciclos (mínimo 60; ex.: 2100 = 35 s). */
    private final int tempoTotal;
    /** Tempo restante, em ciclos. */
    private int tempoRestante;
    /** Coluna do cursor. */
    private int cursorColuna;
    /** Linha do cursor. */
    private int cursorLinha;
    /** Estado das 6 teclas no ciclo anterior; começa a true para ignorar teclas já premidas. */
    private final boolean[] teclasAntes = { true, true, true, true, true, true };
    /** Resultado do mini-jogo, ou null enquanto decorre. */
    private Resultado resultado;
    /** Ciclos restantes a mostrar o resultado. */
    private int framesResultado;

    /**
     * Cria o painel com o cursor na linha de entrada do sinal e desenha-o.
     *
     * @param terminal Terminal que abriu o painel (ex.: this, em TerminalHacking.iniciarHacking)
     * @param hacker Agente que vai hackear (ex.: o Zag)
     * @param puzzle Puzzle a resolver (ex.: new PuzzleCircuito(5, 4))
     * @param tempoFrames Tempo limite em ciclos, mínimo 60 (ex.: 35 * 60 = 2100)
     */
    public PainelHacking(TerminalHacking terminal, AgenteInvestigador hacker, PuzzleCircuito puzzle, int tempoFrames)
    {
        this.terminal = terminal;
        this.hacker = hacker;
        this.puzzle = puzzle;
        this.tempoTotal = Math.max(60, tempoFrames);
        this.tempoRestante = this.tempoTotal;
        this.cursorLinha = puzzle.getLinhaEntrada();
        redesenhar();
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine Actor.act): depois de terminado, mostra o resultado
     * e fecha; caso contrário cancela se o Zag deixou de estar ocupado, desconta o tempo, lê o teclado
     * e verifica se o puzzle foi resolvido ou o tempo acabou.
     */
    public void act()
    {
        if (resultado != null) {
            framesResultado--;
            if (framesResultado <= 0) {
                terminal.concluirHacking(resultado);
                remover();
            }
            return;
        }
        if (hacker.getWorld() == null || !hacker.isOcupado()) {
            terminar(Resultado.CANCELADO);          // o Zag foi atingido ou caiu
            return;
        }
        tempoRestante--;
        lerTeclado();
        if (resultado == null && puzzle.isResolvido()) {
            terminar(Resultado.SUCESSO);
        } else if (resultado == null && tempoRestante <= 0) {
            terminar(Resultado.FALHOU);
        }
        redesenhar();
    }

    /** Lê as teclas do próprio agente; reage só no momento em que são premidas. */
    private void lerTeclado()
    {
        ControlosJogador c = hacker.getControlos();
        int horizontal = c.getDirecaoHorizontal();
        boolean[] agora = { horizontal < 0, horizontal > 0, c.saltoPremido(), c.agacharPremido(),
                            c.acaoPremida(), c.cancelarPremido() };
        boolean[] novo = new boolean[agora.length];
        for (int i = 0; i < agora.length; i++) {
            novo[i] = agora[i] && !teclasAntes[i];
            teclasAntes[i] = agora[i];
        }
        if (novo[0]) { cursorColuna = Math.max(0, cursorColuna - 1); }
        if (novo[1]) { cursorColuna = Math.min(puzzle.getColunas() - 1, cursorColuna + 1); }
        if (novo[2]) { cursorLinha = Math.max(0, cursorLinha - 1); }
        if (novo[3]) { cursorLinha = Math.min(puzzle.getLinhas() - 1, cursorLinha + 1); }
        if (novo[4]) { puzzle.rodar(cursorColuna, cursorLinha); Audio.tocar("tecla.wav"); }
        if (novo[5]) { terminar(Resultado.CANCELADO); }
    }

    /**
     * Termina o mini-jogo com o resultado indicado (cancelar fecha no ciclo seguinte; os outros
     * mostram o resultado durante 50 ciclos).
     *
     * @param r Resultado (ex.: Resultado.SUCESSO)
     */
    private void terminar(Resultado r)
    {
        resultado = r;
        framesResultado = (r == Resultado.CANCELADO) ? 1 : FRAMES_RESULTADO;
        redesenhar();
    }

    // ------------------------------------------------------------------ desenho

    /** Redesenha o painel completo: fundo CRT, cabeçalho, tempo, grelha, cursor, teclas e resultado. */
    private void redesenhar()
    {
        int altura = CABECALHO + puzzle.getLinhas() * PECA + RODAPE;
        GreenfootImage img = new GreenfootImage(LARGURA, altura);
        img.setColor(FUNDO);
        img.fillRect(0, 0, LARGURA, altura);
        img.setColor(VERDE_ESCURO);
        for (int y = 0; y < altura; y += 3) {          // linhas de varrimento CRT
            img.drawLine(0, y, LARGURA, y);
        }
        img.setColor(VERDE);
        img.drawRect(0, 0, LARGURA - 1, altura - 1);
        img.drawRect(2, 2, LARGURA - 5, altura - 5);

        img.setFont(fonte(15, true));
        img.drawString("ATLANTIDA SEC-OS v2.3 :: INTRUSAO", 14, 22);
        img.setFont(fonte(13, false));
        img.drawString("Ligue o sinal da ENTRADA a SAIDA", 14, 40);
        desenharTempo(img);

        int grelhaX = (LARGURA - puzzle.getColunas() * PECA) / 2;
        int grelhaY = CABECALHO;
        for (int c = 0; c < puzzle.getColunas(); c++) {
            for (int l = 0; l < puzzle.getLinhas(); l++) {
                desenharPeca(img, grelhaX + c * PECA, grelhaY + l * PECA, puzzle.getPeca(c, l), puzzle.isEnergizada(c, l));
            }
        }
        desenharTerminais(img, grelhaX, grelhaY);
        img.setColor(AMARELO);
        int cx = grelhaX + cursorColuna * PECA;
        int cy = grelhaY + cursorLinha * PECA;
        img.drawRect(cx + 1, cy + 1, PECA - 3, PECA - 3);
        img.drawRect(cx + 2, cy + 2, PECA - 5, PECA - 5);

        img.setColor(VERDE);
        img.setFont(fonte(12, false));
        ControlosJogador c = hacker.getControlos();
        img.drawString("Setas: mover   " + ControlosJogador.nomeTecla(c.getTeclaAcao()) + ": rodar   "
                       + ControlosJogador.nomeTecla(c.getTeclaCancelar()) + ": sair", 14, altura - 18);
        if (resultado != null && resultado != Resultado.CANCELADO) {
            desenharResultado(img);
        }
        setImage(img);
    }

    /**
     * Desenha a barra de tempo no canto superior direito (vermelha no último quarto).
     *
     * @param img Imagem do painel
     */
    private void desenharTempo(GreenfootImage img)
    {
        int largura = 150;
        int x = LARGURA - largura - 14;
        img.setColor(VERDE_ESCURO);
        img.fillRect(x, 30, largura, 10);
        boolean urgente = tempoRestante < tempoTotal / 4;
        img.setColor(urgente ? new Color(255, 80, 80) : VERDE);
        img.fillRect(x, 30, largura * Math.max(0, tempoRestante) / tempoTotal, 10);
        img.drawRect(x, 30, largura, 10);
    }

    /**
     * Desenha uma peça da grelha: um quadrado com os braços correspondentes aos lados ligados.
     *
     * @param img Imagem do painel
     * @param x Coordenada x do canto superior esquerdo da peça, em píxeis (ex.: 105)
     * @param y Coordenada y do canto superior esquerdo da peça, em píxeis (ex.: 64)
     * @param mascara Lados ligados como máscara de bits N=1, E=2, S=4, O=8 (ex.: 5 para uma reta vertical)
     * @param ligada true se a peça recebe o sinal (desenhada em verde vivo e mais grossa)
     */
    private void desenharPeca(GreenfootImage img, int x, int y, int mascara, boolean ligada)
    {
        img.setColor(new Color(10, 34, 20));
        img.fillRect(x + 2, y + 2, PECA - 4, PECA - 4);
        img.setColor(VERDE_ESCURO);
        img.drawRect(x + 2, y + 2, PECA - 5, PECA - 5);
        Color cor = ligada ? VERDE : new Color(70, 140, 100);
        int meio = PECA / 2;
        int grossura = ligada ? 8 : 6;
        int g2 = grossura / 2;
        img.setColor(cor);
        img.fillRect(x + meio - g2, y + meio - g2, grossura, grossura);
        if ((mascara & PuzzleCircuito.NORTE) != 0) { img.fillRect(x + meio - g2, y + 2, grossura, meio - 2); }
        if ((mascara & PuzzleCircuito.SUL) != 0)   { img.fillRect(x + meio - g2, y + meio, grossura, meio - 2); }
        if ((mascara & PuzzleCircuito.OESTE) != 0) { img.fillRect(x + 2, y + meio - g2, meio - 2, grossura); }
        if ((mascara & PuzzleCircuito.ESTE) != 0)  { img.fillRect(x + meio, y + meio - g2, meio - 2, grossura); }
    }

    /**
     * Desenha a seta de entrada (à esquerda) e a lâmpada de saída (à direita, verde quando resolvido).
     *
     * @param img Imagem do painel
     * @param grelhaX Coordenada x do canto esquerdo da grelha, em píxeis (ex.: 105 numa grelha 5x4)
     * @param grelhaY Coordenada y do topo da grelha, em píxeis (ex.: 64)
     */
    private void desenharTerminais(GreenfootImage img, int grelhaX, int grelhaY)
    {
        int meio = PECA / 2;
        int ye = grelhaY + puzzle.getLinhaEntrada() * PECA + meio;
        int ys = grelhaY + puzzle.getLinhaSaida() * PECA + meio;
        img.setColor(VERDE);
        img.fillRect(grelhaX - 26, ye - 4, 24, 8);
        img.fillPolygon(new int[] { grelhaX - 30, grelhaX - 20, grelhaX - 30 }, new int[] { ye - 9, ye, ye + 9 }, 3);
        img.setColor(puzzle.isResolvido() ? VERDE : new Color(255, 90, 90));
        img.fillRect(grelhaX + puzzle.getColunas() * PECA + 2, ys - 4, 22, 8);
        img.fillOval(grelhaX + puzzle.getColunas() * PECA + 22, ys - 8, 16, 16);
    }

    /**
     * Desenha a faixa central "ACESSO CONCEDIDO" ou "ACESSO NEGADO".
     *
     * @param img Imagem do painel
     */
    private void desenharResultado(GreenfootImage img)
    {
        boolean ok = resultado == Resultado.SUCESSO;
        int y = img.getHeight() / 2 - 26;
        img.setColor(new Color(0, 0, 0, 200));
        img.fillRect(20, y, LARGURA - 40, 52);
        img.setColor(ok ? VERDE : new Color(255, 80, 80));
        img.drawRect(20, y, LARGURA - 41, 51);
        img.setFont(fonte(24, true));
        escreverCentrado(img, ok ? "ACESSO CONCEDIDO" : "ACESSO NEGADO", y + 35, 24);
    }

    /**
     * Devolve o resultado do mini-jogo.
     *
     * @return resultado, ou null enquanto o mini-jogo decorre
     */
    public Resultado getResultado()
    {
        return resultado;
    }

    /**
     * Devolve o puzzle deste painel.
     *
     * @return puzzle em jogo
     */
    public PuzzleCircuito getPuzzle()
    {
        return puzzle;
    }
}
