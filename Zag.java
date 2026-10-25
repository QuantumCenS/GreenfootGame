import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Agente Zag — ocultismo e tecnologia. Controlos: SETAS, ação ENTER, sair do hacking BACKSPACE.
 *  - Planar: manter a seta para cima durante a queda.
 *  - Especialidade: HACKEAR terminais (mini-jogo de circuitos) para desligar câmaras,
 *    barreiras laser e abrir cofres.
 *  - Distração: lança AMULETOS LUMINOSOS (ficam a brilhar e hipnotizam quem está perto).
 *
 * Hierarquia: Actor -> EntidadeAnimada -> AgenteInvestigador -> Zag
 * (Zag estende AgenteInvestigador, que estende EntidadeAnimada, que estende Actor: 3 níveis além de Actor).
 * Conceitos de POO: herança, overriding de todos os métodos abstratos de AgenteInvestigador,
 * overloading de construtores (3 versões encadeadas).
 */
public class Zag extends AgenteInvestigador
{
    /** Zag com velocidade e teclas padrão (overloading de construtores): 4 píxeis por ciclo e SETAS. */
    public Zag()
    {
        this(VELOCIDADE_PADRAO);
    }

    /**
     * Zag com velocidade escolhida e teclas SETAS (overloading).
     *
     * @param velocidade Velocidade horizontal em píxeis por ciclo (ex.: 4)
     */
    public Zag(int velocidade)
    {
        this(velocidade, ControlosJogador.setas());
    }

    /**
     * Zag com velocidade e esquema de teclas à escolha. Carrega as animações obrigatórias
     * e as opcionais de planar e de hackear.
     *
     * @param velocidade Velocidade horizontal em píxeis por ciclo (ex.: 4)
     * @param controlos Esquema de teclas (ex.: ControlosJogador.setas())
     */
    public Zag(int velocidade, ControlosJogador controlos)
    {
        super(velocidade, controlos);
        carregarAnimacoes("zag");
        carregarAnimacao("zag", EstadoAgente.PLANAR);
        carregarAnimacao("zag", EstadoAgente.HACKEAR);
    }

    /**
     * Especialidade do Zag (implementa o método abstrato): se estiver junto a um terminal ativo,
     * começa a hackeá-lo.
     *
     * @return true se iniciou o hacking; false se não havia terminal disponível
     */
    @Override
    protected boolean usarEspecialidade()
    {
        TerminalHacking terminal = TerminalHacking.procurarPara(this);
        if (terminal == null) {
            return false;
        }
        return terminal.iniciarHacking(this);
    }

    /**
     * Cria a distração do Zag: um amuleto luminoso.
     *
     * @param direcao Sentido do lançamento: 1 direita, -1 esquerda (ex.: getDirecao())
     * @return novo AmuletoLuminoso
     */
    @Override
    protected ProjectilDistracao criarDistracao(int direcao)
    {
        return new AmuletoLuminoso(direcao);
    }

    /**
     * O Zag não tem salto duplo.
     *
     * @return 0
     */
    @Override
    protected int getSaltosAereos()
    {
        return 0;
    }

    /**
     * O Zag plana ao manter o salto premido durante a queda.
     *
     * @return true
     */
    @Override
    protected boolean podePlanar()
    {
        return true;
    }

    /**
     * O Zag sabe hackear terminais.
     *
     * @return true
     */
    @Override
    public boolean podeHackear()
    {
        return true;
    }

    /**
     * O Zag não neutraliza inimigos.
     *
     * @return false
     */
    @Override
    public boolean podeNeutralizarInimigos()
    {
        return false;
    }

    /**
     * Nome de código do agente.
     *
     * @return "Zag"
     */
    @Override
    public String getNomeCodigo()
    {
        return "Zag";
    }
}
