import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (método desenhar)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Barreira laser: uma parede sólida e intransponível (vai até acima do ecrã) que só
 * desaparece quando o Zag hackeia o terminal a que está ligada (Hackeavel).
 *
 * Hierarquia: BarreiraSeguranca estende Plataforma, que estende ElementoInterativo, que estende Actor.
 * Conceitos de POO: herança (é uma Plataforma sólida para efeitos de colisão), implementação de
 * interface (Hackeavel), overriding de act().
 */
public class BarreiraSeguranca extends Plataforma implements Hackeavel
{
    /** Largura da barreira, em píxeis (32). */
    private static final int LARGURA = 32;

    /** Primeira fase da animação dos raios. */
    private final GreenfootImage imagemA;
    /** Segunda fase da animação dos raios (raios desfasados 9 píxeis). */
    private final GreenfootImage imagemB;
    /** true depois de ser desativada por hacking. */
    private boolean desativada;
    /** Contador de ciclos para alternar as imagens. */
    private int contador;

    /**
     * Cria a barreira com as duas fases da animação.
     *
     * @param altura altura total (usar um valor que vá acima do topo do ecrã), em píxeis
     *               (ex.: CHAO + 100 = 620, colocada com o topo em -100)
     */
    public BarreiraSeguranca(int altura)
    {
        super(desenhar(altura, 0), true);
        imagemA = getImage();
        imagemB = desenhar(altura, 1);
    }
    //foi utilizado AI
    /**
     * Desenha a barreira: dois postes laterais, um campo vermelho semitransparente e raios
     * horizontais a cada 18 píxeis.
     *
     * @param altura Altura da imagem em píxeis, mínimo 10 (ex.: 620)
     * @param fase Fase da animação: 0 ou 1 (desfasa os raios 9 píxeis) (ex.: 1)
     * @return nova imagem da barreira
     */
    private static GreenfootImage desenhar(int altura, int fase)
    {
        GreenfootImage img = new GreenfootImage(LARGURA, Math.max(10, altura));
        img.setColor(new Color(60, 64, 76));
        img.fillRect(0, 0, 6, img.getHeight());
        img.fillRect(LARGURA - 6, 0, 6, img.getHeight());
        img.setColor(new Color(110, 116, 130));
        img.fillRect(1, 0, 2, img.getHeight());
        img.fillRect(LARGURA - 5, 0, 2, img.getHeight());
        img.setColor(new Color(255, 40, 60, 60));
        img.fillRect(6, 0, LARGURA - 12, img.getHeight());
        for (int y = (fase * 9) % 18; y < img.getHeight(); y += 18) {
            img.setColor(new Color(255, 60, 80, 220));
            img.fillRect(6, y, LARGURA - 12, 2);
            img.setColor(new Color(255, 200, 210));
            img.fillRect(6, y, LARGURA - 12, 1);
        }
        return img;
    }

    /** Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): alterna as imagens a cada 6 ciclos. */
    public void act()
    {
        contador++;
        if (contador % 6 == 0) {
            setImage(getImage() == imagemA ? imagemB : imagemA);
        }
    }

    /** Implementa Hackeavel: lança faíscas ao longo da barreira e remove-a do mundo. */
    @Override
    public void desativarPorHacking()
    {
        if (desativada || getWorld() == null) {
            return;
        }
        desativada = true;
        World mundo = getWorld();
        for (int y = getTopo() + 40; y < getBase(); y += 120) {
            mundo.addObject(new EfeitoParticulas(new Color(255, 80, 100), 8), getX(), y);
        }
        mundo.removeObject(this);
    }

    /**
     * Implementa Hackeavel.
     *
     * @return true se a barreira já foi desativada
     */
    @Override
    public boolean isHackeado()
    {
        return desativada;
    }
}
