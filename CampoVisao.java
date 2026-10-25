import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.ArrayList;
import java.util.List;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Campo de visão cónico partilhado (por composição) pelos cultistas e pelas câmaras.
 * Faz três verificações: distância, ângulo dentro do cone e linha de visão livre
 * (paredes sólidas tapam a vista; plataformas de madeira/grade não).
 * Também desenha o cone, recortado pelas paredes, para o jogador perceber o que é visto.
 *
 * Ângulos em graus: 0 = direita, 90 = para baixo (o eixo y do ecrã cresce para baixo).
 *
 * Classe auxiliar (não é um Actor).
 * Conceitos de POO: composição (CultistaPatrulheiro e CamaraVigilancia "têm um" CampoVisao, em vez
 * de herdarem a visão), encapsulamento (atributos privados e finais), métodos estáticos utilitários.
 */
public class CampoVisao
{
    /** Distância entre amostras ao longo de um raio, em píxeis (8). */
    private static final int PASSO_RAIO = 8;
    /** Número de raios usados para desenhar o contorno do cone (14). */
    private static final int RAIOS_DESENHO = 14;

    /** Alcance máximo da visão, em píxeis (mínimo 10; ex.: 240). */
    private final int alcance;
    /** Metade da abertura do cone, em graus (entre 1 e 89; ex.: 24). */
    private final double semiAngulo;

    /**
     * Cria um campo de visão. Os valores são limitados: alcance mínimo 10 e semiângulo entre 1 e 89 graus.
     *
     * @param alcance Alcance da visão, em píxeis (ex.: 240 num cultista, 420 numa câmara)
     * @param semiAnguloGraus metade da abertura do cone, em graus (ex.: 24 num cultista, 17 numa câmara)
     */
    public CampoVisao(int alcance, double semiAnguloGraus)
    {
        this.alcance = Math.max(10, alcance);
        this.semiAngulo = Math.max(1, Math.min(89, semiAnguloGraus));
    }

    /**
     * Verifica se o observador em (ox, oy), a olhar na direção indicada, vê o agente.
     * Testa a cabeça e o tronco; um agente escondido nas sombras nunca é visto.
     *
     * @param mundo Mundo onde estão o observador e o agente (ex.: getWorld())
     * @param ox Coordenada x do observador, em píxeis (ex.: getOlhoX() de um cultista)
     * @param oy Coordenada y do observador, em píxeis (ex.: getOlhoY() de um cultista)
     * @param direcao Direção para onde olha, em graus (ex.: 0 para a direita, 90 para baixo)
     * @param agente Agente a testar (ex.: o Zig)
     * @return true se o agente estiver dentro do alcance, do cone e sem paredes pelo meio
     */
    public boolean ve(World mundo, int ox, int oy, double direcao, AgenteInvestigador agente)
    {
        if (agente.getWorld() != mundo || agente.isEscondido()) {
            return false;
        }
        List<Plataforma> paredes = paredesProximas(mundo, ox, oy);
        int[] alturas = { agente.getTopoColisao() + 10, agente.getCentroY() };
        for (int y : alturas) {
            if (pontoVisivel(paredes, ox, oy, direcao, agente.getX(), y)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se um ponto está dentro do alcance, dentro do cone e com linha de visão livre.
     *
     * @param paredes Paredes sólidas próximas (ex.: o resultado de paredesProximas)
     * @param ox Coordenada x do observador, em píxeis
     * @param oy Coordenada y do observador, em píxeis
     * @param direcao Direção do cone, em graus (ex.: 90)
     * @param px Coordenada x do ponto, em píxeis (ex.: agente.getX())
     * @param py Coordenada y do ponto, em píxeis (ex.: agente.getCentroY())
     * @return true se o ponto for visível
     */
    private boolean pontoVisivel(List<Plataforma> paredes, int ox, int oy, double direcao, int px, int py)
    {
        double dx = px - ox;
        double dy = py - oy;
        if (dx * dx + dy * dy > (double) alcance * alcance) {
            return false;
        }
        double angulo = Math.toDegrees(Math.atan2(dy, dx));
        if (Math.abs(diferencaAngulo(angulo, direcao)) > semiAngulo) {
            return false;
        }
        return linhaLivre(paredes, ox, oy, px, py);
    }

    /**
     * Calcula a diferença entre dois ângulos, normalizada para [-180, 180].
     *
     * @param a Primeiro ângulo, em graus (ex.: 170)
     * @param b Segundo ângulo, em graus (ex.: -170)
     * @return a - b normalizado em graus (ex.: -20 para os valores acima)
     */
    public static double diferencaAngulo(double a, double b)
    {
        double d = (a - b) % 360;
        if (d > 180) {
            d -= 360;
        }
        if (d < -180) {
            d += 360;
        }
        return d;
    }

    /**
     * Verifica se nenhuma parede sólida corta o segmento entre os dois pontos
     * (amostra o segmento a cada 8 píxeis).
     *
     * @param paredes Paredes sólidas a considerar (ex.: o resultado de paredesProximas)
     * @param x0 Coordenada x do início, em píxeis (ex.: a posição x dos olhos)
     * @param y0 Coordenada y do início, em píxeis
     * @param x1 Coordenada x do fim, em píxeis (ex.: agente.getX())
     * @param y1 Coordenada y do fim, em píxeis
     * @return true se a linha estiver livre
     */
    public static boolean linhaLivre(List<Plataforma> paredes, double x0, double y0, double x1, double y1)
    {
        double distancia = Math.hypot(x1 - x0, y1 - y0);
        int passos = (int) (distancia / PASSO_RAIO);
        for (int i = 1; i < passos; i++) {
            double t = (double) i / passos;
            if (dentroDeParede(paredes, x0 + (x1 - x0) * t, y0 + (y1 - y0) * t)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Verifica se um ponto está dentro de alguma das paredes.
     *
     * @param paredes Paredes a testar
     * @param x Coordenada x do ponto, em píxeis
     * @param y Coordenada y do ponto, em píxeis
     * @return true se o ponto estiver dentro de uma parede
     */
    private static boolean dentroDeParede(List<Plataforma> paredes, double x, double y)
    {
        for (Plataforma p : paredes) {
            if (x > p.getEsquerda() && x < p.getDireita() && y > p.getTopo() && y < p.getBase()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Seleciona as paredes sólidas (e colidíveis) dentro do alcance (otimização).
     *
     * @param mundo Mundo onde procurar (ex.: getWorld())
     * @param ox Coordenada x do observador, em píxeis
     * @param oy Coordenada y do observador, em píxeis
     * @return lista nova com as paredes relevantes
     */
    private List<Plataforma> paredesProximas(World mundo, int ox, int oy)
    {
        List<Plataforma> resultado = new ArrayList<Plataforma>();
        for (Plataforma p : mundo.getObjects(Plataforma.class)) {
            if (p.isSolida() && p.isColidivel()
                && p.getDireita() > ox - alcance && p.getEsquerda() < ox + alcance
                && p.getBase() > oy - alcance && p.getTopo() < oy + alcance) {
                resultado.add(p);
            }
        }
        return resultado;
    }

    /**
     * Calcula a distância até à primeira parede numa direção (ou o alcance, se não houver).
     *
     * @param paredes Paredes a considerar
     * @param ox Coordenada x da origem, em píxeis
     * @param oy Coordenada y da origem, em píxeis
     * @param anguloGraus Direção do raio, em graus (ex.: 73)
     * @return comprimento do raio em píxeis
     */
    private double comprimentoRaio(List<Plataforma> paredes, int ox, int oy, double anguloGraus)
    {
        double rad = Math.toRadians(anguloGraus);
        double cx = Math.cos(rad);
        double cy = Math.sin(rad);
        for (int d = PASSO_RAIO; d < alcance; d += PASSO_RAIO) {
            if (dentroDeParede(paredes, ox + cx * d, oy + cy * d)) {
                return d;
            }
        }
        return alcance;
    }

    /**
     * Desenha o cone numa imagem (polígono preenchido e contorno), recortado pelas paredes.
     * @param imagem  imagem de destino (ex.: a imagem do ConeVisao)
     * @param cx      posição do vértice do cone DENTRO da imagem (ex.: 242, o centro da imagem do cone)
     * @param cy      idem, em y (ex.: 242)
     * @param mundo   mundo onde procurar as paredes (ex.: getWorld())
     * @param ox      posição do vértice no MUNDO (para recortar pelas paredes) (ex.: getOlhoX())
     * @param oy      idem, em y (ex.: getOlhoY())
     * @param direcao direção do centro do cone, em graus (ex.: 90)
     * @param preenchimento cor do interior, normalmente semitransparente (ex.: new Color(255, 245, 190, 40))
     * @param contorno cor das linhas do contorno (ex.: new Color(255, 245, 190, 90))
     */
    public void desenhar(GreenfootImage imagem, int cx, int cy, World mundo, int ox, int oy,
                         double direcao, Color preenchimento, Color contorno)
    {
        List<Plataforma> paredes = paredesProximas(mundo, ox, oy);
        int n = RAIOS_DESENHO + 2;
        int[] xs = new int[n];
        int[] ys = new int[n];
        xs[0] = cx;
        ys[0] = cy;
        for (int i = 0; i <= RAIOS_DESENHO; i++) {
            double a = direcao - semiAngulo + 2 * semiAngulo * i / RAIOS_DESENHO;
            double r = comprimentoRaio(paredes, ox, oy, a);
            xs[i + 1] = cx + (int) Math.round(Math.cos(Math.toRadians(a)) * r);
            ys[i + 1] = cy + (int) Math.round(Math.sin(Math.toRadians(a)) * r);
        }
        imagem.setColor(preenchimento);
        imagem.fillPolygon(xs, ys, n);
        imagem.setColor(contorno);
        imagem.drawLine(cx, cy, xs[1], ys[1]);
        imagem.drawLine(cx, cy, xs[n - 1], ys[n - 1]);
        for (int i = 1; i < n - 1; i++) {
            imagem.drawLine(xs[i], ys[i], xs[i + 1], ys[i + 1]);
        }
    }

    /**
     * Devolve o alcance da visão.
     *
     * @return alcance em píxeis (ex.: 240)
     */
    public int getAlcance()
    {
        return alcance;
    }

    /**
     * Devolve metade da abertura do cone.
     *
     * @return semiângulo em graus (ex.: 24)
     */
    public double getSemiAngulo()
    {
        return semiAngulo;
    }
}
