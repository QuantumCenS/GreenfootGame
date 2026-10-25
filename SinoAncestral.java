import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira (incluindo a animação de balanço em act)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Sino de bronze pendurado nas catacumbas. Quando um frasco ou amuleto lhe acerta,
 * toca e o som ouve-se a 800 px: todos os inimigos à volta vão investigar o sino.
 * Ótimo para afastar guardas de um terminal ou de uma porta.
 *
 * Hierarquia: SinoAncestral estende ElementoInterativo, que estende Actor.
 * Conceitos de POO: herança, overriding de act(), encapsulamento (estado privado).
 */
public class SinoAncestral extends ElementoInterativo
{
    /** Ciclos de espera até poder tocar outra vez (120). */
    private static final int RECARGA = 120;
    /** Duração do balanço depois de tocar, em ciclos (50). */
    private static final int FRAMES_BALANCO = 50;

    /** Ciclos de recarga restantes. */
    private int recarga;
    /** Ciclos de balanço restantes. */
    private int balanco;

    /** Cria o sino com a imagem "sino.png". */
    public SinoAncestral()
    {
        setImage("sino.png");
    }

    /**
     * Faz o sino tocar (se não tiver tocado há pouco): som, ruído TipoRuido.SINO no Cenario,
     * balão "DONG!" e balanço.
     */
    public void tocar()
    {
        if (recarga > 0 || getWorld() == null) {
            return;
        }
        recarga = RECARGA;
        balanco = FRAMES_BALANCO;
        Audio.tocar("sino.wav");
        if (getWorld() instanceof Cenario) {
            ((Cenario) getWorld()).emitirRuido(getX(), getBase(), TipoRuido.SINO);
        }
        getWorld().addObject(new Balao(this, "DONG!", new Color(160, 120, 30), 50), getX(), getY());
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): desconta a recarga e,
     * enquanto balança, roda a imagem com uma oscilação que vai diminuindo.
     */
    public void act()
    {
        if (recarga > 0) {
            recarga--;
        }
        if (balanco > 0) {
            balanco--;
            setRotation((int) (Math.sin(balanco * 0.5) * balanco * 0.5));
        } else {
            setRotation(0);
        }
    }

    /**
     * Indica se o sino está a tocar (a balançar).
     *
     * @return true durante o balanço
     */
    public boolean isATocar()
    {
        return balanco > 0;
    }
}
