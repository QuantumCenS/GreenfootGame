import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Menu de pausa (tecla P ou ESC durante o jogo): continuar, recomeçar o nível,
 * ligar/desligar o som, ou guardar e sair para o menu principal.
 * "Continuar" (ou ESC/BACKSPACE) volta ao mesmo objeto Cenario, que continua onde estava.
 *
 * Hierarquia: EcraPausa estende EcraMenu, que estende World.
 * Conceitos de POO: herança, overriding de aoEscolher (abstrato) e do hook aoVoltar,
 * encapsulamento (constantes privadas para os índices dos botões).
 */
public class EcraPausa extends EcraMenu
{
    /** Índice do botão "Continuar". */
    private static final int CONTINUAR = 0;
    /** Índice do botão "Recomeçar o nível". */
    private static final int RECOMECAR = 1;
    /** Índice do botão do som. */
    private static final int SOM = 2;
    /** Índice do botão "Guardar e sair para o menu". */
    private static final int SAIR = 3;

    /** Nível que foi pausado. */
    private final Cenario origem;
    /** Botão do som (o texto muda ao ligar/desligar). */
    private final Botao botaoSom;

    /**
     * Cria o menu de pausa por cima da imagem congelada do nível.
     *
     * @param origem Nível que foi pausado (ex.: this, em Cenario.lerTeclasGerais)
     */
    public EcraPausa(Cenario origem)
    {
        this.origem = origem;
        GreenfootImage fundo = fundoEscurecido(origem.capturarEcra(), 170);
        fundo.setColor(new Color(200, 180, 255));
        fundo.setFont(new Font("Monospaced", true, false, 40));
        centrar(fundo, "PAUSA", 150, 40);
        fundo.setFont(new Font("Monospaced", false, false, 16));
        fundo.setColor(new Color(220, 214, 200));
        centrar(fundo, origem.getNomeNivel(), 185, 16);
        setBackground(fundo);
        adicionarBotao(new Botao("Continuar"), 250);
        adicionarBotao(new Botao("Recomeçar o nível"), 300);
        botaoSom = adicionarBotao(new Botao(textoSom()), 350);
        adicionarBotao(new Botao("Guardar e sair para o menu"), 400);
    }

    /**
     * Devolve o texto do botão do som conforme o estado atual.
     *
     * @return "Som: LIGADO" ou "Som: DESLIGADO"
     */
    private static String textoSom()
    {
        return "Som: " + (Audio.isSomAtivo() ? "LIGADO" : "DESLIGADO");
    }

    /**
     * Implementa EcraMenu.aoEscolher: continuar, recomeçar com o estado de entrada do nível,
     * ligar/desligar o som, ou gravar o checkpoint e voltar ao MenuInicial.
     *
     * @param indice Botão escolhido (ex.: 1, "Recomeçar o nível")
     */
    @Override
    protected void aoEscolher(int indice)
    {
        switch (indice) {
            case CONTINUAR:
                Greenfoot.setWorld(origem);
                break;
            case RECOMECAR:
                Greenfoot.setWorld(origem.criarNovaTentativa());
                break;
            case SOM:
                Audio.setSomAtivo(!Audio.isSomAtivo());
                SistemaSave.setSomAtivo(Audio.isSomAtivo());
                botaoSom.setTexto(textoSom());
                break;
            default:
                SistemaSave.guardar(origem.getEstadoInicial(), origem.getNumeroNivel());
                Greenfoot.setWorld(new MenuInicial());
                break;
        }
    }

    /** Redefine o hook de EcraMenu: ESC/BACKSPACE volta ao jogo. */
    @Override
    protected void aoVoltar()
    {
        Greenfoot.setWorld(origem);
    }
}
