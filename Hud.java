import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (método desenhar)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Barra de informação no topo do ecrã: sanidade partilhada (corações), notas de lore
 * encontradas no nível, pontuação, ficheiro classificado e aviso de ALARME.
 * Em modo developer mostra também uma linha amarela com as teclas de teste.
 *
 * Hierarquia: Hud estende ElementoEcra, que estende Actor (fica fixo no ecrã durante o scroll).
 * Conceitos de POO: herança (usa fonte() de ElementoEcra), overriding de act(), encapsulamento.
 */
public class Hud extends ElementoEcra
{
    /** Largura da barra, em píxeis (1024, a largura do ecrã). */
    private static final int LARGURA = 1024;
    /** Altura da faixa escura, em píxeis (40). */
    private static final int ALTURA = 40;
    /** Altura total da imagem (inclui a linha do modo developer), em píxeis (60). */
    private static final int ALTURA_IMAGEM = 60;

    /** Ícone de coração cheio (sanidade disponível). */
    private final GreenfootImage coracaoCheio;
    /** Ícone de coração vazio (sanidade perdida). */
    private final GreenfootImage coracaoVazio;
    /** Ícone das notas de lore. */
    private final GreenfootImage iconeNota;
    /** Ícone do ficheiro classificado. */
    private final GreenfootImage iconeFicheiro;
    /** Ícone da sanidade. */
    private final GreenfootImage iconeSanidade;
    /** Resumo em texto do último estado desenhado (para só redesenhar quando muda). */
    private String ultimoEstado = "";
    /** Contador de ciclos para o piscar do alarme. */
    private int fase;

    /** Carrega os ícones e cria a imagem transparente da barra. */
    public Hud()
    {
        coracaoCheio = new GreenfootImage("coracao_cheio.png");
        coracaoVazio = new GreenfootImage("coracao_vazio.png");
        iconeNota = new GreenfootImage("icone_nota.png");
        iconeFicheiro = new GreenfootImage("icone_ficheiro.png");
        iconeSanidade = new GreenfootImage("icone_sanidade.png");
        setImage(new GreenfootImage(LARGURA, ALTURA_IMAGEM));
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine Actor.act): só num Cenario; junta o estado
     * relevante num texto e redesenha apenas se for diferente do último.
     */
    public void act()
    {
        if (!(getWorld() instanceof Cenario)) {
            return;
        }
        fase++;
        Cenario cenario = (Cenario) getWorld();
        EstadoMissao e = cenario.getEstado();
        boolean piscar = cenario.isAlarmeAtivo() && (fase / 15) % 2 == 0;
        String estado = e.getVidaSanidade() + "|" + e.getPontuacao() + "|" + e.getNotasRecolhidas(cenario.getNumeroNivel())
                        + "|" + e.isFicheiroObtido() + "|" + cenario.isAlarmeAtivo() + "|" + piscar + "|" + Audio.isSomAtivo();
        if (!estado.equals(ultimoEstado)) {
            ultimoEstado = estado;
            desenhar(cenario, e, piscar);
        }
    }
    //utilizado AI auxiliar para desenhar (caixas simples)
    /**
     * Desenha a barra: corações, notas, pontos, ficheiro, missão, teclas, linha do modo developer
     * e caixa de alarme.
     *
     * @param cenario Nível atual (ex.: um Cenario2)
     * @param e Estado da missão a mostrar (ex.: cenario.getEstado())
     * @param piscar true na fase "acesa" do alarme (alterna a cada 15 ciclos)
     */
    private void desenhar(Cenario cenario, EstadoMissao e, boolean piscar)
    {
        GreenfootImage img = getImage();
        img.clear();
        img.setColor(new Color(8, 6, 16, 170));
        img.fillRect(0, 0, LARGURA, ALTURA);
        img.setColor(new Color(90, 80, 120, 200));
        img.drawLine(0, ALTURA - 1, LARGURA, ALTURA - 1);

        img.drawImage(iconeSanidade, 10, 8);
        for (int i = 0; i < EstadoMissao.SANIDADE_MAXIMA; i++) {
            img.drawImage(i < e.getVidaSanidade() ? coracaoCheio : coracaoVazio, 44 + i * 26, 8);
        }
        img.setFont(fonte(17, true));
        img.setColor(new Color(240, 230, 200));
        img.drawImage(iconeNota, 196, 10);
        img.drawString(e.getNotasRecolhidas(cenario.getNumeroNivel()) + "/"
                       + ArquivoLore.getTotalNotasNivel(cenario.getNumeroNivel()), 222, 27);
        img.drawString("PONTOS " + String.format("%05d", e.getPontuacao()), 290, 27);

        if (e.isFicheiroObtido()) {
            img.drawImage(iconeFicheiro, 480, 10);
            img.setColor(new Color(255, 110, 110));
            img.drawString("CLASSIFICADO OBTIDO", 510, 27);
        } else {
            GreenfootImage apagado = new GreenfootImage(iconeFicheiro);
            apagado.setTransparency(90);
            img.drawImage(apagado, 480, 10);
            img.setColor(new Color(160, 150, 170));
            img.drawString("Ficheiro em falta", 510, 27);
        }
        img.setFont(fonte(14, true));
        img.setColor(new Color(200, 180, 255));
        img.drawString("MISSÃO " + cenario.getNumeroNivel() + "/" + Niveis.TOTAL, 716, 18);
        img.setFont(fonte(12, false));
        img.setColor(new Color(170, 170, 190));
        img.drawString("P pausa  M som" + (Audio.isSomAtivo() ? "" : " (off)"), 716, 33);
        if (e.isModoDeveloper()) {
            img.setColor(new Color(255, 200, 80));
            img.fillRect(0, ALTURA - 3, LARGURA, 3);
            img.setFont(fonte(13, true));
            img.drawString("DEV: vidas infinitas | 9 concluir nível | 8 obter ficheiro", 10, ALTURA + 14);
        }
        if (cenario.isAlarmeAtivo()) {
            img.setColor(piscar ? new Color(255, 50, 50) : new Color(120, 20, 20));
            img.fillRect(LARGURA - 150, 6, 140, 28);
            img.setColor(Color.WHITE);
            img.drawString("! ALARME !", LARGURA - 132, 27);
        }
    }
}
