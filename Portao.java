import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira (incluindo o desenho em definirAbertura)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Portão de enrolar: uma parede sólida que sobe (enrola) quando é aberto e desce quando
 * é fechado. Acionado por placas de pressão (interface Mecanismo).
 * A parte visível encolhe de baixo para cima, por isso as colisões acompanham a imagem.
 * Nunca fecha em cima de ninguém.
 *
 * Hierarquia: Portao estende Plataforma, que estende ElementoInterativo, que estende Actor;
 * subclasses: PortaoSelado e Cofre.
 * Conceitos de POO: herança, implementação de interface (Mecanismo), overriding (act,
 * addedToWorld, isColidivel), overloading de construtores (3 versões, uma protegida para subclasses).
 */
public class Portao extends Plataforma implements Mecanismo
{
    /** Velocidade de abertura/fecho, em píxeis por ciclo (4). */
    private static final int VELOCIDADE = 4;
    /** Largura do portão criado por textura, em píxeis (32). */
    private static final int LARGURA = 32;

    /** Imagem do portão completamente fechado. */
    private final GreenfootImage imagemCompleta;
    /** Altura do portão fechado, em píxeis (ex.: 220). */
    private final int alturaTotal;
    /** Quanto já enrolou, em píxeis: 0 = fechado, alturaTotal = totalmente aberto. */
    private int abertura;      // 0 = fechado, alturaTotal = totalmente aberto
    /** Coordenada y do topo, que fica fixo enquanto a parte de baixo sobe. */
    private int topoFixo;
    /** true se o objetivo atual é abrir; false se é fechar. */
    private boolean aAbrir;

    /**
     * Portão de metal com a altura indicada (overloading).
     *
     * @param altura Altura do portão fechado, em píxeis (ex.: 220)
     */
    public Portao(int altura)
    {
        this(altura, TipoPlataforma.PORTAO_METAL);
    }

    /**
     * Portão com 32 px de largura e a textura do tipo indicado.
     *
     * @param altura Altura do portão fechado, em píxeis (ex.: 220, no Cenario1)
     * @param tipo Textura do portão (ex.: TipoPlataforma.PORTAO_METAL)
     */
    public Portao(int altura, TipoPlataforma tipo)
    {
        this(Plataforma.criarImagem(LARGURA, altura, tipo.getTexturaTopo(), tipo.getTexturaCorpo()));
    }

    /**
     * Portão com imagem própria (ex.: a porta do cofre).
     *
     * @param imagem Imagem do portão fechado (ex.: new GreenfootImage("cofre.png") no Cofre)
     */
    protected Portao(GreenfootImage imagem)
    {
        super(new GreenfootImage(imagem), true);
        this.imagemCompleta = imagem;
        this.alturaTotal = imagem.getHeight();
    }

    /**
     * Guarda o topo do portão quando é colocado no mundo (overriding de Actor.addedToWorld).
     *
     * @param mundo Mundo onde foi colocado (ex.: um Cenario1)
     */
    @Override
    protected void addedToWorld(World mundo)
    {
        topoFixo = getTopo();
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine ElementoInterativo.act): abre ou fecha 4 píxeis
     * em direção ao objetivo; ao fechar, espera se houver alguém por baixo.
     */
    public void act()
    {
        int alvo = aAbrir ? alturaTotal : 0;
        if (abertura < alvo) {
            definirAbertura(Math.min(alvo, abertura + VELOCIDADE));
        } else if (abertura > alvo) {
            int nova = Math.max(alvo, abertura - VELOCIDADE);
            if (!haAlguemPorBaixo(nova)) {
                definirAbertura(nova);
            }
        }
    }

    /**
     * Atualiza a abertura: recorta a imagem à parte ainda visível e reposiciona o portão
     * para o topo continuar no mesmo sítio.
     *
     * @param nova Nova abertura em píxeis, de 0 a alturaTotal (ex.: 4)
     */
    private void definirAbertura(int nova)
    {
        abertura = nova;
        int visivel = Math.max(1, alturaTotal - abertura);
        GreenfootImage img = new GreenfootImage(imagemCompleta.getWidth(), visivel);
        img.drawImage(imagemCompleta, 0, -abertura);
        setImage(img);
        setLocation(getX(), topoFixo + visivel / 2);
    }

    /**
     * Verifica se alguma entidade ou caixa ficaria esmagada se o portão descesse até esta abertura.
     *
     * @param novaAbertura Abertura que se quer aplicar, em píxeis (ex.: 216)
     * @return true se houver alguém ou alguma caixa por baixo
     */
    private boolean haAlguemPorBaixo(int novaAbertura)
    {
        int base = topoFixo + alturaTotal - novaAbertura;
        List<EntidadeAnimada> entidades = getWorld().getObjects(EntidadeAnimada.class);
        for (EntidadeAnimada e : entidades) {
            if (e.getDireitaColisao() > getEsquerda() && e.getEsquerdaColisao() < getDireita()
                && e.getTopoColisao() < base && e.getBaseColisao() > topoFixo) {
                return true;
            }
        }
        for (CaixaPesada c : getWorld().getObjects(CaixaPesada.class)) {
            if (c.getDireita() > getEsquerda() && c.getEsquerda() < getDireita() && c.getTopo() < base) {
                return true;
            }
        }
        return false;
    }

    /** Implementa Mecanismo.abrir: começa a abrir (toca o som só quando muda de fechar para abrir). */
    @Override
    public void abrir()
    {
        if (!aAbrir) {
            Audio.tocar("portao.wav");
        }
        aAbrir = true;
    }

    /** Implementa Mecanismo.fechar: começa a fechar. */
    @Override
    public void fechar()
    {
        aAbrir = false;
    }

    /**
     * Implementa Mecanismo.isAberto.
     *
     * @return true se estiver totalmente aberto
     */
    @Override
    public boolean isAberto()
    {
        return abertura >= alturaTotal;
    }

    /**
     * Totalmente aberto (até 2 píxeis do fim) deixa de contar para colisões (overriding de Plataforma).
     *
     * @return true enquanto ainda tapa a passagem
     */
    @Override
    public boolean isColidivel()
    {
        return abertura < alturaTotal - 2;
    }

    /**
     * Indica se o portão está a abrir (ou aberto).
     *
     * @return true se o objetivo atual for abrir
     */
    public boolean isAAbrir()
    {
        return aAbrir;
    }
}
