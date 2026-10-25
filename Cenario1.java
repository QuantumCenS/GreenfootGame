import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Nível 1 — Docas de Zanarkin e Conserveira Atlântida (3 ecrãs de largura).
 *
 *  A. DOCAS : tutorial (movimento, jangada móvel e o primeiro cultista)
 *  B. PÁTIO DE CARGA : tutorial parte 2 (terminal que desliga a câmara e a barreira laser,
 *     caixote pesado que os dois empurram até à placa de pressão do portão)
 *  C. INTERIOR DA FÁBRICA : cria cega no chão dos tanques, passadiços,
 *     terminal T2 que desliga a câmara do cofre e o abre. Dentro do cofre está o
 *     ficheiro CLASSIFICADO e a extração.
 *
 * Passagem ao nível seguinte: com o ficheiro "D1", os dois agentes entram na ZonaExtracao
 * "ALÇAPÃO"; o Cenario abre o EcraFim, que leva ao briefing do nível 2.
 *
 * Hierarquia: Cenario1 estende Cenario, que estende World.
 * Conceitos de POO: herança (usa os métodos de construção de níveis de Cenario), overriding dos
 * métodos abstratos (getNomeNivel, getNumeroNivel), overloading de construtores (2 versões).
 */
public class Cenario1 extends Cenario
{
    /** Largura total do nível, em píxeis (3072, três ecrãs). */
    public static final int LARGURA_NIVEL = 3072;
    /** Coordenada y do topo do chão principal, em píxeis (520). */
    private static final int CHAO = 520;

    /** Nível 1 com um estado de missão novo (para correr diretamente no Greenfoot). */
    public Cenario1()
    {
        this(new EstadoMissao());
    }

    /**
     * Constrói o nível 1 com o estado de missão indicado.
     *
     * @param estado Estado da missão (ex.: o estado criado pelo MenuInicial num jogo novo)
     */
    public Cenario1(EstadoMissao estado)
    {
        super("panorama_nivel1.png", LARGURA_NIVEL, estado);
        construirDocas();
        construirPatioDeCarga();
        construirFabrica();
        adicionarAgentes(50, CHAO, 110, CHAO);
    }

    /** A. Docas: movimento, jangada e o primeiro cultista. */
    private void construirDocas()
    {
        adicionarChao(0, 608, CHAO, TipoPlataforma.CAIS);
        adicionarSombra(40, CHAO - 110, 160, 110);
        adicionarSobre(new Dica("Zig: A/D mover, W saltar (2x no ar), S agachar, E ação. "
                                + "Zag: setas, CIMA a cair = planar, ENTER ação."), 185, CHAO);
        adicionarPlataforma(256, CHAO - 48, 96, TipoPlataforma.CAIXOTE);
        adicionarPlataforma(304, CHAO - 96, 48, TipoPlataforma.CAIXOTE);
        adicionarPlataforma(352, 360, 192, TipoPlataforma.MADEIRA);
        addObject(new NotaLore("N1-1"), 500, 328);

        adicionarPlataforma(new PlataformaMovel(96, TipoPlataforma.MADEIRA, 96, 0, 260), 608, CHAO);
        adicionarChao(800, 800, CHAO, TipoPlataforma.CAIS);
        adicionarSombra(820, CHAO - 110, 110, 110);
        adicionarSobre(new Dica("Agachados não fazem barulho. Zig: cola-te às costas de um cultista e carrega E."), 860, CHAO);
        adicionarSobre(new CultistaPatrulheiro(250), 940, CHAO);
    }

    /** B. Pátio de carga: hacking, barreira laser e o caixote pesado. */
    private void construirPatioDeCarga()
    {
        adicionarChao(1600, 672, CHAO, TipoPlataforma.METAL);
        adicionarSobre(new Dica("Zag: ENTER no terminal para hackear a câmara e a barreira. Zig, protege-o!"), 1130, CHAO);

        CamaraVigilancia camara = new CamaraVigilancia(60, 120, 420, CHAO - 150);
        addObject(camara, 1560, 150);
        BarreiraSeguranca barreira = new BarreiraSeguranca(CHAO + 100);
        adicionarPlataforma(barreira, 1700, -100);
        adicionarSobre(new TerminalHacking(5, 4, 35, camara, barreira), 1230, CHAO);

        adicionarPlataforma(1330, CHAO - 96, 96, 96, TipoPlataforma.CAIXOTE);
        adicionarPlataforma(1480, 300, 120, TipoPlataforma.MADEIRA);
        addObject(new NotaLore("N1-2"), 1545, 268);

        adicionarSobre(new CultistaPatrulheiro(110), 1750, CHAO);
        adicionarSobre(new Dica("Este caixote é pesado: empurrem-no OS DOIS até à placa."), 1850, CHAO);
        CaixaPesada caixa = new CaixaPesada("caixa_pesada.png");
        adicionarPlataforma(caixa, 1890, CHAO - caixa.getAltura());

        Portao portao = new Portao(220, TipoPlataforma.PORTAO_METAL);
        adicionarPlataforma(portao, 2240, CHAO - 220);
        adicionarPlataforma(2240, -100, 32, CHAO - 220 + 100, TipoPlataforma.METAL);   // parede por cima do portão
        PlacaPressao placa = new PlacaPressao(portao);
        adicionarElemento(placa, 2100, CHAO - placa.getAltura());
        adicionarPlataforma(2196, CHAO - 24, 32, 24, TipoPlataforma.METAL);   // batente: a caixa pára em cima da placa
    }

    /** C. Interior da fábrica: cria, passadiços, câmara do cofre, ficheiro e extração. */
    private void construirFabrica()
    {
        adicionarChao(2272, 800, CHAO, TipoPlataforma.METAL);
        adicionarSobre(new Dica("A cria é cega mas ouve tudo: andem agachados, por cima, ou distraiam-na."), 2300, CHAO);
        adicionarSobre(new CriaturaAbissal(330), 2380, CHAO);
        addObject(new NotaLore("N1-4"), 2470, CHAO - 30);

        adicionarPlataforma(2330, 390, 200, TipoPlataforma.GRADE);
        adicionarPlataforma(2580, 270, 220, TipoPlataforma.GRADE);
        addObject(new NotaLore("N1-3"), 2770, 238);

        CamaraVigilancia camaraCofre = new CamaraVigilancia(70, 110, 440);
        addObject(camaraCofre, 2850, 70);
        Cofre cofre = new Cofre();
        adicionarPlataforma(cofre, 2900, CHAO - cofre.getAltura());
        adicionarPlataforma(2900, 300, 172, CHAO - cofre.getAltura() - 300, TipoPlataforma.METAL);   // teto do cofre
        adicionarSobre(new TerminalHacking(5, 4, 35, camaraCofre, cofre), 2620, 270);
        adicionarSombra(2826, CHAO - 110, 72, 110);
        addObject(new NotaLore("N1-5"), 3030, 268);

        addObject(new FicheiroSecreto("D1"), 2985, CHAO - 34);
        adicionarElemento(new ZonaExtracao(112, 150, "ALÇAPÃO"), 2955, CHAO - 152);
    }

    /**
     * Implementa Cenario.getNomeNivel.
     *
     * @return "Nível 1 — Docas de Zanarkin"
     */
    @Override
    public String getNomeNivel()
    {
        return "Nível 1 — Docas de Zanarkin";
    }

    /**
     * Implementa Cenario.getNumeroNivel.
     *
     * @return 1
     */
    @Override
    public int getNumeroNivel()
    {
        return 1;
    }
}
