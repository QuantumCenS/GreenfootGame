import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Nível 3 — A Biblioteca Proibida (Universidade de Zanarkin, 3 ecrãs de largura).
 *
 *  ÁTRIO: vigia que se vira de tantos em tantos segundos — é preciso cronometrar
 *     a aproximação; estantes para escalar; uma estante alta separa as salas.
 *  SALA DE LEITURA: o carrinho de livros é pesado — os DOIS empurram-no para
 *     debaixo da varanda (o Zag não chega lá de outra forma); na varanda de cima um cultista
 *     guarda o terminal que desliga a câmara e a barreira.
 *  ARQUIVO 0-Ω: a cria-bibliotecária, um invocador numa estante e o
 *     monta-cargas que só sobe com os dois em cima, até à estante do ficheiro e à claraboia.
 *
 * Passagem ao nível seguinte: com o ficheiro "D3", os dois agentes entram na ZonaExtracao
 * "CLARABOIA"; o Cenario abre o EcraFim, que leva ao briefing do nível 4.
 *
 * Hierarquia: Cenario3 estende Cenario, que estende World.
 * Conceitos de POO: herança, overriding dos métodos abstratos de Cenario, overloading de construtores.
 */
public class Cenario3 extends Cenario
{
    /** Largura total do nível, em píxeis (3072). */
    public static final int LARGURA_NIVEL = 3072;
    /** Coordenada y do topo do chão principal, em píxeis (520). */
    private static final int CHAO = 520;
    /** Coordenada y do topo da última estante (ficheiro e extração), em píxeis (160). */
    private static final int TOPO_ESTANTE_FINAL = 160;

    /** Nível 3 com um estado de missão novo (para correr diretamente no Greenfoot). */
    public Cenario3()
    {
        this(new EstadoMissao());
    }

    /**
     * Constrói o nível 3 com o estado de missão indicado (o chão de soalho ocupa o nível inteiro).
     *
     * @param estado Estado da missão vindo do nível 2 (ex.: o estado passado pelo EcraHistoria)
     */
    public Cenario3(EstadoMissao estado)
    {
        super("panorama_nivel3.png", LARGURA_NIVEL, estado);
        adicionarChao(0, LARGURA_NIVEL, CHAO, TipoPlataforma.SOALHO);
        construirAtrio();
        construirSalaDeLeitura();
        construirArquivo();
        adicionarAgentes(50, CHAO, 110, CHAO);
    }

    /** A. Átrio: vigia, estantes para escalar e a estante alta que separa as salas. */
    private void construirAtrio()
    {
        adicionarSobre(new Dica("Os vigias não andam, mas viram-se. Esperem que olhem para o outro lado."), 180, CHAO);
        adicionarPlataforma(250, CHAO - 48, 96, 48, TipoPlataforma.ESTANTE);        // secretária
        adicionarPlataforma(360, 376, 96, CHAO - 376, TipoPlataforma.ESTANTE);
        addObject(new NotaLore("N3-1"), 408, 344);
        adicionarSombra(470, CHAO - 110, 110, 110);
        adicionarSobre(new CultistaVigia(200, false), 700, CHAO);

        adicionarPlataforma(820, CHAO - 96, 96, 96, TipoPlataforma.ESTANTE);
        adicionarPlataforma(900, 330, 200, TipoPlataforma.MADEIRA);
        addObject(new NotaLore("N3-2"), 1070, 300);
        // estante até ao teto: só se passa por baixo (impede planar diretamente para a sala seguinte)
        adicionarPlataforma(1150, -100, 48, 430, TipoPlataforma.ESTANTE);
    }

    /** B. Sala de leitura: carrinho pesado, varandas, cultista, câmara, barreira e terminal. */
    private void construirSalaDeLeitura()
    {
        adicionarSobre(new Dica("Carrinho pesado: empurrem-no OS DOIS para debaixo da varanda."), 1230, CHAO);
        CaixaPesada carrinho = new CaixaPesada("carrinho_livros.png");
        adicionarPlataforma(carrinho, 1260, CHAO - carrinho.getAltura());

        adicionarPlataforma(1330, 370, 220, TipoPlataforma.MADEIRA);
        adicionarPlataforma(1600, 250, 300, TipoPlataforma.MADEIRA);
        adicionarSobre(new CultistaPatrulheiro(190), 1620, 250);

        CamaraVigilancia camara = new CamaraVigilancia(60, 120, 420);
        addObject(camara, 1460, 60);
        BarreiraSeguranca barreira = new BarreiraSeguranca(CHAO + 100);
        adicionarPlataforma(barreira, 1950, -100);
        adicionarSobre(new TerminalHacking(6, 4, 40, camara, barreira), 1862, 250);
        addObject(new NotaLore("N3-3"), 1640, 220);
    }

    /** C. Arquivo 0-Ω: cria, invocador, monta-cargas, ficheiro e extração pela claraboia. */
    private void construirArquivo()
    {
        adicionarSobre(new Dica("O que guarda o arquivo é cego. Silêncio absoluto."), 2010, CHAO);
        adicionarSobre(new CriaturaAbissal(320), 2080, CHAO);
        addObject(new NotaLore("N3-4"), 2260, CHAO - 30);

        adicionarPlataforma(2480, CHAO - 96, 96, 96, TipoPlataforma.ESTANTE);
        adicionarPlataforma(2620, 328, 96, CHAO - 328, TipoPlataforma.ESTANTE);
        CultistaInvocador invocador = new CultistaInvocador(40, 25);
        adicionarSobre(invocador, 2640, 328);

        adicionarPlataforma(new RochaRunica(96, 0, TOPO_ESTANTE_FINAL - (CHAO - 30), "elevador_off.png", "elevador_on.png"), 2776, CHAO - 30);
        adicionarSobre(new Dica("Monta-cargas: só sobe com OS DOIS em cima."), 2740, CHAO);
        adicionarPlataforma(2872, TOPO_ESTANTE_FINAL, 200, 40, TipoPlataforma.ESTANTE);
        addObject(new FicheiroSecreto("D3"), 2950, TOPO_ESTANTE_FINAL - 32);
        addObject(new NotaLore("N3-5"), 3040, TOPO_ESTANTE_FINAL - 32);
        adicionarElemento(new ZonaExtracao(140, TOPO_ESTANTE_FINAL, "CLARABOIA"), 2900, 0);
    }

    /**
     * Implementa Cenario.getNomeNivel.
     *
     * @return "Nível 3 — A Biblioteca Proibida"
     */
    @Override
    public String getNomeNivel()
    {
        return "Nível 3 — A Biblioteca Proibida";
    }

    /**
     * Implementa Cenario.getNumeroNivel.
     *
     * @return 3
     */
    @Override
    public int getNumeroNivel()
    {
        return 3;
    }
}
