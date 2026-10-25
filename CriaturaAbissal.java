import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Cria da Fossa — aberração tentacular nascida dos iniciados que perderam os olhos.
 *  - É CEGA: não tem cone de visão, guia-se apenas pelo som.
 *  - Ouve passos de agentes a correr, aterragens, caixas a arrastar, frascos...
 *    e corre (rápida) para o local do som; depois fareja e volta à rota.
 *  - NÃO pode ser neutralizada (não implementa Neutralizavel): é preciso passar
 *    agachado em silêncio, por cima, ou atraí-la para longe com uma distração.
 *  - Um frasco em cheio deixa-a atordoada uns segundos.
 *
 * Hierarquia: Actor -> EntidadeAnimada -> InimigoCosmico -> CriaturaAbissal (3 níveis além de Actor).
 * Conceitos de POO: herança, overriding dos métodos abstratos de InimigoCosmico (polimorfismo:
 * reage ao som de forma diferente dos cultistas), overloading de construtores (3 versões).
 */
public class CriaturaAbissal extends InimigoCosmico
{
    /** Velocidade em patrulha e ao regressar, em píxeis por ciclo (1). */
    private static final int VELOCIDADE_RASTEJAR = 1;
    /** Velocidade a caçar um som, em píxeis por ciclo (3). */
    private static final int VELOCIDADE_CACAR = 3;
    /** Ciclos a farejar no local do som antes de regressar (120). */
    private static final int TEMPO_FAREJAR = 120;
    /** Largura da hitbox, em píxeis (78). */
    private static final int LARGURA_COLISAO = 78;
    /** Altura da hitbox, em píxeis (48). */
    private static final int ALTURA_COLISAO = 48;

    /** Animação a rastejar (patrulha e regresso). */
    private final Animacao animRastejar;
    /** Animação a caçar e a farejar. */
    private final Animacao animCacar;
    /** Imagem atordoada. */
    private final Animacao animAtordoada;

    /** Criatura com rota de 240 px (para criar pelo menu do Greenfoot). */
    public CriaturaAbissal()
    {
        this(240);
    }

    /**
     * Criatura com a velocidade de rastejar padrão (overloading).
     *
     * @param limite Comprimento da rota para a direita, em píxeis (ex.: 330, no Cenario1)
     */
    public CriaturaAbissal(int limite)
    {
        this(limite, VELOCIDADE_RASTEJAR);
    }

    /**
     * Assinatura original do grupo: (limite, velocidade). Carrega as animações da criatura.
     *
     * @param limite Comprimento da rota para a direita, em píxeis (ex.: 330)
     * @param velocidade Velocidade de patrulha em píxeis por ciclo (ex.: 1)
     */
    public CriaturaAbissal(int limite, int velocidade)
    {
        super(limite, velocidade);
        animRastejar = carregarAnimacao("criatura_rastejar", 4, 8);
        animCacar = carregarAnimacao("criatura_cacar", 2, 6);
        animAtordoada = carregarAnimacao("criatura_atordoado", 1, 1);
        setImage(animRastejar.getFrameAtual(true));
    }

    /**
     * Implementa o método abstrato de InimigoCosmico: patrulha, caça o som (ALERTA),
     * fareja no local (INVESTIGAR) e regressa à rota.
     */
    @Override
    protected void executarComportamento()
    {
        switch (getEstado()) {
            case PATRULHA:
                patrulhar(getVelocidadeHorizontal());
                break;
            case ALERTA:      // a caçar o som
                if (irPara(getPontoAlvoX(), VELOCIDADE_CACAR)) {
                    mudarEstado(EstadoInimigo.INVESTIGAR);
                }
                break;
            case INVESTIGAR:  // a farejar
                if (getTemporizador() % 30 == 0) {
                    setViradoParaDireita(!isViradoParaDireita());
                }
                if (getTemporizador() > TEMPO_FAREJAR) {
                    mudarEstado(EstadoInimigo.REGRESSAR);
                }
                break;
            case REGRESSAR:
                if (irPara(getXinicial(), VELOCIDADE_RASTEJAR)) {
                    mudarEstado(EstadoInimigo.PATRULHA);
                }
                break;
            default:
                mudarEstado(EstadoInimigo.REGRESSAR);
                break;
        }
    }

    /**
     * Overriding: qualquer som ao alcance (mesmo os subtis) faz a criatura caçar o local.
     *
     * @param x Coordenada x da origem do som (ex.: a posição de um agente a correr)
     * @param y Coordenada y da origem do som (ex.: a base do agente)
     * @param tipo Tipo de ruído (ex.: TipoRuido.PASSOS)
     */
    @Override
    public void reagirADistracao(int x, int y, TipoRuido tipo)
    {
        if (getEstado() != EstadoInimigo.ALERTA) {
            mostrarBalao("!!", new Color(150, 40, 170), 45);
            Audio.tocar("cria.wav");
        }
        setPontoAlvoX(x);
        mudarEstado(EstadoInimigo.ALERTA);
    }

    /** Implementa o método abstrato de InimigoCosmico: escolhe a animação conforme o estado. */
    @Override
    protected void atualizarAnimacao()
    {
        switch (getEstado()) {
            case ATORDOADO:
                mostrar(animAtordoada);
                break;
            case ALERTA:
            case INVESTIGAR:
                mostrar(animCacar);
                break;
            default:
                mostrar(animRastejar);
                break;
        }
    }

    /**
     * Redefine a largura da hitbox.
     *
     * @return 78 píxeis
     */
    @Override
    public int getLarguraColisao()
    {
        return LARGURA_COLISAO;
    }

    /**
     * Redefine a altura da hitbox.
     *
     * @return 48 píxeis
     */
    @Override
    public int getAlturaColisao()
    {
        return ALTURA_COLISAO;
    }
}
