import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Guarda que não patrulha: fica parado no posto e vira-se para o outro lado de
 * tantos em tantos segundos (redefine comportamentoPatrulha()). O resto do comportamento
 * (visão, alerta, investigar, ser neutralizado por trás) é herdado do CultistaPatrulheiro.
 * Para o apanhar é preciso cronometrar: esperar que se vire e aproximar-se por trás.
 *
 * Hierarquia: ... -> InimigoCosmico -> CultistaPatrulheiro -> CultistaVigia
 * (CultistaVigia estende CultistaPatrulheiro, que estende InimigoCosmico, que estende EntidadeAnimada,
 * que estende Actor: 4 níveis além de Actor).
 * Conceitos de POO: herança (reutiliza toda a máquina de estados), overriding de comportamentoPatrulha(),
 * overloading de construtores (3 versões encadeadas).
 */
public class CultistaVigia extends CultistaPatrulheiro
{
    /** Ciclos entre cada vez que se vira (mínimo 30; ex.: 200). */
    private final int intervaloVirar;
    /** Ciclos desde a última vez que se virou. */
    private int contador;

    /** Vigia que se vira a cada 180 ciclos (para criar pelo menu do Greenfoot). */
    public CultistaVigia()
    {
        this(180);
    }

    /**
     * Vigia que começa a olhar para a direita (overloading).
     *
     * @param intervaloVirar frames entre cada vez que se vira (ex.: 180)
     */
    public CultistaVigia(int intervaloVirar)
    {
        this(intervaloVirar, true);
    }

    /**
     * Vigia com imagens "vigia", rota nula, cone inclinado 6 graus e alcance de 260 px.
     *
     * @param intervaloVirar Ciclos entre cada vez que se vira, mínimo 30 (ex.: 200, no Cenario3)
     * @param comecaVirado true = começa a olhar para a direita (ex.: false, no Cenario3)
     */
    public CultistaVigia(int intervaloVirar, boolean comecaVirado)
    {
        super(0, 6, "vigia", 260, 24);
        this.intervaloVirar = Math.max(30, intervaloVirar);
        setViradoParaDireita(comecaVirado);
    }

    /** Overriding de CultistaPatrulheiro.comportamentoPatrulha: em vez de andar, vira-se periodicamente. */
    @Override
    protected void comportamentoPatrulha()
    {
        contador++;
        if (contador >= intervaloVirar) {
            contador = 0;
            setViradoParaDireita(!isViradoParaDireita());
        }
    }

    /**
     * Devolve o intervalo entre viragens.
     *
     * @return ciclos entre cada viragem (ex.: 200)
     */
    public int getIntervaloVirar()
    {
        return intervaloVirar;
    }
}
