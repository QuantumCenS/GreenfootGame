import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Nível 2 — O Templo Subterrâneo (catacumbas sob a capital)
 *
 *  ENTRADA: estão em cima do abismo, cultista a guardar o terminal T3,
 *     sino ancestral serve para atrair guardas, câmara + barreira de runas.
 *  CÂMARA DAS ESTÁTUAS: estátua pesada que os DOIS empurram até à parede
 *     da galeria para servir de levantamento; invocador está numa plataforma a disparar orbes.
 *  GALERIA E SALA DO SELO: cria abismal na galeria, cofre de pedra que só abre com as DUAS placas premidas ao mesmo tempo
 *     (o item final está lá dentro) e a Rocha Rúnica que só sobe com os dois em cima,
 *     até à extração junto à fenda
 *
 * Passagem ao nível seguinte: com o ficheiro "D2", os dois agentes entram na ZonaExtracao "SAÍDA";
 * o Cenario abre o EcraFim, que leva ao briefing do nível 3.
 *
 * Hierarquia: Cenario2 estende Cenario, que estende World.
 * Conceitos de POO: herança, overriding dos métodos abstratos de Cenario, overloading de construtores.
 */
public class Cenario2 extends Cenario
{
    /** Largura total do nível, em píxeis (3072). */
    public static final int LARGURA_NIVEL = 3072;
    /** Coordenada y do topo do chão principal, em píxeis (496). */
    private static final int CHAO = 496;
    /** Coordenada y do topo da galeria, em píxeis (256). */
    private static final int TOPO_GALERIA = 256;
    /** Coordenada y da base da zona de extração e destino da Rocha Rúnica, em píxeis (140). */
    private static final int TOPO_EXTRACAO = 140;

    /** Nível 2 com um estado de missão novo (para correr diretamente no Greenfoot). */
    public Cenario2()
    {
        this(new EstadoMissao());
    }

    /**
     * Constrói o nível 2 com o estado de missão indicado.
     *
     * @param estado Estado da missão vindo do nível 1 (ex.: o estado passado pelo EcraHistoria)
     */
    public Cenario2(EstadoMissao estado)
    {
        super("panorama_nivel2.png", LARGURA_NIVEL, estado);
        construirEntrada();
        construirCamaraDasEstatuas();
        construirSalaDoSelo();
        adicionarAgentes(50, CHAO, 110, CHAO);
    }

    /** A. Entrada: laje móvel, guarda do terminal, sino, câmara e barreira. */
    private void construirEntrada()
    {
        adicionarChao(0, 420, CHAO, TipoPlataforma.CATACUMBA);
        adicionarPlataforma(120, 376, 144, TipoPlataforma.LAJE);
        addObject(new NotaLore("N2-1"), 192, 344);
        adicionarSobre(new Dica("Acertem num sino com um frasco ou amuleto: os cultistas vão todos ver."), 330, CHAO);

        adicionarPlataforma(new PlataformaMovel(96, TipoPlataforma.LAJE, 180, 0, 240), 420, CHAO);
        adicionarChao(696, 1204, CHAO, TipoPlataforma.CATACUMBA);
        adicionarSombra(700, CHAO - 110, 110, 110);
        adicionarSobre(new CultistaPatrulheiro(240), 790, CHAO);

        SinoAncestral sino = new SinoAncestral();
        addObject(sino, 1090, 350);
        CamaraVigilancia camara = new CamaraVigilancia(70, 130, 400);
        addObject(camara, 1150, 90);
        BarreiraSeguranca barreira = new BarreiraSeguranca(CHAO + 100);
        adicionarPlataforma(barreira, 1240, -100);
        adicionarSobre(new TerminalHacking(6, 4, 40, camara, barreira), 735, CHAO);
    }

    /** B. Estátua pesada, varanda do invocador. */
    private void construirCamaraDasEstatuas()
    {
        adicionarSombra(1272, CHAO - 110, 100, 110);
        addObject(new NotaLore("N2-2"), 1300, CHAO - 30);
        adicionarSobre(new Dica("A estátua é pesada demais para um só. Empurrem-na até à parede."), 1330, CHAO);
        CaixaPesada estatua = new CaixaPesada("estatua.png");
        adicionarPlataforma(estatua, 1380, CHAO - estatua.getAltura());

        adicionarPlataforma(1500, 300, 160, TipoPlataforma.LAJE);
        adicionarSobre(new CultistaInvocador(80, 35), 1520, 300);
        addObject(new NotaLore("N2-5"), 1640, 268);

        adicionarPlataforma(1900, TOPO_GALERIA, 500, ALTURA - TOPO_GALERIA, TipoPlataforma.CATACUMBA);
    }

    /** C. Galeria da cria, selo das duas placas, Rocha Rúnica e extração. */
    private void construirSalaDoSelo()
    {
        adicionarSobre(new CriaturaAbissal(210), 2120, TOPO_GALERIA);
        adicionarPlataforma(2050, 150, 96, TipoPlataforma.LAJE);
        adicionarPlataforma(2200, 150, 96, TipoPlataforma.LAJE);
        adicionarPlataforma(2350, 150, 96, TipoPlataforma.LAJE);
        addObject(new NotaLore("N2-3"), 2248, 118);

        adicionarChao(2400, 672, CHAO, TipoPlataforma.CATACUMBA);
        PlacaPressao placaChao = new PlacaPressao();
        adicionarElemento(placaChao, 2440, CHAO - placaChao.getAltura());
        adicionarPlataforma(2560, 380, 96, TipoPlataforma.LAJE);
        PlacaPressao placaAlta = new PlacaPressao();
        adicionarElemento(placaAlta, 2560, 380 - placaAlta.getAltura());
        adicionarSobre(new Dica("O selo só abre com as DUAS placas premidas ao mesmo tempo."), 2620, CHAO);

        PortaoSelado selo = new PortaoSelado(156, placaChao, placaAlta);
        adicionarPlataforma(selo, 2700, CHAO - 156);
        adicionarPlataforma(2700, 300, 180, 40, TipoPlataforma.CATACUMBA);     // teto da câmara do selo
        adicionarPlataforma(2860, 340, 20, CHAO - 340, TipoPlataforma.CATACUMBA);
        addObject(new FicheiroSecreto("D2"), 2795, CHAO - 34);
        addObject(new NotaLore("N2-4"), 2790, 268);
        adicionarSobre(new CultistaInvocador(150), 2520, CHAO);

        adicionarPlataforma(new RochaRunica(96, TOPO_EXTRACAO - (CHAO - 30)), 2900, CHAO - 30);
        adicionarSobre(new Dica("A Rocha Rúnica só se ergue com OS DOIS em cima."), 3030, CHAO);
        adicionarElemento(new ZonaExtracao(112, TOPO_EXTRACAO, "SAÍDA"), 2892, 0);
    }

    /**
     * Implementa Cenario.getNomeNivel.
     *
     * @return "Nível 2 — O Templo Subterrâneo"
     */
    @Override
    public String getNomeNivel()
    {
        return "Nível 2 — O Templo Subterrâneo";
    }

    /**
     * Implementa Cenario.getNumeroNivel.
     *
     * @return 2
     */
    @Override
    public int getNumeroNivel()
    {
        return 2;
    }
}
