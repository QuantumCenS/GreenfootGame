import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Briefing antes de cada missão: o que aconteceu até agora e o objetivo do nível.
 * Antes da missão 1 mostra também o prólogo da história.
 * O botão "Começar a missão" cria o nível com Niveis.criar(); ESC/BACKSPACE volta ao MenuInicial.
 *
 * Hierarquia: EcraHistoria estende EcraMenu, que estende World.
 * Conceitos de POO: herança (usa fundoEscurecido, centrar, paragrafo e adicionarBotao de EcraMenu),
 * overriding de aoEscolher (abstrato) e do hook aoVoltar.
 */
public class EcraHistoria extends EcraMenu
{
    /** Número da missão a começar (1 a 5). */
    private final int nivel;
    /** Estado da missão com que o nível vai começar. */
    private final EstadoMissao estado;

    /**
     * Cria o briefing da missão indicada.
     *
     * @param nivel Número da missão, de 1 a 5 (ex.: 1 num jogo novo)
     * @param estado Estado da missão a passar ao nível (ex.: SistemaSave.carregarEstado() ao continuar)
     */
    public EcraHistoria(int nivel, EstadoMissao estado)
    {
        this.nivel = nivel;
        this.estado = estado;
        Audio.tocarMusica("musica_menu.wav");
        GreenfootImage fundo = fundoEscurecido(new GreenfootImage("fundo_menu.png"), 175);
        fundo.setColor(new Color(130, 255, 200));
        fundo.setFont(new Font("Monospaced", true, false, 16));
        centrar(fundo, "DIVISÃO ÓMEGA — PROCESSO 7-B — DOCUMENTO RESERVADO", 50, 16);
        fundo.setColor(new Color(200, 180, 255));
        fundo.setFont(new Font("Monospaced", true, false, 32));
        centrar(fundo, "MISSÃO " + nivel + ": " + Niveis.getNome(nivel).toUpperCase(), 100, 32);
        fundo.setColor(new Color(235, 228, 210));
        fundo.setFont(new Font("Monospaced", false, false, 17));
        int y = 150;
        if (nivel == 1) {
            y = paragrafo(fundo, ArquivoLore.TEXTO_INTRODUCAO, y, 17, 84, 23) + 18;
        }
        fundo.setColor(new Color(255, 220, 140));
        y = paragrafo(fundo, ArquivoLore.getBriefing(nivel), y, 17, 84, 23);
        fundo.setColor(new Color(170, 170, 190));
        fundo.setFont(new Font("Monospaced", false, false, 14));
        centrar(fundo, "Objetivo: o ficheiro CLASSIFICADO e os DOIS agentes na extração. Notas: "
                + ArquivoLore.getTotalNotasNivel(nivel) + " (opcionais).", Math.max(y + 20, 420), 14);
        setBackground(fundo);
        adicionarBotao(new Botao("Começar a missão"), 500);
    }

    /**
     * Implementa EcraMenu.aoEscolher: começa o nível.
     *
     * @param indice Botão escolhido (ex.: 0, o único botão)
     */
    @Override
    protected void aoEscolher(int indice)
    {
        Greenfoot.setWorld(Niveis.criar(nivel, estado));
    }

    /** Redefine o hook de EcraMenu: ESC/BACKSPACE volta ao MenuInicial. */
    @Override
    protected void aoVoltar()
    {
        Greenfoot.setWorld(new MenuInicial());
    }

    /**
     * Devolve o número da missão deste briefing.
     *
     * @return número da missão (ex.: 1)
     */
    public int getNivel()
    {
        return nivel;
    }
}
