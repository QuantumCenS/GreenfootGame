import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Plataforma estática. A imagem é montada repetindo a textura do tipo escolhido
 * (primeira linha com a textura de topo, o resto com a de corpo), por isso pode ter
 * qualquer largura e altura.
 *
 * Hierarquia: Actor -> ElementoInterativo -> Plataforma -> (Chao, PlataformaMovel -> RochaRunica)
 * Outras subclasses: PlataformaFragil, CaixaPesada, BarreiraSeguranca, Portao (-> PortaoSelado, Cofre).
 * Conceitos de POO: herança (Plataforma estende ElementoInterativo, que estende Actor), overloading
 * de construtores (5 versões), métodos redefinidos pelas subclasses (isColidivel, isMovel, getDeltaX,
 * getDeltaY), encapsulamento (atributos privados e finais), método protected static reutilizável.
 */
public class Plataforma extends ElementoInterativo
{
    /** Tipo de plataforma (textura e solidez), ou null se a imagem foi criada à parte. */
    private final TipoPlataforma tipo;
    /** true se bloqueia por todos os lados; false se só se apoia por cima. */
    private final boolean solida;

    /** Plataforma de pedra com 3 blocos (permite criar pelo menu do Greenfoot). */
    public Plataforma()
    {
        this(144, TipoPlataforma.PEDRA);
    }

    /**
     * Plataforma com a altura padrão do tipo (overloading).
     *
     * @param largura Largura em píxeis (ex.: 96)
     * @param tipo Tipo de plataforma (ex.: TipoPlataforma.CAIXOTE)
     */
    public Plataforma(int largura, TipoPlataforma tipo)
    {
        this(largura, tipo.getAlturaPadrao(), tipo);
    }

    /**
     * Plataforma com largura, altura e tipo à escolha. Um tipo invisível fica com uma imagem
     * transparente (só colisão).
     *
     * @param largura Largura em píxeis, mínimo 1 (ex.: 96)
     * @param altura Altura em píxeis, mínimo 1 (ex.: 96, num caixote alto do Cenario1)
     * @param tipo Tipo de plataforma (ex.: TipoPlataforma.CAIXOTE)
     */
    public Plataforma(int largura, int altura, TipoPlataforma tipo)
    {
        this.tipo = tipo;
        this.solida = tipo.isSolida();
        if (tipo.isVisivel()) {
            setImage(criarImagem(largura, altura, tipo.getTexturaTopo(), tipo.getTexturaCorpo()));
        } else {
            setImage(new GreenfootImage(Math.max(1, largura), Math.max(1, altura)));
        }
    }

    /**
     * Plataforma a partir de uma imagem já desenhada (ex.: ficheiros antigos do grupo).
     * Usado pelo construtor Chao(String), que atualmente não é chamado.
     *
     * @param ficheiroImagem Nome do ficheiro na pasta images (ex.: "caixa_pesada.png")
     * @param solida true se bloqueia por todos os lados (ex.: true)
     */
    public Plataforma(String ficheiroImagem, boolean solida)
    {
        this(new GreenfootImage(ficheiroImagem), solida);
    }

    /**
     * Plataforma com uma imagem criada no código (usado por barreiras, portões, caixas...).
     *
     * @param imagem Imagem da plataforma (ex.: new GreenfootImage("caixa_pesada.png") na CaixaPesada)
     * @param solida true se bloqueia por todos os lados (ex.: false na PlataformaFragil)
     */
    protected Plataforma(GreenfootImage imagem, boolean solida)
    {
        this.tipo = null;
        this.solida = solida;
        setImage(imagem);
    }

    /**
     * Monta uma imagem largura x altura repetindo as texturas.
     * protected + static para as subclasses criarem variantes (ex.: runas acesas).
     *
     * @param largura Largura da imagem em píxeis (ex.: 64)
     * @param altura Altura da imagem em píxeis (ex.: 30)
     * @param texturaTopo Ficheiro da textura da primeira linha (ex.: "fragil.png")
     * @param texturaCorpo Ficheiro da textura do resto da imagem (ex.: "fragil.png")
     * @return nova imagem com as texturas repetidas
     */
    protected static GreenfootImage criarImagem(int largura, int altura, String texturaTopo, String texturaCorpo)
    {
        GreenfootImage imagem = new GreenfootImage(Math.max(1, largura), Math.max(1, altura));
        GreenfootImage topo = new GreenfootImage(texturaTopo);
        GreenfootImage corpo = new GreenfootImage(texturaCorpo);
        int y = 0;
        while (y < altura) {
            GreenfootImage textura = (y == 0) ? topo : corpo;
            for (int x = 0; x < largura; x += textura.getWidth()) {
                imagem.drawImage(textura, x, y);
            }
            y += textura.getHeight();
        }
        return imagem;
    }

    /**
     * Indica se a plataforma é sólida: sólida bloqueia por todos os lados; atravessável só por cima.
     *
     * @return true se for sólida
     */
    public boolean isSolida()
    {
        return solida;
    }

    /**
     * Indica se conta para colisões neste momento. Um portão totalmente aberto deixa de contar
     * (overriding em Portao) e uma plataforma frágil desfeita também (PlataformaFragil).
     *
     * @return true por omissão
     */
    public boolean isColidivel()
    {
        return true;
    }

    /**
     * Devolve o tipo da plataforma.
     *
     * @return tipo (ex.: TipoPlataforma.MADEIRA), ou null se a imagem foi criada à parte
     */
    public TipoPlataforma getTipo()
    {
        return tipo;
    }

    /**
     * Indica se a plataforma se move. Uma plataforma estática não se move (redefinido em PlataformaMovel e CaixaPesada).
     *
     * @return false por omissão
     */
    public boolean isMovel()
    {
        return false;
    }

    /**
     * Devolve o deslocamento horizontal no último ciclo (0 numa plataforma estática).
     *
     * @return deslocamento em píxeis
     */
    public int getDeltaX()
    {
        return 0;
    }

    /**
     * Devolve o deslocamento vertical no último ciclo (0 numa plataforma estática).
     *
     * @return deslocamento em píxeis
     */
    public int getDeltaY()
    {
        return 0;
    }
}
