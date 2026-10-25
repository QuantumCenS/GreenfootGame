import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Nível 4 — O Observatório do Pico Negro (3 ecrãs de largura).
 *
 *  ESTRADA DA MONTANHA: degraus de rocha nevada e uma ponte de gelo
 *     quebradiço sobre o precipício (as plataformas frágeis desfazem-se — passem um de cada vez);
 *     cultista de patrulha.
 *  PÁTIO: terminal, câmara num poste, antena com nota, vigia de costas
 *     para a barreira laser.
 *  INTERIOR: selo de duas placas (uma no chão, outra numa grade) que guarda
 *     a sala dos cálculos com o ficheiro; invocador; elevador oscilante para uma nota;
 *     extração pela cúpula, no telhado da sala.
 *
 * Passagem ao nível seguinte: com o ficheiro "D4", os dois agentes entram na ZonaExtracao
 * "CÚPULA"; o Cenario abre o EcraFim, que leva ao briefing do nível 5.
 *
 * Hierarquia: Cenario4 estende Cenario, que estende World.
 * Conceitos de POO: herança, overriding dos métodos abstratos de Cenario, overloading de construtores.
 */
public class Cenario4 extends Cenario
{
    /** Largura total do nível, em píxeis (3072). */
    public static final int LARGURA_NIVEL = 3072;
    /** Coordenada y do topo do chão principal, em píxeis (520). */
    private static final int CHAO = 520;
    /** Coordenada y do patamar da estrada da montanha, em píxeis (440). */
    private static final int PATAMAR = 440;
    /** Coordenada y do teto da sala dos cálculos, em píxeis (260). */
    private static final int TETO_SALA = 260;

    /** Nível 4 com um estado de missão novo (para correr diretamente no Greenfoot). */
    public Cenario4()
    {
        this(new EstadoMissao());
    }

    /**
     * Constrói o nível 4 com o estado de missão indicado.
     *
     * @param estado Estado da missão vindo do nível 3 (ex.: o estado passado pelo EcraHistoria)
     */
    public Cenario4(EstadoMissao estado)
    {
        super("panorama_nivel4.png", LARGURA_NIVEL, estado);
        construirEstrada();
        construirPatio();
        construirInterior();
        adicionarAgentes(50, CHAO, 110, CHAO);
    }

    /** A. Estrada da montanha: degraus nevados, ponte de gelo frágil e cultista de patrulha. */
    private void construirEstrada()
    {
        adicionarChao(0, 420, CHAO, TipoPlataforma.NEVE);
        adicionarPlataforma(180, 400, 96, 24, TipoPlataforma.NEVE);
        addObject(new NotaLore("N4-1"), 228, 370);
        adicionarSobre(new Dica("Gelo rachado: desfaz-se pouco depois de lhe tocarem. Não parem em cima!"), 330, CHAO);
        adicionarChao(420, 300, PATAMAR, TipoPlataforma.NEVE);
        adicionarPlataforma(new PlataformaFragil(64), 762, PATAMAR);
        adicionarPlataforma(new PlataformaFragil(64), 862, PATAMAR);
        adicionarChao(960, 340, PATAMAR, TipoPlataforma.NEVE);
        adicionarSombra(970, PATAMAR - 110, 100, 110);
        adicionarSobre(new CultistaPatrulheiro(200), 1050, PATAMAR);
    }

    /** B. Pátio: terminal, câmara num poste, barreira laser, antena e vigia. */
    private void construirPatio()
    {
        adicionarChao(1300, 832, CHAO, TipoPlataforma.METAL);
        CamaraVigilancia camara = new CamaraVigilancia(60, 120, 420, CHAO - 140);
        addObject(camara, 1660, 140);
        BarreiraSeguranca barreira = new BarreiraSeguranca(CHAO + 100);
        adicionarPlataforma(barreira, 2100, -100);
        adicionarSobre(new TerminalHacking(6, 5, 45, camara, barreira), 1370, CHAO);
        adicionarSobre(new Dica("Zag, o terminal. Zig, o vigia: só por trás, quando ele se virar."), 1440, CHAO);

        adicionarPlataforma(1480, CHAO - 96, 96, 96, TipoPlataforma.CAIXOTE);
        adicionarPlataforma(1580, 300, 120, TipoPlataforma.GRADE);
        addObject(new NotaLore("N4-2"), 1640, 270);
        adicionarSobre(new CultistaVigia(170, false), 1880, CHAO);
        addObject(new NotaLore("N4-3"), 2030, CHAO - 30);
    }

    /** C. Interior: selo de duas placas, invocador, elevador oscilante, ficheiro e extração pela cúpula. */
    private void construirInterior()
    {
        adicionarChao(2132, 940, CHAO, TipoPlataforma.METAL);
        PlacaPressao placaChao = new PlacaPressao();
        adicionarElemento(placaChao, 2200, CHAO - placaChao.getAltura());
        adicionarPlataforma(2330, 390, 120, TipoPlataforma.GRADE);
        PlacaPressao placaAlta = new PlacaPressao();
        adicionarElemento(placaAlta, 2342, 390 - placaAlta.getAltura());
        adicionarSobre(new Dica("Selo de dupla chave: uma placa para cada agente, ao mesmo tempo."), 2270, CHAO);

        adicionarPlataforma(new PlataformaFragil(96), 2440, TETO_SALA);
        addObject(new NotaLore("N4-4"), 2488, TETO_SALA - 30);
        adicionarSobre(new CultistaInvocador(140), 2420, CHAO);

        adicionarPlataforma(new PlataformaMovel(96, TipoPlataforma.GRADE, 0, -300, 320), 2560, CHAO - 18);
        adicionarPlataforma(2660, 160, 96, TipoPlataforma.GRADE);
        addObject(new NotaLore("N4-5"), 2708, 130);

        adicionarPlataforma(2720, 390, 110, TipoPlataforma.GRADE);
        PortaoSelado selo = new PortaoSelado(CHAO - 300, placaChao, placaAlta);
        adicionarPlataforma(selo, 2860, 300);
        adicionarPlataforma(2860, TETO_SALA, 212, 300 - TETO_SALA, TipoPlataforma.METAL);
        addObject(new FicheiroSecreto("D4"), 2970, CHAO - 34);
        adicionarElemento(new ZonaExtracao(180, TETO_SALA - 100, "CÚPULA"), 2880, 100);
    }

    /**
     * Implementa Cenario.getNomeNivel.
     *
     * @return "Nível 4 — O Observatório do Pico Negro"
     */
    @Override
    public String getNomeNivel()
    {
        return "Nível 4 — O Observatório do Pico Negro";
    }

    /**
     * Implementa Cenario.getNumeroNivel.
     *
     * @return 4
     */
    @Override
    public int getNumeroNivel()
    {
        return 4;
    }
}
