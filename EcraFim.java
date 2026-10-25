import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira, em especial o método desenhar (o próprio código já o indica)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Ecrã entre níveis e de fim de jogo:
 *  - NIVEL_CONCLUIDO: resumo (notas, pontos, inimigos) -> "Continuar" leva ao briefing seguinte;
 *  - INSANIDADE (game over: sanidade a zero) -> "Tentar outra vez" recomeça o nível;
 *  - VITORIA: epílogo (final em aberto) e pontuação final.
 * Todos têm "Menu principal".
 *
 * Hierarquia: EcraFim estende EcraMenu, que estende World.
 * Conceitos de POO: herança, enum público interno (Tipo), overriding de aoEscolher (abstrato),
 * o mesmo ecrã comporta-se de forma diferente conforme o tipo.
 */
public class EcraFim extends EcraMenu
{
    /** Tipo de ecrã de fim. */
    public enum Tipo {
        /** Nível concluído com sucesso. */
        NIVEL_CONCLUIDO,
        /** Sanidade a zero (game over do nível). */
        INSANIDADE,
        /** Fim do jogo depois do nível 5. */
        VITORIA
    }

    /** Tipo deste ecrã. */
    private final Tipo tipo;
    /** Nível que acabou de terminar. */
    private final Cenario origem;
    /** Estado da missão a mostrar e a levar para o ecrã seguinte. */
    private final EstadoMissao estado;

    /**
     * Cria o ecrã: desenha o fundo, toca o som certo e acrescenta os botões. Na VITORIA também
     * regista o fim do jogo no SistemaSave.
     *
     * @param tipo Tipo de ecrã (ex.: EcraFim.Tipo.NIVEL_CONCLUIDO)
     * @param origem Nível que terminou (ex.: this, em Cenario.concluirNivel)
     * @param estado Estado da missão (ex.: o estado atual; na INSANIDADE, uma cópia do estado inicial)
     */
    public EcraFim(Tipo tipo, Cenario origem, EstadoMissao estado)
    {
        this.tipo = tipo;
        this.origem = origem;
        this.estado = estado;
        desenhar();
        switch (tipo) {
            case INSANIDADE:
                Audio.pararMusica();
                Audio.tocar("insanidade.wav");
                adicionarBotao(new Botao("Tentar outra vez"), 470);
                break;
            case VITORIA:
                Audio.tocarMusica("musica_menu.wav");
                Audio.tocar("vitoria.wav");
                SistemaSave.registarFimDeJogo(estado);
                break;
            default:
                Audio.tocar("nivel.wav");
                adicionarBotao(new Botao(Niveis.existe(origem.getNumeroNivel() + 1) ? "Continuar a missão" : "Ver o epílogo"), 470);
                break;
        }
        adicionarBotao(new Botao("Menu principal"), 520);
    }
    //esta funcao foi feita com ajuda de AI, Gemini
    /**
     * Desenha o fundo: título e cor conforme o tipo; epílogo (VITORIA), texto de insanidade ou
     * resumo do nível; pontuação e recorde (exceto na INSANIDADE).
     */
    private void desenhar()
    {
        GreenfootImage fundo = fundoEscurecido(new GreenfootImage("fundo_menu.png"), 165);
        Color cor;
        String titulo;
        switch (tipo) {
            case INSANIDADE:
                cor = new Color(255, 80, 90);
                titulo = "INSANIDADE";
                break;
            case VITORIA:
                cor = new Color(150, 255, 190);
                titulo = "O SELO FECHOU-SE";
                break;
            default:
                cor = new Color(200, 180, 255);
                titulo = "MISSÃO " + origem.getNumeroNivel() + " CONCLUÍDA";
                break;
        }
        fundo.setColor(cor);
        fundo.setFont(new Font("Monospaced", true, false, 40));
        centrar(fundo, titulo, 80, 40);
        fundo.setColor(new Color(235, 228, 210));
        fundo.setFont(new Font("Monospaced", false, false, 16));
        int y = 120;
        if (tipo == Tipo.VITORIA) {
            y = paragrafo(fundo, ArquivoLore.TEXTO_EPILOGO, y, 16, 88, 21);
            fundo.setFont(new Font("Monospaced", true, false, 26));
            fundo.setColor(new Color(255, 120, 120));
            centrar(fundo, "FIM...?", y + 24, 26);
            y += 40;
        } else if (tipo == Tipo.INSANIDADE) {
            y = paragrafo(fundo, ArquivoLore.TEXTO_INSANIDADE, y + 20, 18, 70, 26);
        } else {
            fundo.setFont(new Font("Monospaced", false, false, 18));
            centrar(fundo, origem.getNomeNivel(), y + 10, 18);
            y += 50;
            int nivel = origem.getNumeroNivel();
            fundo.setFont(new Font("Monospaced", true, false, 20));
            centrar(fundo, "Notas encontradas: " + estado.getNotasRecolhidas(nivel) + " / "
                    + ArquivoLore.getTotalNotasNivel(nivel), y, 20);
            centrar(fundo, "Cultistas neutralizados (total): " + estado.getInimigosNeutralizados(), y + 32, 20);
            centrar(fundo, "Ficheiros classificados: " + estado.getNumeroDossiers() + " / " + Niveis.TOTAL, y + 64, 20);
            y += 96;
        }
        if (tipo != Tipo.INSANIDADE) {
            fundo.setFont(new Font("Monospaced", true, false, 22));
            fundo.setColor(new Color(255, 220, 120));
            centrar(fundo, "PONTUAÇÃO: " + estado.getPontuacao(), Math.min(y + 30, 440), 22);
            fundo.setFont(new Font("Monospaced", false, false, 14));
            fundo.setColor(new Color(200, 200, 220));
            centrar(fundo, "Recorde: " + Math.max(estado.getPontuacao(), SistemaSave.getMelhorPontuacao()), Math.min(y + 52, 446 + 16), 14);
        }
        setBackground(fundo);
    }

    /**
     * Implementa EcraMenu.aoEscolher. Com 2 botões, o primeiro recomeça o nível (INSANIDADE) ou
     * avança para o briefing seguinte / epílogo (NIVEL_CONCLUIDO); qualquer outro caso volta ao MenuInicial.
     *
     * @param indice Botão escolhido (ex.: 0, "Continuar a missão")
     */
    @Override
    protected void aoEscolher(int indice)
    {
        boolean primeiroBotao = indice == 0 && getNumeroBotoes() == 2;
        if (!primeiroBotao) {
            Greenfoot.setWorld(new MenuInicial());
            return;
        }
        if (tipo == Tipo.INSANIDADE) {
            Greenfoot.setWorld(origem.criarNovaTentativa());
        } else {
            int proximo = origem.getNumeroNivel() + 1;
            if (Niveis.existe(proximo)) {
                Greenfoot.setWorld(new EcraHistoria(proximo, estado));
            } else {
                Greenfoot.setWorld(new EcraFim(Tipo.VITORIA, origem, estado));
            }
        }
    }

    /**
     * Devolve o tipo deste ecrã.
     *
     * @return tipo (ex.: Tipo.VITORIA)
     */
    public Tipo getTipo()
    {
        return tipo;
    }
}
