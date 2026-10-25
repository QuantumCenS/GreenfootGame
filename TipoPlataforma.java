/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Tipos de plataforma: textura (topo e corpo), se é sólida e a altura padrão.
 *  - Sólida: bloqueia por cima, pelos lados e por baixo (bate-se com a cabeça).
 *  - Atravessável: só se apoia por cima; atravessa-se a saltar por baixo
 *    e pode-se descer com agachar + saltar.
 *  - INVISIVEL: sem imagem; serve para pôr colisões por cima de um fundo já desenhado.
 *
 * Usado por Plataforma (e subclasses) e pelos métodos de construção de níveis do Cenario.
 * Conceitos de POO: enum com construtor e atributos finais (encapsulamento), getters.
 */
public enum TipoPlataforma
{
    /** Cais de madeira e pedra das docas (sólido, 48 px). */
    CAIS("cais_topo.png", "cais.png", true, 48),
    /** Bloco de pedra (sólido, 48 px). */
    PEDRA("pedra_topo.png", "pedra.png", true, 48),
    /** Caixote de carga (sólido, 48 px). */
    CAIXOTE("caixote.png", "caixote.png", true, 48),
    /** Pedra das catacumbas (sólida, 48 px). */
    CATACUMBA("catacumba_topo.png", "catacumba.png", true, 48),
    /** Tábuas de madeira (atravessáveis, 18 px). */
    MADEIRA("madeira.png", "madeira.png", false, 18),
    /** Laje de pedra fina (atravessável, 24 px). */
    LAJE("laje.png", "laje.png", false, 24),
    /** Pedra rúnica apagada, usada pela RochaRunica (atravessável, 30 px). */
    RUNICA("runica_off.png", "runica_off.png", false, 30),
    /** Chapa de metal (sólida, 48 px). */
    METAL("metal_topo.png", "metal.png", true, 48),
    /** Grade metálica de passadiço (atravessável, 18 px). */
    GRADE("grade.png", "grade.png", false, 18),
    /** Portão de metal de enrolar (sólido). */
    PORTAO_METAL("portao_metal.png", "portao_metal.png", true, 48),
    /** Selo de pedra (sólido). */
    PORTAO_PEDRA("portao_pedra.png", "portao_pedra.png", true, 48),
    /** Soalho da biblioteca (sólido, 48 px). */
    SOALHO("soalho_topo.png", "soalho.png", true, 48),
    /** Estante de livros (sólida, 48 px). */
    ESTANTE("estante_topo.png", "estante.png", true, 48),
    /** Rocha coberta de neve (sólida, 48 px). */
    NEVE("neve_topo.png", "neve.png", true, 48),
    /** Rocha da Fossa (sólida, 48 px). */
    ROCHA_ABISSAL("rocha_abissal_topo.png", "rocha_abissal.png", true, 48),
    /** Cristal luminoso (atravessável, 24 px). */
    CRISTAL("cristal.png", "cristal.png", false, 24),
    /** Sem imagem: apenas colisão (sólida, 16 px). */
    INVISIVEL(null, null, true, 16);

    /** Ficheiro da textura da primeira linha (topo), ou null se for invisível (ex.: "cais_topo.png"). */
    private final String texturaTopo;
    /** Ficheiro da textura repetida no resto do corpo, ou null se for invisível (ex.: "cais.png"). */
    private final String texturaCorpo;
    /** true se bloqueia por todos os lados; false se só se apoia por cima. */
    private final boolean solida;
    /** Altura usada quando não se indica outra, em píxeis (ex.: 48). */
    private final int alturaPadrao;

    /**
     * Cria um tipo de plataforma.
     *
     * @param texturaTopo Ficheiro da textura do topo, ou null para invisível (ex.: "cais_topo.png")
     * @param texturaCorpo Ficheiro da textura do corpo, ou null para invisível (ex.: "cais.png")
     * @param solida true se bloqueia por todos os lados (ex.: true em CAIS, false em MADEIRA)
     * @param alturaPadrao Altura padrão em píxeis (ex.: 48)
     */
    TipoPlataforma(String texturaTopo, String texturaCorpo, boolean solida, int alturaPadrao)
    {
        this.texturaTopo = texturaTopo;
        this.texturaCorpo = texturaCorpo;
        this.solida = solida;
        this.alturaPadrao = alturaPadrao;
    }

    /**
     * Devolve o ficheiro da textura do topo.
     *
     * @return nome do ficheiro (ex.: "cais_topo.png"), ou null em INVISIVEL
     */
    public String getTexturaTopo()  { return texturaTopo; }

    /**
     * Devolve o ficheiro da textura do corpo.
     *
     * @return nome do ficheiro (ex.: "cais.png"), ou null em INVISIVEL
     */
    public String getTexturaCorpo() { return texturaCorpo; }

    /**
     * Indica se a plataforma é sólida.
     *
     * @return true se bloqueia por todos os lados; false se é atravessável
     */
    public boolean isSolida()       { return solida; }

    /**
     * Devolve a altura padrão do tipo.
     *
     * @return altura em píxeis (ex.: 48 em CAIS, 18 em MADEIRA)
     */
    public int getAlturaPadrao()    { return alturaPadrao; }

    /**
     * Indica se o tipo tem imagem.
     *
     * @return true se tiver textura; false em INVISIVEL
     */
    public boolean isVisivel()      { return texturaTopo != null; }
}
