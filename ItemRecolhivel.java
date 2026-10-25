import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (método desenhar)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Classe base ABSTRATA dos objetos que os agentes apanham ao tocar-lhes
 * (notas de lore, ficheiros classificados). Flutua a brilhar e, quando um agente
 * lhe toca, chama aoSerRecolhido() — que cada subclasse define — e desaparece.
 *
 * Hierarquia: ItemRecolhivel estende ElementoInterativo, que estende Actor; subclasses: NotaLore e FicheiroSecreto.
 * Conceitos de POO: classe abstrata com método abstrato (aoSerRecolhido), polimorfismo (cada item
 * reage à recolha de forma diferente), construtor protegido, overriding de act() e addedToWorld().
 */
public abstract class ItemRecolhivel extends ElementoInterativo
{
    /** Lado da imagem quadrada do item, em píxeis (64). */
    private static final int TAMANHO = 64;

    /** Ícone desenhado no centro (ex.: "nota.png"). */
    private final GreenfootImage icone;
    /** Cor do halo e das partículas ao ser apanhado. */
    private final Color corBrilho;
    /** Coordenada y à volta da qual o item flutua. */
    private int yBase;
    /** Contador da animação (começa num valor aleatório para os itens não pulsarem em sincronia). */
    private int fase;

    /**
     * Cria o item com o ícone e a cor de brilho indicados.
     *
     * @param ficheiroIcone Ficheiro do ícone na pasta images (ex.: "nota.png")
     * @param corBrilho Cor do halo (ex.: new Color(255, 230, 150) nas notas, new Color(255, 70, 70) nos ficheiros)
     */
    protected ItemRecolhivel(String ficheiroIcone, Color corBrilho)
    {
        this.icone = new GreenfootImage(ficheiroIcone);
        this.corBrilho = corBrilho;
        this.fase = Greenfoot.getRandomNumber(60);
        desenhar();
    }

    /**
     * Guarda a altura de referência para a flutuação (overriding de Actor.addedToWorld).
     *
     * @param mundo Mundo onde foi colocado (ex.: um Cenario1)
     */
    @Override
    protected void addedToWorld(World mundo)
    {
        yBase = getY();
    }

    /**
     * Contrato: o que acontece quando é apanhado (polimorfismo).
     *
     * @param agente Agente que apanhou o item (ex.: o Zig)
     * @param cenario Nível onde o item foi apanhado (ex.: um Cenario1)
     */
    protected abstract void aoSerRecolhido(AgenteInvestigador agente, Cenario cenario);

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): flutua 4 píxeis para
     * cima e para baixo, redesenha o halo e, se um agente lhe tocar, é recolhido e desaparece com partículas.
     */
    public void act()
    {
        fase++;
        setLocation(getX(), yBase + (int) Math.round(Math.sin(fase * 0.08) * 4));
        if (fase % 3 == 0) {
            desenhar();
        }
        for (AgenteInvestigador agente : getWorld().getObjects(AgenteInvestigador.class)) {
            if (tocaNoAgente(agente)) {
                if (getWorld() instanceof Cenario) {
                    aoSerRecolhido(agente, (Cenario) getWorld());
                }
                getWorld().addObject(new EfeitoParticulas(corBrilho, 16), getX(), getY());
                getWorld().removeObject(this);
                return;
            }
        }
    }

    /**
     * Verifica se a hitbox do agente toca num quadrado de 28 x 28 píxeis à volta do centro do item.
     *
     * @param a Agente a testar (ex.: o Zag)
     * @return true se houver contacto
     */
    private boolean tocaNoAgente(AgenteInvestigador a)
    {
        int meio = 14;
        return a.getDireitaColisao() > getX() - meio && a.getEsquerdaColisao() < getX() + meio
            && a.getBaseColisao() > getY() - meio && a.getTopoColisao() < getY() + meio;
    }

    //utilizado AI para o desenho
    /** Desenha o halo pulsante (círculos semitransparentes) e o ícone por cima. */
    private void desenhar()
    {
        GreenfootImage img = new GreenfootImage(TAMANHO, TAMANHO);
        int pulso = (int) (4 * Math.sin(fase * 0.12));
        for (int r = 26 + pulso; r > 6; r -= 5) {
            int alfa = 70 * (30 - r) / 30 + 10;
            img.setColor(new Color(corBrilho.getRed(), corBrilho.getGreen(), corBrilho.getBlue(), Math.max(0, Math.min(255, alfa))));
            img.fillOval(TAMANHO / 2 - r, TAMANHO / 2 - r, r * 2, r * 2);
        }
        img.drawImage(icone, (TAMANHO - icone.getWidth()) / 2, (TAMANHO - icone.getHeight()) / 2);
        setImage(img);
    }
}
