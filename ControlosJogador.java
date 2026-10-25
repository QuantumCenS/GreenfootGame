import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Conjunto de teclas que controla um agente. Isola a leitura do teclado da lógica do
 * personagem: o Zig e o Zag usam a mesma classe, só muda o objeto de controlos.
 *
 * Nomes de teclas Greenfoot: "a".."z", "left", "right", "up", "down", "space", "enter", "shift"...
 *
 * Classe auxiliar (não é um Actor), usada por composição em AgenteInvestigador e PainelHacking.
 * Conceitos de POO: encapsulamento (atributos privados e finais), overloading de construtores
 * (3 versões encadeadas), métodos de fábrica estáticos (wasd, setas).
 */
public class ControlosJogador
{
    /** Tecla para andar para a esquerda (ex.: "a"). */
    private final String teclaEsquerda;
    /** Tecla para andar para a direita (ex.: "d"). */
    private final String teclaDireita;
    /** Tecla de salto (ex.: "w"). */
    private final String teclaSalto;
    /** Tecla para agachar (ex.: "s"). */
    private final String teclaAgachar;
    /** Tecla de ação especial / lançar distração, ou null (ex.: "e"). */
    private final String teclaAcao;
    /** Tecla para sair do mini-jogo de hacking, ou null (ex.: "backspace"). */
    private final String teclaCancelar;

    /**
     * Controlos sem tecla de ação (overloading).
     *
     * @param esquerda Tecla para a esquerda (ex.: "a")
     * @param direita Tecla para a direita (ex.: "d")
     * @param salto Tecla de salto (ex.: "w")
     * @param agachar Tecla para agachar (ex.: "s")
     */
    public ControlosJogador(String esquerda, String direita, String salto, String agachar)
    {
        this(esquerda, direita, salto, agachar, null);
    }

    /**
     * Controlos com tecla de ação mas sem tecla para cancelar (overloading).
     *
     * @param esquerda Tecla para a esquerda (ex.: "left")
     * @param direita Tecla para a direita (ex.: "right")
     * @param salto Tecla de salto (ex.: "up")
     * @param agachar Tecla para agachar (ex.: "down")
     * @param acao Tecla de ação especial / lançar distração (ex.: "enter")
     */
    public ControlosJogador(String esquerda, String direita, String salto, String agachar, String acao)
    {
        this(esquerda, direita, salto, agachar, acao, null);
    }

    /**
     * Controlos completos.
     * @param esquerda Tecla para a esquerda (ex.: "a")
     * @param direita  Tecla para a direita (ex.: "d")
     * @param salto    Tecla de salto (ex.: "w")
     * @param agachar  Tecla para agachar (ex.: "s")
     * @param acao     ação especial / lançar distração (ex.: "e")
     * @param cancelar sair do mini-jogo de hacking (ex.: "q")
     */
    public ControlosJogador(String esquerda, String direita, String salto, String agachar, String acao, String cancelar)
    {
        this.teclaCancelar = cancelar;
        this.teclaEsquerda = esquerda;
        this.teclaDireita = direita;
        this.teclaSalto = salto;
        this.teclaAgachar = agachar;
        this.teclaAcao = acao;
    }

    /**
     * Cria o esquema do Zig: W A S D, ação em E, cancelar em Q.
     *
     * @return novo objeto de controlos com as teclas W A S D
     */
    public static ControlosJogador wasd()
    {
        return new ControlosJogador("a", "d", "w", "s", "e", "q");
    }

    /**
     * Cria o esquema do Zag: setas, ação em ENTER, cancelar em BACKSPACE.
     *
     * @return novo objeto de controlos com as setas
     */
    public static ControlosJogador setas()
    {
        return new ControlosJogador("left", "right", "up", "down", "enter", "backspace");
    }

    /**
     * Lê as teclas de movimento horizontal.
     *
     * @return -1 (esquerda), 0 (nenhuma ou ambas) ou 1 (direita)
     */
    public int getDirecaoHorizontal()
    {
        int direcao = 0;
        if (Greenfoot.isKeyDown(teclaDireita)) {
            direcao++;
        }
        if (Greenfoot.isKeyDown(teclaEsquerda)) {
            direcao--;
        }
        return direcao;
    }

    /**
     * Indica se a tecla de salto está premida.
     *
     * @return true enquanto a tecla estiver premida
     */
    public boolean saltoPremido()
    {
        return Greenfoot.isKeyDown(teclaSalto);
    }

    /**
     * Indica se a tecla de agachar está premida.
     *
     * @return true enquanto a tecla estiver premida
     */
    public boolean agacharPremido()
    {
        return Greenfoot.isKeyDown(teclaAgachar);
    }

    /**
     * Indica se a tecla de ação está premida.
     *
     * @return true enquanto a tecla estiver premida; false se não houver tecla de ação
     */
    public boolean acaoPremida()
    {
        return teclaAcao != null && Greenfoot.isKeyDown(teclaAcao);
    }

    /**
     * Indica se a tecla de cancelar está premida.
     *
     * @return true enquanto a tecla estiver premida; false se não houver tecla de cancelar
     */
    public boolean cancelarPremido()
    {
        return teclaCancelar != null && Greenfoot.isKeyDown(teclaCancelar);
    }

    /**
     * Converte o nome de uma tecla do Greenfoot num texto pronto a mostrar no ecrã.
     *
     * @param tecla Nome da tecla no Greenfoot, ou null (ex.: "left" ou "enter")
     * @return texto a mostrar (ex.: "SETA ESQ.", "ENTER"), ou "-" se a tecla for null
     */
    public static String nomeTecla(String tecla)
    {
        if (tecla == null) {
            return "-";
        }
        switch (tecla) {
            case "left":  return "SETA ESQ.";
            case "right": return "SETA DIR.";
            case "up":    return "SETA CIMA";
            case "down":  return "SETA BAIXO";
            default:      return tecla.toUpperCase();
        }
    }

    // Getters. Usados por AgenteInvestigador e PainelHacking (getTeclaAcao, getTeclaCancelar);
    // o ecrã de instruções tem as teclas escritas diretamente e não os usa.

    /**
     * Devolve a tecla de cancelar.
     *
     * @return nome da tecla (ex.: "backspace"), ou null
     */
    public String getTeclaCancelar() { return teclaCancelar; }

    /**
     * Devolve a tecla para a esquerda.
     *
     * @return nome da tecla (ex.: "a")
     */
    public String getTeclaEsquerda() { return teclaEsquerda; }

    /**
     * Devolve a tecla para a direita.
     *
     * @return nome da tecla (ex.: "d")
     */
    public String getTeclaDireita()  { return teclaDireita; }

    /**
     * Devolve a tecla de salto.
     *
     * @return nome da tecla (ex.: "w")
     */
    public String getTeclaSalto()    { return teclaSalto; }

    /**
     * Devolve a tecla de agachar.
     *
     * @return nome da tecla (ex.: "s")
     */
    public String getTeclaAgachar()  { return teclaAgachar; }

    /**
     * Devolve a tecla de ação.
     *
     * @return nome da tecla (ex.: "e"), ou null
     */
    public String getTeclaAcao()     { return teclaAcao; }
}
