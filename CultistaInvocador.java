import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Sacerdote da Ordem Abissal: patrulha como os outros cultistas (herda tudo de
 * CultistaPatrulheiro), mas quando entra em ALERTA e tem o agente à vista, em vez de
 * correr para ele PÁRA e invoca orbes sombrios que voam na direção do agente.
 * Vê mais longe (280 px). Também pode ser neutralizado pelo Zig por trás.
 *
 * Hierarquia: Actor -> EntidadeAnimada -> InimigoCosmico -> CultistaPatrulheiro -> CultistaInvocador
 * (4 níveis além de Actor); subclasse: SumoSacerdote.
 * Conceitos de POO: herança, overriding (comportamentoAlerta, atualizarAnimacao, act), uso de
 * super.metodo() para estender o comportamento herdado, overloading de construtores (4 versões),
 * método hook getRecargaInvocacao() redefinido pelo SumoSacerdote.
 */
public class CultistaInvocador extends CultistaPatrulheiro
{
    /** Distância horizontal mínima ao agente para invocar em vez de perseguir, em píxeis (90). */
    private static final int DISTANCIA_MINIMA = 90;
    /** Ciclos de braços no ar antes do disparo (40). */
    private static final int PREPARACAO = 40;     // frames de braços no ar antes do disparo
    /** Ciclos entre dois orbes (85). */
    private static final int RECARGA = 85;

    /** Animação de invocação (braços no ar). */
    private final Animacao animInvocar;
    /** Ciclos passados no ciclo de invocação atual. */
    private int framesInvocacao;
    /** true enquanto está parado a invocar. */
    private boolean aInvocar;

    /** Invocador com rota de 160 px (para criar pelo menu do Greenfoot). */
    public CultistaInvocador()
    {
        this(160);
    }

    /**
     * Invocador com o cone de visão ligeiramente inclinado para baixo (8 graus) (overloading).
     *
     * @param limite Comprimento da rota para a direita, em píxeis (ex.: 150, no Cenario2)
     */
    public CultistaInvocador(int limite)
    {
        this(limite, 8);
    }

    /**
     * Invocador com imagens "invocador" e alcance de visão de 280 px.
     *
     * @param limite Comprimento da rota para a direita, em píxeis (ex.: 80, no Cenario2)
     * @param inclinacaoVisao graus para baixo (ex.: 35 numa varanda)
     */
    public CultistaInvocador(int limite, int inclinacaoVisao)
    {
        this(limite, inclinacaoVisao, "invocador", 280);
    }

    /**
     * Construtor para variantes (ex.: SumoSacerdote) com outras imagens e alcance.
     *
     * @param limite Comprimento da rota para a direita, em píxeis (ex.: 180 no SumoSacerdote do Cenario5)
     * @param inclinacaoVisao Graus que o cone aponta para baixo (ex.: 10)
     * @param prefixo Prefixo dos ficheiros de imagem (ex.: "sacerdote")
     * @param alcanceVisao Alcance do cone de visão, em píxeis (ex.: 340)
     */
    protected CultistaInvocador(int limite, int inclinacaoVisao, String prefixo, int alcanceVisao)
    {
        super(limite, inclinacaoVisao, prefixo, alcanceVisao, 26);
        animInvocar = carregarAnimacao(prefixo + "_invocar", 2, 8);
    }

    /**
     * Devolve os ciclos entre orbes (o SumoSacerdote redefine para ser mais rápido).
     *
     * @return 85 ciclos
     */
    protected int getRecargaInvocacao()
    {
        return RECARGA;
    }

    /**
     * Overriding de CultistaPatrulheiro.comportamentoAlerta: à distância (mais de 90 px) pára e
     * invoca orbes; muito perto, persegue como um cultista normal (super.comportamentoAlerta).
     *
     * @param visto Agente visível neste ciclo, ou null (ex.: o Zag a fugir)
     */
    @Override
    protected void comportamentoAlerta(AgenteInvestigador visto)
    {
        if (visto != null && Math.abs(visto.getX() - getX()) > DISTANCIA_MINIMA) {
            virarPara(visto.getX());
            aInvocar = true;
            framesInvocacao++;
            if (framesInvocacao == PREPARACAO) {
                lancarOrbe(visto);
            }
            if (framesInvocacao >= getRecargaInvocacao()) {
                framesInvocacao = 0;
            }
        } else {
            aInvocar = false;
            framesInvocacao = Math.min(framesInvocacao, PREPARACAO / 2);
            super.comportamentoAlerta(visto);
        }
    }

    /**
     * Cria um ProjectilSombrio à frente das mãos, apontado ao centro do agente.
     *
     * @param alvo Agente para onde o orbe é lançado (ex.: o Zig)
     */
    private void lancarOrbe(AgenteInvestigador alvo)
    {
        int x = getX() + getDirecao() * 22;
        int y = getTopoColisao() + 8;
        getWorld().addObject(new ProjectilSombrio(alvo.getX() - x, alvo.getCentroY() - y), x, y);
        Audio.tocar("orbe.wav");
    }

    /** Redefine atualizarAnimacao: mostra a pose de invocação enquanto invoca; senão usa a da superclasse. */
    @Override
    protected void atualizarAnimacao()
    {
        if (aInvocar && getEstado() == EstadoInimigo.ALERTA) {
            mostrar(animInvocar);
        } else {
            super.atualizarAnimacao();
        }
    }

    /**
     * Redefine act (chamado pelo Greenfoot em cada ciclo): fora de ALERTA cancela a invocação
     * em curso e depois executa o ciclo herdado.
     */
    @Override
    public void act()
    {
        if (getEstado() != EstadoInimigo.ALERTA) {
            aInvocar = false;
            framesInvocacao = 0;
        }
        super.act();
    }
}
