import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (método desenhar)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Zona que marca o fim do nível. Os DOIS agentes têm de estar lá dentro, em pé,
 * com o ficheiro classificado já recolhido. Fica vermelha (trancada) até ao ficheiro
 * ser obtido e verde depois. Com os dois lá dentro durante 45 ciclos, conclui o nível.
 *
 * Hierarquia: ZonaExtracao estende ElementoInterativo, que estende Actor.
 * Conceitos de POO: herança, overriding de act(), overloading de construtores (2 versões).
 */
public class ZonaExtracao extends ElementoInterativo
{
    /** Ciclos seguidos com os dois agentes dentro até concluir o nível (45). */
    private static final int FRAMES_PARA_SAIR = 45;

    /** Largura da zona, em píxeis (ex.: 112). */
    private final int largura;
    /** Altura da zona, em píxeis (ex.: 150). */
    private final int altura;
    /** Texto escrito no topo da zona (ex.: "ALÇAPÃO"). */
    private final String rotulo;
    /** Ciclos seguidos com os dois agentes dentro. */
    private int framesDentro;
    /** Contador de ciclos para a animação. */
    private int fase;
    /** true se já avisou que falta o ficheiro (evita repetir a mensagem). */
    private boolean avisouFicheiro;
    /** true se o ficheiro classificado já foi obtido (zona verde). */
    private boolean pronta;

    /**
     * Zona com o rótulo "EXTRAÇÃO" (overloading).
     *
     * @param largura Largura em píxeis (ex.: 112)
     * @param altura Altura em píxeis (ex.: 150)
     */
    public ZonaExtracao(int largura, int altura)
    {
        this(largura, altura, "EXTRAÇÃO");
    }

    /**
     * Zona com rótulo à escolha.
     *
     * @param largura Largura em píxeis (ex.: 112, no Cenario1)
     * @param altura Altura em píxeis (ex.: 150)
     * @param rotulo Texto escrito no topo da zona (ex.: "ALÇAPÃO")
     */
    public ZonaExtracao(int largura, int altura, String rotulo)
    {
        this.largura = largura;
        this.altura = altura;
        this.rotulo = rotulo;
        desenhar();
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): atualiza a cor e a animação;
     * se os dois agentes estiverem dentro e em pé, avisa se faltar o ficheiro ou, ao fim de 45 ciclos,
     * conclui o nível.
     */
    public void act()
    {
        Cenario cenario = (getWorld() instanceof Cenario) ? (Cenario) getWorld() : null;
        fase++;
        pronta = cenario != null && cenario.getEstado().isFicheiroObtido();
        if (fase % 4 == 0) {
            desenhar();
        }
        if (cenario == null) {
            return;
        }
        boolean todosDentro = !cenario.getAgentes().isEmpty();
        for (AgenteInvestigador a : cenario.getAgentes()) {
            todosDentro &= contem(a) && a.isNoChao();
        }
        if (!todosDentro) {
            framesDentro = 0;
            avisouFicheiro = false;
            return;
        }
        if (!pronta) {
            if (!avisouFicheiro) {
                cenario.mostrarMensagem("Falta o ficheiro CLASSIFICADO!", new Color(255, 90, 90), 120);
                avisouFicheiro = true;
            }
            return;
        }
        framesDentro++;
        if (framesDentro >= FRAMES_PARA_SAIR) {
            cenario.concluirNivel();
        }
    }

    /**
     * Verifica se o centro do agente está dentro da zona.
     *
     * @param a Agente a testar (ex.: o Zag)
     * @return true se o ponto (x, centro da hitbox) estiver dentro da zona
     */
    public boolean contem(AgenteInvestigador a)
    {
        int cy = a.getCentroY();
        return a.getX() >= getEsquerda() && a.getX() <= getDireita() && cy >= getTopo() && cy <= getBase();
    }
    //desenhado com AI
    /** Desenha a zona: degradê vermelho ou verde, partículas a subir, linhas laterais e o rótulo. */
    private void desenhar()
    {
        GreenfootImage img = new GreenfootImage(largura, altura);
        Color base = pronta ? new Color(90, 255, 150) : new Color(255, 70, 70);
        for (int y = 0; y < altura; y += 2) {
            int alfa = (int) (8 + 60.0 * y / altura);
            img.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), alfa));
            img.fillRect(0, y, largura, 2);
        }
        img.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 150));
        for (int i = 0; i < 4; i++) {         // partículas a subir
            int px = (i * 37 + fase) % Math.max(1, largura - 4);
            int py = altura - ((fase * 2 + i * 53) % altura);
            img.fillRect(px, py, 3, 3);
        }
        img.drawLine(0, 0, 0, altura - 1);
        img.drawLine(largura - 1, 0, largura - 1, altura - 1);
        img.setFont(new Font("Monospaced", true, false, 14));
        img.setColor(new Color(255, 255, 255, 220));
        int larguraTexto = rotulo.length() * 9;
        img.drawString(rotulo, (largura - larguraTexto) / 2, 18);
        setImage(img);
    }

    /**
     * Indica se a zona está pronta (ficheiro obtido).
     *
     * @return true se o ficheiro classificado do nível já foi recolhido
     */
    public boolean isPronta()
    {
        return pronta;
    }
}
