/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Tipos de ruído que os inimigos conseguem ouvir, com o raio de alcance de cada um.
 * Os ruídos "subtis" (passos, aterragens) só fazem um cultista olhar;
 * os outros fazem-no ir investigar o local. A CriaturaAbissal (cega) persegue todos.
 *
 * Usado por Cenario.emitirRuido() e pelas reações dos inimigos (reagirADistracao).
 * Conceitos de POO: enum com construtor e atributos finais (encapsulamento), getters.
 */
public enum TipoRuido
{
    /** Agente a correr (agachado não faz barulho): raio 160 px, subtil. */
    PASSOS(160, true),
    /** Aterrar de um salto alto: raio 200 px, subtil. */
    ATERRAGEM(200, true),
    /** Caixa ou estátua a ser empurrada: raio 170 px. */
    ARRASTAR(170, false),
    /** Amuleto luminoso no chão (repete enquanto brilha): raio 260 px. */
    AMULETO(260, false),
    /** Cultista que deu o alarme chama os outros: raio 320 px. */
    GRITO(320, false),
    /** Frasco de éter a partir-se: raio 340 px. */
    FRASCO(340, false),
    /** Sino ancestral: raio 800 px. */
    SINO(800, false),
    /** Câmara de vigilância: todo o nível ouve (raio 100000 px). */
    ALARME(100000, false);

    /** Distância máxima a que o ruído é ouvido, em píxeis (ex.: 340). */
    private final int raio;
    /** true se o ruído só faz o cultista olhar (não investigar). */
    private final boolean subtil;

    /**
     * Cria um tipo de ruído.
     *
     * @param raio Alcance do ruído em píxeis (ex.: 340 no FRASCO)
     * @param subtil true se só faz o cultista olhar (ex.: true em PASSOS, false em SINO)
     */
    TipoRuido(int raio, boolean subtil)
    {
        this.raio = raio;
        this.subtil = subtil;
    }

    /**
     * Devolve o alcance do ruído.
     *
     * @return raio em píxeis (ex.: 800 no SINO)
     */
    public int getRaio()       { return raio; }

    /**
     * Indica se o ruído é subtil.
     *
     * @return true em PASSOS e ATERRAGEM; false nos restantes
     */
    public boolean isSubtil()  { return subtil; }
}
