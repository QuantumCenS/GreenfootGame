import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Nível 5 — O Coração da Fossa (nível final, 3 ecrãs de largura).
 *
 *  DESCIDA: cristais luminosos, ponte de rocha podre (frágil) sobre o abismo,
 *     uma cria na margem.
 *  ACAMPAMENTO DO CULTO: vigia a guardar o terminal, patrulheiro, sino para
 *     atrair os guardas, câmara e barreira de runas.
 *  LIMIAR: o Sumo-Sacerdote (mini-chefe: só cai depois de atordoado por um
 *     frasco), selo de duas placas que guarda o Relatório Ómega-Zero, e a Rocha Rúnica que
 *     leva os dois agentes ao Selo — o fim da história.
 *
 * Fim do jogo: com o ficheiro "D5", os dois agentes entram na ZonaExtracao "O SELO"; o Cenario abre
 * o EcraFim (NIVEL_CONCLUIDO) e, como não há nível seguinte, o botão "Ver o epílogo" leva ao
 * EcraFim de VITORIA.
 *
 * Hierarquia: Cenario5 estende Cenario, que estende World.
 * Conceitos de POO: herança, overriding dos métodos abstratos de Cenario, overloading de construtores.
 */
public class Cenario5 extends Cenario
{
    /** Largura total do nível, em píxeis (3072). */
    public static final int LARGURA_NIVEL = 3072;
    /** Coordenada y do topo do chão principal, em píxeis (520). */
    private static final int CHAO = 520;
    /** Coordenada y da margem do outro lado da ponte, em píxeis (470). */
    private static final int MARGEM = 470;
    /** Coordenada y do teto da sala do selo, em píxeis (260). */
    private static final int TETO_SALA = 260;
    /** Coordenada y da base da zona final e destino da Rocha Rúnica, em píxeis (140). */
    private static final int TOPO_SELO = 140;

    /** Nível 5 com um estado de missão novo (para correr diretamente no Greenfoot). */
    public Cenario5()
    {
        this(new EstadoMissao());
    }

    /**
     * Constrói o nível 5 com o estado de missão indicado.
     *
     * @param estado Estado da missão vindo do nível 4 (ex.: o estado passado pelo EcraHistoria)
     */
    public Cenario5(EstadoMissao estado)
    {
        super("panorama_nivel5.png", LARGURA_NIVEL, estado);
        construirDescida();
        construirAcampamento();
        construirLimiar();
        adicionarAgentes(50, CHAO, 110, CHAO);
    }

    /** A. Descida: cristais, ponte de rocha podre e uma cria na margem. */
    private void construirDescida()
    {
        adicionarChao(0, 360, CHAO, TipoPlataforma.ROCHA_ABISSAL);
        adicionarSobre(new Dica("A rocha podre desfaz-se. Não parem! E o Sumo-Sacerdote só cai depois de um frasco."), 200, CHAO);
        adicionarPlataforma(150, 420, 96, TipoPlataforma.CRISTAL);
        adicionarPlataforma(260, 330, 96, TipoPlataforma.CRISTAL);
        addObject(new NotaLore("N5-1"), 308, 300);

        adicionarPlataforma(new PlataformaFragil(72), 384, 500);
        adicionarPlataforma(new PlataformaFragil(72), 494, 480);
        adicionarPlataforma(new PlataformaFragil(72), 604, 500);
        adicionarChao(700, 450, MARGEM, TipoPlataforma.ROCHA_ABISSAL);
        adicionarSobre(new CriaturaAbissal(260), 820, MARGEM);
        adicionarSombra(1080, MARGEM - 110, 70, 110);
    }

    /** B. Acampamento do culto: vigia, patrulheiro, escada de cristais, sino, câmara, barreira e terminal. */
    private void construirAcampamento()
    {
        adicionarChao(1150, 962, CHAO, TipoPlataforma.ROCHA_ABISSAL);
        adicionarSombra(1160, CHAO - 110, 80, 110);
        adicionarSobre(new CultistaVigia(160, true), 1320, CHAO);
        adicionarSobre(new CultistaPatrulheiro(220), 1460, CHAO);
        adicionarPlataforma(1480, 420, 96, TipoPlataforma.CRISTAL);           // escada de cristais
        adicionarPlataforma(1590, 330, 96, TipoPlataforma.CRISTAL);
        adicionarPlataforma(1700, 240, 96, TipoPlataforma.CRISTAL);
        addObject(new NotaLore("N5-2"), 1748, 210);
        addObject(new SinoAncestral(), 1900, 350);

        CamaraVigilancia camara = new CamaraVigilancia(65, 125, 420);
        addObject(camara, 1860, 80);
        BarreiraSeguranca barreira = new BarreiraSeguranca(CHAO + 100);
        adicionarPlataforma(barreira, 2080, -100);
        adicionarSobre(new TerminalHacking(6, 5, 45, camara, barreira), 1215, CHAO);
        addObject(new NotaLore("N5-3"), 2000, CHAO - 30);
    }

    /** C. Limiar: Sumo-Sacerdote, selo de duas placas, Relatório Ómega-Zero, Rocha Rúnica e o Selo. */
    private void construirLimiar()
    {
        adicionarChao(2112, 960, CHAO, TipoPlataforma.ROCHA_ABISSAL);
        PlacaPressao placaChao = new PlacaPressao();
        adicionarElemento(placaChao, 2240, CHAO - placaChao.getAltura());
        adicionarPlataforma(2420, 390, 96, TipoPlataforma.CRISTAL);
        PlacaPressao placaAlta = new PlacaPressao();
        adicionarElemento(placaAlta, 2420, 390 - placaAlta.getAltura());
        adicionarSobre(new SumoSacerdote(180), 2340, CHAO);

        adicionarPlataforma(2560, 330, 80, TipoPlataforma.CRISTAL);
        addObject(new NotaLore("N5-5"), 2600, 300);
        PortaoSelado selo = new PortaoSelado(CHAO - 300, placaChao, placaAlta);
        adicionarPlataforma(selo, 2680, 300);
        adicionarPlataforma(2680, TETO_SALA, 200, 300 - TETO_SALA, TipoPlataforma.ROCHA_ABISSAL);
        adicionarPlataforma(2860, 300, 20, CHAO - 300, TipoPlataforma.ROCHA_ABISSAL);
        addObject(new FicheiroSecreto("D5"), 2786, CHAO - 34);
        addObject(new NotaLore("N5-4"), 2780, TETO_SALA - 30);

        adicionarPlataforma(new RochaRunica(96, TOPO_SELO - (CHAO - 30)), 2900, CHAO - 30);
        adicionarSobre(new Dica("O Selo: subam OS DOIS na Rocha Rúnica e pronunciem o Nome."), 3030, CHAO);
        adicionarElemento(new ZonaExtracao(112, TOPO_SELO, "O SELO"), 2892, 0);
    }

    /**
     * Implementa Cenario.getNomeNivel.
     *
     * @return "Nível 5 — O Coração da Fossa"
     */
    @Override
    public String getNomeNivel()
    {
        return "Nível 5 — O Coração da Fossa";
    }

    /**
     * Implementa Cenario.getNumeroNivel.
     *
     * @return 5
     */
    @Override
    public int getNumeroNivel()
    {
        return 5;
    }
}
