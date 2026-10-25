import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/*
 * NOTA – USO DE IA
 * Foi utilizado Claude (Anthropic) aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Lógica do mini-jogo de hacking (sem nada de gráfico — fácil de testar).
 *
 * Uma grelha de peças de circuito; cada peça liga alguns dos seus lados (N, E, S, O).
 * O sinal entra pela esquerda numa linha e tem de sair pela direita noutra linha.
 * O jogador roda as peças (90° no sentido horário) até o circuito ficar ligado.
 *
 * Geração garantidamente resolúvel: primeiro cria um caminho aleatório da entrada à
 * saída (procura em profundidade), põe as peças certas ao longo dele, enche o resto com
 * peças aleatórias e só depois baralha as rotações de todas.
 *
 * Cada peça é guardada como uma máscara de bits: N=1, E=2, S=4, O=8.
 *
 * Classe auxiliar (não é um Actor), usada pelo PainelHacking (separação entre lógica e desenho).
 * Conceitos de POO: encapsulamento (estado privado, alterado só por rodar()), overloading de
 * construtores (2 públicos e 1 privado), constantes públicas estáticas.
 */
public class PuzzleCircuito
{
    /** Bit do lado norte (1). */
    public static final int NORTE = 1;
    /** Bit do lado este (2). */
    public static final int ESTE = 2;
    /** Bit do lado sul (4). */
    public static final int SUL = 4;
    /** Bit do lado oeste (8). */
    public static final int OESTE = 8;

    /** Deslocamento em x de cada direção, pela ordem N, E, S, O. */
    private static final int[] DX = { 0, 1, 0, -1 };   // N, E, S, O
    /** Deslocamento em y de cada direção, pela ordem N, E, S, O. */
    private static final int[] DY = { -1, 0, 1, 0 };
    /** Bit de cada direção, pela ordem N, E, S, O. */
    private static final int[] BIT = { NORTE, ESTE, SUL, OESTE };

    /** Número de colunas da grelha (ex.: 5). */
    private final int colunas;
    /** Número de linhas da grelha (ex.: 4). */
    private final int linhas;
    /** Máscara de cada peça, indexada por [coluna][linha]. */
    private final int[][] pecas;          // [coluna][linha]
    /** Peças que recebem o sinal, indexadas por [coluna][linha]. */
    private final boolean[][] energia;
    /** Linha por onde o sinal entra (à esquerda). */
    private final int linhaEntrada;
    /** Linha por onde o sinal tem de sair (à direita). */
    private final int linhaSaida;
    /** Gerador de números aleatórios. */
    private final Random aleatorio;

    /**
     * Puzzle aleatório (overloading).
     *
     * @param colunas Número de colunas, mínimo 2 (ex.: 5)
     * @param linhas Número de linhas, mínimo 2 (ex.: 4)
     */
    public PuzzleCircuito(int colunas, int linhas)
    {
        this(colunas, linhas, new Random());
    }

    /**
     * Puzzle reproduzível a partir de uma semente (útil para testes; atualmente não é chamado no projeto).
     *
     * @param colunas Número de colunas, mínimo 2 (ex.: 5)
     * @param linhas Número de linhas, mínimo 2 (ex.: 4)
     * @param semente Semente do gerador aleatório (ex.: 42L)
     */
    public PuzzleCircuito(int colunas, int linhas, long semente)
    {
        this(colunas, linhas, new Random(semente));
    }

    /**
     * Construtor comum: escolhe as linhas de entrada e saída e gera o puzzle.
     *
     * @param colunas Número de colunas, mínimo 2 (ex.: 6)
     * @param linhas Número de linhas, mínimo 2 (ex.: 5)
     * @param aleatorio Gerador de números aleatórios a usar (ex.: new Random())
     * @throws IllegalArgumentException se a grelha for menor do que 2x2
     */
    private PuzzleCircuito(int colunas, int linhas, Random aleatorio)
    {
        if (colunas < 2 || linhas < 2) {
            throw new IllegalArgumentException("A grelha precisa de pelo menos 2x2");
        }
        this.colunas = colunas;
        this.linhas = linhas;
        this.aleatorio = aleatorio;
        this.pecas = new int[colunas][linhas];
        this.energia = new boolean[colunas][linhas];
        this.linhaEntrada = aleatorio.nextInt(linhas);
        this.linhaSaida = aleatorio.nextInt(linhas);
        gerar();
    }

    //  geração

    /**
     * Gera o puzzle: peças certas ao longo de um caminho aleatório, peças aleatórias no resto
     * e, no fim, rotações baralhadas.
     *
     * @throws IllegalStateException se a solução gerada não ligar o circuito (não deve acontecer)
     */
    private void gerar()
    {
        List<int[]> caminho = criarCaminho();
        boolean[][] noCaminho = new boolean[colunas][linhas];
        for (int i = 0; i < caminho.size(); i++) {
            int[] atual = caminho.get(i);
            int mascara = 0;
            mascara |= (i == 0) ? OESTE : ladoEntre(atual, caminho.get(i - 1));
            mascara |= (i == caminho.size() - 1) ? ESTE : ladoEntre(atual, caminho.get(i + 1));
            pecas[atual[0]][atual[1]] = mascara;
            noCaminho[atual[0]][atual[1]] = true;
        }
        int[] aleatorias = { 5, 3, 7, 5, 3, 6 };   // retas, curvas e "T"
        for (int c = 0; c < colunas; c++) {
            for (int l = 0; l < linhas; l++) {
                if (!noCaminho[c][l]) {
                    pecas[c][l] = aleatorias[aleatorio.nextInt(aleatorias.length)];
                }
            }
        }
        recalcularEnergia();
        if (!isResolvido()) {
            throw new IllegalStateException("Erro interno: a solução gerada não liga o circuito");
        }
        baralhar(caminho);
    }

    /**
     * Cria um caminho aleatório sem repetições da entrada (coluna 0) à saída (última coluna).
     *
     * @return lista de células {coluna, linha}, da entrada à saída
     */
    private List<int[]> criarCaminho()
    {
        boolean[][] visitado = new boolean[colunas][linhas];
        List<int[]> caminho = new ArrayList<int[]>();
        procurar(0, linhaEntrada, visitado, caminho);
        return caminho;
    }

    /**
     * Procura em profundidade (recursiva, com direções baralhadas) um caminho da célula (c, l) até à saída.
     *
     * @param c Coluna atual (ex.: 0 na primeira chamada)
     * @param l Linha atual (ex.: linhaEntrada na primeira chamada)
     * @param visitado Células já visitadas (alterado pelo método)
     * @param caminho Caminho construído até aqui (alterado pelo método)
     * @return true se encontrou a saída a partir desta célula
     */
    private boolean procurar(int c, int l, boolean[][] visitado, List<int[]> caminho)
    {
        visitado[c][l] = true;
        caminho.add(new int[] { c, l });
        if (c == colunas - 1 && l == linhaSaida) {
            return true;
        }
        List<Integer> direcoes = new ArrayList<Integer>();
        for (int d = 0; d < 4; d++) {
            direcoes.add(d);
        }
        Collections.shuffle(direcoes, aleatorio);
        for (int d : direcoes) {
            int nc = c + DX[d];
            int nl = l + DY[d];
            if (nc >= 0 && nc < colunas && nl >= 0 && nl < linhas && !visitado[nc][nl]) {
                if (procurar(nc, nl, visitado, caminho)) {
                    return true;
                }
            }
        }
        caminho.remove(caminho.size() - 1);
        return false;
    }

    /**
     * Devolve o bit do lado da célula "de" que está virado para a célula vizinha "para".
     *
     * @param de Célula de origem {coluna, linha} (ex.: {0, 2})
     * @param para Célula vizinha {coluna, linha} (ex.: {1, 2})
     * @return bit do lado (ex.: ESTE = 2 para os valores acima)
     * @throws IllegalArgumentException se as células não forem vizinhas
     */
    private int ladoEntre(int[] de, int[] para)
    {
        for (int d = 0; d < 4; d++) {
            if (de[0] + DX[d] == para[0] && de[1] + DY[d] == para[1]) {
                return BIT[d];
            }
        }
        throw new IllegalArgumentException("Células não vizinhas");
    }

    /**
     * Roda todas as peças ao acaso, garantindo que o puzzle não começa resolvido.
     *
     * @param caminho Caminho da solução (só se rodam peças dele enquanto o puzzle estiver resolvido)
     */
    private void baralhar(List<int[]> caminho)
    {
        for (int c = 0; c < colunas; c++) {
            for (int l = 0; l < linhas; l++) {
                int voltas = aleatorio.nextInt(4);
                for (int v = 0; v < voltas; v++) {
                    pecas[c][l] = rodarMascara(pecas[c][l]);
                }
            }
        }
        recalcularEnergia();
        while (isResolvido()) {
            int[] celula = caminho.get(aleatorio.nextInt(caminho.size()));
            rodar(celula[0], celula[1]);
        }
    }

    //  jogo

    /**
     * Roda uma máscara 90° no sentido horário: N->E->S->O->N.
     *
     * @param mascara Máscara de lados ligados (ex.: 5, reta vertical N+S)
     * @return máscara rodada (ex.: 10, reta horizontal E+O)
     */
    public static int rodarMascara(int mascara)
    {
        return ((mascara << 1) | (mascara >> 3)) & 15;
    }

    /**
     * Roda a peça na posição indicada e atualiza o circuito (posições fora da grelha são ignoradas).
     *
     * @param coluna Coluna da peça (ex.: a coluna do cursor do PainelHacking)
     * @param linha Linha da peça (ex.: a linha do cursor do PainelHacking)
     */
    public void rodar(int coluna, int linha)
    {
        if (coluna < 0 || coluna >= colunas || linha < 0 || linha >= linhas) {
            return;
        }
        pecas[coluna][linha] = rodarMascara(pecas[coluna][linha]);
        recalcularEnergia();
    }

    /** Percorre o circuito a partir da entrada (pesquisa em largura) e marca as peças ligadas. */
    private void recalcularEnergia()
    {
        for (boolean[] coluna : energia) {
            java.util.Arrays.fill(coluna, false);
        }
        if ((pecas[0][linhaEntrada] & OESTE) == 0) {
            return;
        }
        List<int[]> fila = new ArrayList<int[]>();
        fila.add(new int[] { 0, linhaEntrada });
        energia[0][linhaEntrada] = true;
        while (!fila.isEmpty()) {
            int[] atual = fila.remove(0);
            int mascara = pecas[atual[0]][atual[1]];
            for (int d = 0; d < 4; d++) {
                int nc = atual[0] + DX[d];
                int nl = atual[1] + DY[d];
                int oposto = BIT[(d + 2) % 4];
                if ((mascara & BIT[d]) != 0 && nc >= 0 && nc < colunas && nl >= 0 && nl < linhas
                    && !energia[nc][nl] && (pecas[nc][nl] & oposto) != 0) {
                    energia[nc][nl] = true;
                    fila.add(new int[] { nc, nl });
                }
            }
        }
    }

    /**
     * Verifica se o puzzle está resolvido: a peça de saída tem energia e liga ao lado direito.
     *
     * @return true se o circuito estiver ligado da entrada à saída
     */
    public boolean isResolvido()
    {
        return energia[colunas - 1][linhaSaida] && (pecas[colunas - 1][linhaSaida] & ESTE) != 0;
    }

    /**
     * Devolve a máscara da peça numa posição.
     *
     * @param coluna Coluna (ex.: 0)
     * @param linha Linha (ex.: 2)
     * @return máscara de lados ligados (N=1, E=2, S=4, O=8)
     */
    public int getPeca(int coluna, int linha)        { return pecas[coluna][linha]; }

    /**
     * Indica se a peça numa posição recebe o sinal.
     *
     * @param coluna Coluna (ex.: 0)
     * @param linha Linha (ex.: 2)
     * @return true se a peça estiver ligada à entrada
     */
    public boolean isEnergizada(int coluna, int linha) { return energia[coluna][linha]; }

    /**
     * Devolve o número de colunas.
     *
     * @return colunas da grelha (ex.: 5)
     */
    public int getColunas()      { return colunas; }

    /**
     * Devolve o número de linhas.
     *
     * @return linhas da grelha (ex.: 4)
     */
    public int getLinhas()       { return linhas; }

    /**
     * Devolve a linha de entrada do sinal.
     *
     * @return índice da linha de entrada (à esquerda)
     */
    public int getLinhaEntrada() { return linhaEntrada; }

    /**
     * Devolve a linha de saída do sinal.
     *
     * @return índice da linha de saída (à direita)
     */
    public int getLinhaSaida()   { return linhaSaida; }
}
