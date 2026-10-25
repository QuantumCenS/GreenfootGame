import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira (incluindo o desenho em caixa)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Instruções básicas (requisito do enunciado): controlos de cada agente, objetivo e regras.
 * Abre-se a partir do MenuInicial; o botão "Voltar" (ou ESC/BACKSPACE) regressa ao MenuInicial.
 *
 * Hierarquia: EcraInstrucoes estende EcraMenu, que estende World.
 * Conceitos de POO: herança, overriding de aoEscolher (abstrato) e do hook aoVoltar,
 * método privado estático auxiliar de desenho.
 */
public class EcraInstrucoes extends EcraMenu
{
    /** Cria o ecrã: título, uma caixa de controlos para cada agente, as regras e o botão "Voltar". */
    public EcraInstrucoes()
    {
        GreenfootImage fundo = fundoEscurecido(new GreenfootImage("fundo_menu.png"), 170);
        fundo.setColor(new Color(200, 180, 255));
        fundo.setFont(new Font("Monospaced", true, false, 34));
        centrar(fundo, "INSTRUÇÕES", 56, 34);
        caixa(fundo, 50, 80, "ZIG — assalto tático", new String[] {
            "A / D    mover", "W        saltar (salto duplo no ar)", "S        agachar: silencioso",
            "S + W    descer de madeira/grade", "E        neutralizar cultista POR TRÁS",
            "         ou lançar frasco de éter" }, new Color(230, 180, 110));
        caixa(fundo, 530, 80, "ZAG — ocultismo e tecnologia", new String[] {
            "SETAS    mover / saltar / agachar", "CIMA     manter a cair: planar", "ENTER    hackear terminal (puzzle)",
            "         ou lançar amuleto luminoso", "BACKSP.  sair do hacking", "" }, new Color(230, 110, 130));
        fundo.setColor(new Color(225, 220, 210));
        fundo.setFont(new Font("Monospaced", false, false, 14));
        String[] regras = {
            "OBJETIVO: obter o ficheiro CLASSIFICADO e levar os DOIS agentes à zona de EXTRAÇÃO.",
            "SANIDADE (vidas): 5 corações partilhados. Perde-se 1 ao tocar em inimigos, orbes,",
            "  quedas e alarmes de câmaras. A zero: insanidade (game over) e recomeça o nível.",
            "PONTOS: +100 por nota de lore (opcionais) e +25 por cultista neutralizado.",
            "FURTIVIDADE: agachados nas sombras ninguém vos vê. Correr faz barulho.",
            "COOPERAÇÃO: caixas pesadas, selos de duas placas e rochas rúnicas precisam dos dois.",
            "As crias são cegas: guiam-se pelo som. Frascos e sinos atraem inimigos.",
            "P ou ESC: pausa (guardar e sair).  M: som on/off.  O jogo grava sozinho em cada nível." };
        int y = 316;
        for (String r : regras) {
            fundo.drawString(r, 60, y);
            y += 21;
        }
        setBackground(fundo);
        adicionarBotao(new Botao("Voltar"), 520);
    }

    /**
     * Desenha uma caixa com título e linhas de texto (usada para os controlos de cada agente).
     *
     * @param img Imagem onde desenhar (ex.: o fundo do ecrã)
     * @param x Coordenada x do canto superior esquerdo, em píxeis (ex.: 50)
     * @param y Coordenada y do canto superior esquerdo, em píxeis (ex.: 80)
     * @param titulo Título da caixa (ex.: "ZIG — assalto tático")
     * @param linhas Linhas de texto, uma por controlo (ex.: "A / D    mover")
     * @param cor Cor da borda e do título (ex.: new Color(230, 180, 110))
     */
    private static void caixa(GreenfootImage img, int x, int y, String titulo, String[] linhas, Color cor)
    {
        img.setColor(new Color(10, 8, 20, 200));
        img.fillRect(x, y, 444, 196);
        img.setColor(cor);
        img.drawRect(x, y, 444, 196);
        img.setFont(new Font("Monospaced", true, false, 17));
        img.drawString(titulo, x + 16, y + 28);
        img.setColor(new Color(230, 225, 215));
        img.setFont(new Font("Monospaced", false, false, 15));
        for (int i = 0; i < linhas.length; i++) {
            img.drawString(linhas[i], x + 16, y + 58 + i * 23);
        }
    }

    /**
     * Implementa EcraMenu.aoEscolher: volta ao MenuInicial.
     *
     * @param indice Botão escolhido (ex.: 0, "Voltar")
     */
    @Override
    protected void aoEscolher(int indice)
    {
        Greenfoot.setWorld(new MenuInicial());
    }

    /** Redefine o hook de EcraMenu: ESC/BACKSPACE volta ao MenuInicial. */
    @Override
    protected void aoVoltar()
    {
        Greenfoot.setWorld(new MenuInicial());
    }
}
