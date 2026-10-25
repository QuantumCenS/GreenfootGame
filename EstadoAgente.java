/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Estados de animação de um agente. Cada estado sabe o nome dos ficheiros de imagem,
 * quantos frames tem e quantos ciclos (act) dura cada frame.
 *
 * Ficheiros esperados na pasta images:  prefixo_nome_0.png, prefixo_nome_1.png, ...
 * (ex.: zig_correr_0.png ... zig_correr_5.png). Todos os frames devem ter o mesmo tamanho.
 *
 * Usado por AgenteInvestigador (um objeto Animacao por estado, guardado num EnumMap).
 * Conceitos de POO: enum com construtor privado e atributos finais (encapsulamento),
 * métodos de consulta (getters).
 */
public enum EstadoAgente
{
    /** Parado em pé: 2 frames, 30 ciclos cada. */
    PARADO("parado", 2, 30, false),
    /** A correr: 6 frames, 5 ciclos cada. */
    CORRER("correr", 6, 5, false),
    /** A subir num salto: 1 frame. */
    SALTAR("saltar", 1, 1, false),
    /** A cair: 1 frame. */
    CAIR("cair", 1, 1, false),
    /** Agachado e parado (modo furtivo): 1 frame. */
    AGACHADO("agachado", 1, 1, false),
    /** Agachado a andar (modo furtivo): 4 frames, 9 ciclos cada. */
    FURTIVO("furtivo", 4, 9, false),
    /** A lançar uma distração: 1 frame. */
    LANCAR("lancar", 1, 1, false),
    /** A planar (opcional: só o Zag tem estas imagens): 2 frames, 8 ciclos cada. */
    PLANAR("planar", 2, 8, true),     // opcionais: só o Zag tem estas imagens...
    /** A hackear um terminal (opcional: só o Zag): 2 frames, 12 ciclos cada. */
    HACKEAR("hackear", 2, 12, true),
    /** A golpear um cultista (opcional: só o Zig tem estas imagens): 3 frames, 8 ciclos cada. */
    ATACAR("atacar", 3, 8, true);     // ...e só o Zig tem estas

    /** Parte do nome do ficheiro de imagem que identifica o estado (ex.: "correr"). */
    private final String nomeFicheiro;
    /** Número de frames da animação (ex.: 6). */
    private final int numeroFrames;
    /** Ciclos de act() que cada frame fica visível (ex.: 5). */
    private final int atrasoFrames;
    /** true se só alguns agentes têm imagens para este estado (não é carregado automaticamente). */
    private final boolean opcional;

    /**
     * Cria um estado de animação com os dados dos respetivos ficheiros de imagem.
     *
     * @param nomeFicheiro Parte do nome dos ficheiros que identifica o estado (ex.: "correr" para zig_correr_0.png)
     * @param numeroFrames Número de frames da animação (ex.: 6)
     * @param atrasoFrames Ciclos de act() que cada frame fica visível (ex.: 5)
     * @param opcional true se só alguns agentes têm estas imagens (ex.: true em PLANAR, só o Zag)
     */
    EstadoAgente(String nomeFicheiro, int numeroFrames, int atrasoFrames, boolean opcional)
    {
        this.nomeFicheiro = nomeFicheiro;
        this.numeroFrames = numeroFrames;
        this.atrasoFrames = atrasoFrames;
        this.opcional = opcional;
    }

    /**
     * Devolve a parte do nome dos ficheiros que identifica o estado.
     *
     * @return nome usado nos ficheiros de imagem (ex.: "correr")
     */
    public String getNomeFicheiro() { return nomeFicheiro; }

    /**
     * Devolve o número de frames da animação deste estado.
     *
     * @return número de frames (ex.: 6 em CORRER)
     */
    public int getNumeroFrames()    { return numeroFrames; }

    /**
     * Devolve quantos ciclos de act() cada frame fica visível.
     *
     * @return atraso entre frames em ciclos (ex.: 5 em CORRER)
     */
    public int getAtrasoFrames()    { return atrasoFrames; }

    /**
     * Indica se o estado é opcional (só alguns agentes têm as imagens).
     *
     * @return true em PLANAR, HACKEAR e ATACAR; false nos restantes
     */
    public boolean isOpcional()     { return opcional; }

    /**
     * Verifica se este é um estado em que o agente está agachado (furtividade).
     *
     * @return true em AGACHADO e FURTIVO; false nos restantes
     */
    public boolean isAgachado()
    {
        return this == AGACHADO || this == FURTIVO;
    }
}
