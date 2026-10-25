import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira (incluindo o efeito visual em tremer)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Plataforma frágil (gelo rachado, rocha podre): quando alguém se apoia nela começa a
 * tremer e, passado meio segundo, desfaz-se. Volta a aparecer uns segundos depois.
 * Obriga os agentes a não ficar parados — e a passar um de cada vez com cuidado.
 *
 * Hierarquia: PlataformaFragil estende Plataforma, que estende ElementoInterativo, que estende Actor.
 * Conceitos de POO: herança, overriding (act, isColidivel), overloading de construtores (2 versões).
 */
public class PlataformaFragil extends Plataforma
{
    /** Ciclos a tremer antes de se desfazer (32, cerca de meio segundo). */
    private static final int FRAMES_ATE_CAIR = 32;
    /** Ciclos até voltar a aparecer depois de se desfazer (240). */
    private static final int FRAMES_ATE_VOLTAR = 240;

    /** Imagem da plataforma inteira. */
    private final GreenfootImage imagemInteira;
    /** Imagem transparente usada enquanto está desfeita. */
    private final GreenfootImage imagemVazia;
    /** Ciclos que faltam para se desfazer; maior que 0 enquanto treme. */
    private int contagem;      // > 0 a tremer
    /** Ciclos que faltam para voltar; maior que 0 enquanto está desfeita. */
    private int ausente;       // > 0 desfeita

    /** Plataforma frágil com 96 px de largura (para criar pelo menu do Greenfoot). */
    public PlataformaFragil()
    {
        this(96);
    }

    /**
     * Plataforma frágil atravessável, com 30 px de altura e a textura "fragil.png".
     *
     * @param largura Largura em píxeis (ex.: 64 na ponte de gelo do Cenario4)
     */
    public PlataformaFragil(int largura)
    {
        super(Plataforma.criarImagem(largura, 30, "fragil.png", "fragil.png"), false);
        imagemInteira = getImage();
        imagemVazia = new GreenfootImage(largura, 30);
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): se estiver desfeita,
     * conta até voltar; se alguém estiver em cima, treme e desfaz-se ao fim de 32 ciclos.
     */
    public void act()
    {
        if (ausente > 0) {
            ausente--;
            if (ausente == 0) {
                setImage(imagemInteira);
            }
            return;
        }
        if (contagem == 0 && alguemEmCima()) {
            contagem = FRAMES_ATE_CAIR;
            Audio.tocar("rachar.wav");
        }
        if (contagem > 0) {
            contagem--;
            tremer();
            if (contagem == 0) {
                desfazer();
            }
        }
    }

    /**
     * Verifica se alguma entidade (agente ou inimigo) está apoiada nesta plataforma.
     *
     * @return true se houver alguém em cima
     */
    private boolean alguemEmCima()
    {
        for (EntidadeAnimada e : getWorld().getObjects(EntidadeAnimada.class)) {
            if (e.getPlataformaApoio() == this) {
                return true;
            }
        }
        return false;
    }

    /** Treme a imagem (sem mexer na posição, para não estragar as colisões). */
    private void tremer()
    {
        GreenfootImage img = new GreenfootImage(imagemInteira.getWidth(), imagemInteira.getHeight());
        int dx = (contagem % 4 < 2) ? -2 : 2;
        img.drawImage(imagemInteira, dx, 0);
        setImage(img);
    }

    /** Desfaz a plataforma: fica invisível e sem colisão, com um efeito de partículas. */
    private void desfazer()
    {
        ausente = FRAMES_ATE_VOLTAR;
        setImage(imagemVazia);
        getWorld().addObject(new EfeitoParticulas(new Color(190, 196, 214), 16), getX(), getY());
    }

    /**
     * Desfeita não conta para colisões (overriding de Plataforma.isColidivel).
     *
     * @return true se estiver inteira; false enquanto estiver desfeita
     */
    @Override
    public boolean isColidivel()
    {
        return ausente == 0;
    }

    /**
     * Indica se a plataforma está desfeita.
     *
     * @return true enquanto estiver desfeita
     */
    public boolean isDesfeita()
    {
        return ausente > 0;
    }
}
