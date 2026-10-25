import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Agente Zig — assalto tático. Controlos: W A S D, ação E.
 *  - Salto duplo (um salto extra no ar).
 *  - Especialidade: NEUTRALIZAR cultistas por trás (golpe não letal, sem armas).
 *  - Distração: lança FRASCOS DE ÉTER (partem-se com estrondo; atordoam quem atingem).
 *
 * Hierarquia: Actor -> EntidadeAnimada -> AgenteInvestigador -> Zig
 * (Zig estende AgenteInvestigador, que estende EntidadeAnimada, que estende Actor: 3 níveis além de Actor).
 * Conceitos de POO: herança, overriding de todos os métodos abstratos de AgenteInvestigador,
 * overloading de construtores (3 versões encadeadas) e polimorfismo (usa inimigos através da
 * interface Neutralizavel).
 */
public class Zig extends AgenteInvestigador
{
    /** Duração da animação do golpe, em ciclos (24). */
    private static final int FRAMES_GOLPE = 24;

    /** Zig com velocidade e teclas padrão (overloading de construtores): 4 píxeis por ciclo e W A S D. */
    public Zig()
    {
        this(VELOCIDADE_PADRAO);
    }

    /**
     * Zig com velocidade escolhida e teclas W A S D (overloading).
     *
     * @param velocidade Velocidade horizontal em píxeis por ciclo (ex.: 4)
     */
    public Zig(int velocidade)
    {
        this(velocidade, ControlosJogador.wasd());
    }

    /**
     * Zig com velocidade e esquema de teclas à escolha. Carrega as animações obrigatórias
     * e a opcional de ataque (zig_atacar_0.png ...).
     *
     * @param velocidade Velocidade horizontal em píxeis por ciclo (ex.: 4)
     * @param controlos Esquema de teclas (ex.: ControlosJogador.wasd())
     */
    public Zig(int velocidade, ControlosJogador controlos)
    {
        super(velocidade, controlos);
        carregarAnimacoes("zig");
        carregarAnimacao("zig", EstadoAgente.ATACAR);
    }

    /**
     * Especialidade do Zig (implementa o método abstrato): procura um inimigo Neutralizavel
     * ao alcance e, se houver, neutraliza-o e mostra a animação de golpe.
     *
     * @return true se neutralizou um inimigo; false se não havia nenhum em posição
     */
    @Override
    protected boolean usarEspecialidade()
    {
        for (InimigoCosmico inimigo : getWorld().getObjects(InimigoCosmico.class)) {
            if (inimigo instanceof Neutralizavel) {
                Neutralizavel alvo = (Neutralizavel) inimigo;
                if (alvo.podeSerNeutralizadoPor(this)) {
                    virarPara(inimigo.getX());
                    alvo.neutralizar(this);
                    Audio.tocar("golpe.wav");
                    ocupar(EstadoAgente.ATACAR, FRAMES_GOLPE);
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Cria a distração do Zig: um frasco de éter.
     *
     * @param direcao Sentido do lançamento: 1 direita, -1 esquerda (ex.: getDirecao())
     * @return novo FrascoEter
     */
    @Override
    protected ProjectilDistracao criarDistracao(int direcao)
    {
        return new FrascoEter(direcao);
    }

    /**
     * O Zig tem salto duplo.
     *
     * @return 1 (um salto extra no ar)
     */
    @Override
    protected int getSaltosAereos()
    {
        return 1;
    }

    /**
     * O Zig não plana.
     *
     * @return false
     */
    @Override
    protected boolean podePlanar()
    {
        return false;
    }

    /**
     * O Zig não sabe hackear terminais.
     *
     * @return false
     */
    @Override
    public boolean podeHackear()
    {
        return false;
    }

    /**
     * O Zig consegue neutralizar cultistas por trás.
     *
     * @return true
     */
    @Override
    public boolean podeNeutralizarInimigos()
    {
        return true;
    }

    /**
     * Nome de código do agente.
     *
     * @return "Zig"
     */
    @Override
    public String getNomeCodigo()
    {
        return "Zig";
    }
}
