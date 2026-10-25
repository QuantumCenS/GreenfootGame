import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Menu principal: continuar a partir da gravação, novo jogo, escolher nível
 * (só os desbloqueados; todos em modo developer), instruções, modo developer e som.
 * Navega-se com o teclado (setas/WASD + ENTER) ou com o rato.
 *
 * Aparece depois do EcraSplash. Daqui passa-se ao EcraHistoria (briefing da missão escolhida)
 * ou ao EcraInstrucoes.
 *
 * Hierarquia: MenuInicial estende EcraMenu, que estende World.
 * Conceitos de POO: herança, overriding de aoEscolher (abstrato) e do hook aoMudarLateral,
 * encapsulamento (constantes privadas para os índices dos botões).
 */
public class MenuInicial extends EcraMenu
{
    /** Índice do botão "Continuar". */
    private static final int CONTINUAR = 0;
    /** Índice do botão "Novo jogo". */
    private static final int NOVO = 1;
    /** Índice do botão "Escolher missão". */
    private static final int ESCOLHER = 2;
    /** Índice do botão "Instruções". */
    private static final int INSTRUCOES = 3;
    /** Índice do botão "Modo developer". */
    private static final int DEVELOPER = 4;
    /** Índice do botão "Som". */
    private static final int SOM = 5;
    /** Índice do botão "Apagar progresso". */
    private static final int APAGAR = 6;

    /** Botão "Continuar" (texto e estado dependem da gravação). */
    private final Botao botaoContinuar;
    /** Botão "Escolher missão" (mostra o nível escolhido). */
    private final Botao botaoEscolher;
    /** Botão do modo developer (mostra LIGADO/DESLIGADO). */
    private final Botao botaoDeveloper;
    /** Botão do som (mostra LIGADO/DESLIGADO). */
    private final Botao botaoSom;
    /** Botão "Apagar progresso" (pede confirmação). */
    private final Botao botaoApagar;
    /** Nível escolhido no botão "Escolher missão" (de 1 ao máximo permitido). */
    private int nivelEscolhido = 1;
    /** true depois do primeiro clique em "Apagar progresso" (o segundo apaga). */
    private boolean confirmarApagar;

    /** Cria o menu: aplica a preferência de som, toca a música, desenha o título e os 7 botões. */
    public MenuInicial()
    {
        Audio.setSomAtivo(SistemaSave.isSomAtivo());
        Audio.tocarMusica("musica_menu.wav");
        GreenfootImage fundo = fundoEscurecido(new GreenfootImage("fundo_menu.png"), 110);
        fundo.setColor(new Color(200, 180, 255));
        fundo.setFont(new Font("Monospaced", true, false, 46));
        centrar(fundo, "AGENTES ZIG & ZAG", 82, 46);
        fundo.setFont(new Font("Monospaced", true, false, 20));
        fundo.setColor(new Color(130, 255, 200));
        centrar(fundo, "Operação Sombras de Zanarkin", 116, 20);
        fundo.setFont(new Font("Monospaced", false, false, 14));
        fundo.setColor(new Color(200, 200, 220));
        centrar(fundo, "Recorde: " + SistemaSave.getMelhorPontuacao() + " pontos", 140, 14);
        centrar(fundo, "Setas/W S + ENTER, ou o rato", 556, 14);
        setBackground(fundo);

        botaoContinuar = adicionarBotao(new Botao(""), 180);
        adicionarBotao(new Botao("Novo jogo"), 228);
        botaoEscolher = adicionarBotao(new Botao(""), 276);
        adicionarBotao(new Botao("Instruções"), 324);
        botaoDeveloper = adicionarBotao(new Botao(""), 372);
        botaoSom = adicionarBotao(new Botao(""), 420);
        botaoApagar = adicionarBotao(new Botao("Apagar progresso"), 468);
        atualizarTextos();
    }

    /** Atualiza os textos e o estado ativo dos botões conforme a gravação e as preferências. */
    private void atualizarTextos()
    {
        boolean temSave = SistemaSave.existeProgresso();
        botaoContinuar.setTexto(temSave ? "Continuar — missão " + SistemaSave.getNivelGuardado() : "Continuar (sem progresso)");
        botaoContinuar.setAtivo(temSave);
        nivelEscolhido = Math.max(1, Math.min(getNivelMaximoPermitido(), nivelEscolhido));
        botaoEscolher.setTexto("Escolher missão:  < " + nivelEscolhido + " >");
        botaoDeveloper.setTexto("Modo developer: " + (SistemaSave.isModoDeveloper() ? "LIGADO (vidas infinitas)" : "DESLIGADO"));
        botaoSom.setTexto("Som: " + (Audio.isSomAtivo() ? "LIGADO" : "DESLIGADO"));
        botaoApagar.setTexto(confirmarApagar ? "Tem a certeza? ENTER para apagar" : "Apagar progresso");
        botaoApagar.setAtivo(temSave || SistemaSave.getMelhorPontuacao() > 0 || SistemaSave.getNivelMaximo() > 1);
        reverSelecao();
    }

    /**
     * Devolve o nível mais alto que se pode escolher. Em modo developer todos os níveis estão disponíveis.
     *
     * @return 5 em modo developer; caso contrário o nível máximo desbloqueado na gravação
     */
    private int getNivelMaximoPermitido()
    {
        return SistemaSave.isModoDeveloper() ? Niveis.TOTAL : SistemaSave.getNivelMaximo();
    }

    /**
     * Implementa EcraMenu.aoEscolher: executa a ação do botão escolhido e atualiza os textos.
     *
     * @param indice Botão escolhido (ex.: 1, "Novo jogo", que abre o briefing da missão 1)
     */
    @Override
    protected void aoEscolher(int indice)
    {
        if (indice != APAGAR) {
            confirmarApagar = false;
        }
        switch (indice) {
            case CONTINUAR:
                Greenfoot.setWorld(new EcraHistoria(SistemaSave.getNivelGuardado(), SistemaSave.carregarEstado()));
                break;
            case NOVO:
                Greenfoot.setWorld(new EcraHistoria(1, novoEstado()));
                break;
            case ESCOLHER:
                EstadoMissao e = SistemaSave.existeProgresso() ? SistemaSave.carregarEstado() : novoEstado();
                e.setModoDeveloper(SistemaSave.isModoDeveloper());
                Greenfoot.setWorld(new EcraHistoria(nivelEscolhido, e));
                break;
            case INSTRUCOES:
                Greenfoot.setWorld(new EcraInstrucoes());
                break;
            case DEVELOPER:
                SistemaSave.setModoDeveloper(!SistemaSave.isModoDeveloper());
                break;
            case SOM:
                Audio.setSomAtivo(!Audio.isSomAtivo());
                SistemaSave.setSomAtivo(Audio.isSomAtivo());
                break;
            default:
                if (confirmarApagar) {
                    SistemaSave.apagarProgresso();
                    confirmarApagar = false;
                } else {
                    confirmarApagar = true;
                }
                break;
        }
        atualizarTextos();
    }

    /**
     * Cria um estado de missão novo com o modo developer gravado nas preferências.
     *
     * @return novo estado de missão
     */
    private EstadoMissao novoEstado()
    {
        EstadoMissao e = new EstadoMissao();
        e.setModoDeveloper(SistemaSave.isModoDeveloper());
        return e;
    }

    /**
     * Redefine o hook de EcraMenu: no botão "Escolher missão", esquerda/direita muda o nível
     * (dá a volta entre 1 e o máximo permitido).
     *
     * @param indice Botão selecionado (ex.: 2)
     * @param direcao -1 esquerda, 1 direita (ex.: 1)
     */
    @Override
    protected void aoMudarLateral(int indice, int direcao)
    {
        if (indice == ESCOLHER) {
            int maximo = getNivelMaximoPermitido();
            nivelEscolhido = ((nivelEscolhido - 1 + direcao + maximo) % maximo) + 1;
            Audio.tocar("menu_mover.wav");
            atualizarTextos();
        }
    }

    /**
     * Devolve o nível escolhido no botão "Escolher missão".
     *
     * @return nível escolhido (ex.: 1)
     */
    public int getNivelEscolhido()
    {
        return nivelEscolhido;
    }
}
