import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: código de desenho/gráficos (método desenhar)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Orbe de energia sombria lançado pelo CultistaInvocador (projétil dos inimigos para o jogador).
 * Voa em linha reta; se tocar num agente causa dano; desfaz-se ao bater numa parede.
 *
 * Hierarquia: ProjectilSombrio estende Projectil, que estende ElementoInterativo, que estende Actor
 * (3 níveis além de Actor).
 * Conceitos de POO: herança, implementação do método abstrato verificarColisoes (overriding),
 * métodos privados estáticos auxiliares.
 */
public class ProjectilSombrio extends Projectil
{
    /** Velocidade do orbe, em píxeis por ciclo (5). */
    private static final double VELOCIDADE = 5.0;
    /** Duração máxima do orbe, em ciclos (200). */
    private static final int DURACAO = 200;

    /**
     * Cria um orbe que voa na direção (dx, dy), sem gravidade, durante no máximo 200 ciclos.
     *
     * @param dx componente x da direção do disparo; é normalizada para a velocidade fixa
     *           (ex.: alvo.getX() - x, a distância horizontal ao agente)
     * @param dy componente y da direção do disparo; é normalizada para a velocidade fixa
     *           (ex.: alvo.getCentroY() - y)
     */
    public ProjectilSombrio(double dx, double dy)
    {
        super(normalizar(dx, dx, dy), normalizar(dy, dx, dy), 0, DURACAO);
        setImage(desenhar(0));
    }

    /**
     * Converte uma componente da direção numa componente da velocidade (comprimento total 5).
     *
     * @param componente Componente a converter (ex.: dx)
     * @param dx Componente x da direção (ex.: 200)
     * @param dy Componente y da direção (ex.: -40)
     * @return componente da velocidade em píxeis por ciclo, ou 0 se a direção for nula
     */
    private static double normalizar(double componente, double dx, double dy)
    {
        double comprimento = Math.hypot(dx, dy);
        return comprimento < 0.001 ? 0 : componente / comprimento * VELOCIDADE;
    }
    //desenhado com ajuda de AI
    /**
     * Desenha o orbe: halo roxo semitransparente, núcleo e reflexo; o halo pulsa a cada 5 ciclos.
     *
     * @param fase Idade do orbe em ciclos, usada para a pulsação (ex.: getIdade())
     * @return nova imagem de 26 x 26 píxeis
     */
    private static GreenfootImage desenhar(int fase)
    {
        GreenfootImage img = new GreenfootImage(26, 26);
        int pulso = fase % 10 < 5 ? 0 : 2;
        img.setColor(new Color(140, 60, 220, 70));
        img.fillOval(1 - pulso / 2, 1 - pulso / 2, 24 + pulso, 24 + pulso);
        img.setColor(new Color(180, 90, 255, 160));
        img.fillOval(5, 5, 16, 16);
        img.setColor(new Color(40, 0, 60));
        img.fillOval(9, 9, 8, 8);
        img.setColor(new Color(240, 200, 255));
        img.fillRect(10, 10, 2, 2);
        return img;
    }

    /**
     * Implementa Projectil.verificarColisoes: atualiza a imagem, fere o primeiro agente que tocar
     * (empurrando-o no sentido do voo) e desfaz-se; também se desfaz ao bater numa parede.
     */
    @Override
    protected void verificarColisoes()
    {
        if (getIdade() % 5 == 0) {
            setImage(desenhar(getIdade()));
        }
        for (AgenteInvestigador agente : getWorld().getObjects(AgenteInvestigador.class)) {
            if (getX() >= agente.getEsquerdaColisao() - 6 && getX() <= agente.getDireitaColisao() + 6
                && getY() >= agente.getTopoColisao() && getY() <= agente.getBaseColisao()) {
                agente.sofrerDano(getVelocidadeX() >= 0 ? 1 : -1);
                desfazer();
                return;
            }
        }
        if (paredeNoPonto() != null) {
            desfazer();
        }
    }

    /** Desfaz o orbe com partículas roxas e remove-o. */
    private void desfazer()
    {
        getWorld().addObject(new EfeitoParticulas(new Color(170, 80, 255), 10), getX(), getY());
        remover();
    }
}
