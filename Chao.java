import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Chão principal de um nível: uma plataforma sólida que vai até ao fundo do ecrã.
 * Criado por Cenario.adicionarChao().
 *
 * Hierarquia: Chao estende Plataforma, que estende ElementoInterativo, que estende Actor (3 níveis além de Actor).
 * Conceitos de POO: herança (reutiliza tudo de Plataforma), overloading de construtores (3 versões),
 * uso de super(...) para inicializar a superclasse.
 */
public class Chao extends Plataforma
{
    /** Largura do chão criado pelo construtor sem argumentos, em píxeis (1024, a largura do ecrã). */
    private static final int LARGURA_PADRAO = 1024;
    /** Altura do chão criado pelo construtor sem argumentos, em píxeis (52). */
    private static final int ALTURA_PADRAO = 52;

    /** Chão do cais com a largura do ecrã (para criar pelo menu do Greenfoot). */
    public Chao()
    {
        this(LARGURA_PADRAO, ALTURA_PADRAO, TipoPlataforma.CAIS);
    }

    /**
     * Chão com dimensões e textura à escolha.
     *
     * @param largura Largura em píxeis (ex.: 608, o primeiro troço do cais no Cenario1)
     * @param altura Altura em píxeis (ex.: 52, do topo 520 até ao fundo do ecrã)
     * @param tipo Textura do chão (ex.: TipoPlataforma.CAIS)
     */
    public Chao(int largura, int altura, TipoPlataforma tipo)
    {
        super(largura, altura, tipo);
    }

    /**
     * Chão a partir de um ficheiro PNG já desenhado (construtor original do grupo).
     * Atualmente não é chamado em nenhuma classe do projeto.
     *
     * @param nomeFicheiro Nome do ficheiro na pasta images (ex.: "chao.png")
     */
    public Chao(String nomeFicheiro)
    {
        super(nomeFicheiro, true);
    }
}
