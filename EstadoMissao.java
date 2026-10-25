import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Estado da missão que passa de nível para nível: sanidade partilhada (as "vidas"),
 * pontuação, notas de lore recolhidas, inimigos neutralizados e ficheiros classificados.
 *
 * Pontuação:
 *  - cada nota de lore encontrada: +100 (fonte principal de pontos; recolher é opcional);
 *  - cada cultista neutralizado: +25 (o enunciado pede pontos por inimigos derrotados).
 *
 * Modo developer: a sanidade nunca desce (vidas infinitas), para testar os níveis.
 *
 * Classe auxiliar (não é um Actor nem um World).
 * Conceitos de POO: encapsulamento (atributos privados alterados só por métodos com validação,
 * ex.: setVidaSanidade), cópia defensiva (copia) e coleções só de leitura (getIdsNotas),
 * método de fábrica estático (restaurar).
 */
public class EstadoMissao
{
    /** Sanidade máxima e inicial em cada nível (5 corações). */
    public static final int SANIDADE_MAXIMA = 5;
    /** Pontos por cada nota de lore (100). */
    public static final int PONTOS_POR_NOTA = 100;
    /** Pontos por cada cultista neutralizado (25). */
    public static final int PONTOS_POR_NEUTRALIZACAO = 25;

    /** Sanidade atual, de 0 a 5. */
    private int vidaSanidade;
    /** Pontuação acumulada. */
    private int pontuacao;
    /** Número total de inimigos neutralizados. */
    private int inimigosNeutralizados;
    /** true se o ficheiro classificado do nível atual já foi obtido. */
    private boolean ficheiroObtido;
    /** true em modo developer (sanidade não desce). */
    private boolean modoDeveloper;
    /** Ids das notas recolhidas, pela ordem de recolha (ex.: "N1-1"). */
    private final Set<String> notasRecolhidas;
    /** Ids dos ficheiros classificados obtidos (ex.: "D1"). */
    private final Set<String> dossiers;

    /** Cria um estado novo: sanidade cheia, sem pontos, notas nem ficheiros. */
    public EstadoMissao()
    {
        vidaSanidade = SANIDADE_MAXIMA;
        notasRecolhidas = new LinkedHashSet<String>();
        dossiers = new LinkedHashSet<String>();
    }

    /**
     * Cria uma cópia independente (usada para recomeçar um nível com o estado que tinha à entrada).
     *
     * @return novo objeto com os mesmos valores e coleções copiadas
     */
    public EstadoMissao copia()
    {
        EstadoMissao c = new EstadoMissao();
        c.vidaSanidade = vidaSanidade;
        c.pontuacao = pontuacao;
        c.inimigosNeutralizados = inimigosNeutralizados;
        c.ficheiroObtido = ficheiroObtido;
        c.modoDeveloper = modoDeveloper;
        c.notasRecolhidas.addAll(notasRecolhidas);
        c.dossiers.addAll(dossiers);
        return c;
    }

    /** Novo nível: sanidade cheia e ainda sem o ficheiro deste nível. */
    public void iniciarNivel()
    {
        vidaSanidade = SANIDADE_MAXIMA;
        ficheiroObtido = false;
    }

    //  sanidade

    /**
     * Devolve a sanidade atual.
     *
     * @return sanidade, de 0 a 5
     */
    public int getVidaSanidade()
    {
        return vidaSanidade;
    }

    /**
     * Define a sanidade. Validação: a sanidade nunca fica negativa nem acima do máximo.
     *
     * @param valor Nova sanidade (ex.: 4); é limitada ao intervalo 0..5
     */
    public void setVidaSanidade(int valor)
    {
        vidaSanidade = Math.max(0, Math.min(SANIDADE_MAXIMA, valor));
    }

    /**
     * Retira sanidade. Em modo developer a sanidade não desce (vidas infinitas).
     *
     * @param quantidade Sanidade a retirar; valores negativos contam como 0 (ex.: 1)
     */
    public void perderSanidade(int quantidade)
    {
        if (!modoDeveloper) {
            setVidaSanidade(vidaSanidade - Math.max(0, quantidade));
        }
    }

    /**
     * Indica se os agentes enlouqueceram (game over do nível).
     *
     * @return true se a sanidade chegou a 0
     */
    public boolean isInsano()
    {
        return vidaSanidade <= 0;
    }

    //  pontuação

    /**
     * Devolve a pontuação acumulada.
     *
     * @return pontos
     */
    public int getPontuacao()
    {
        return pontuacao;
    }

    /**
     * Soma pontos à pontuação (valores negativos são ignorados).
     *
     * @param pontos Pontos a somar (ex.: 200, o bónus do Sumo-Sacerdote)
     */
    public void adicionarPontos(int pontos)
    {
        pontuacao += Math.max(0, pontos);
    }

    /**
     * Regista uma nota recolhida e soma 100 pontos, se ainda não a tinha.
     *
     * @param id Identificador da nota (ex.: "N1-1")
     * @return true se a nota ainda não tinha sido recolhida
     */
    public boolean registarNota(String id)
    {
        if (notasRecolhidas.add(id)) {
            adicionarPontos(PONTOS_POR_NOTA);
            return true;
        }
        return false;
    }

    /**
     * Indica se uma nota já foi recolhida.
     *
     * @param id Identificador da nota (ex.: "N2-3")
     * @return true se já foi recolhida
     */
    public boolean temNota(String id)
    {
        return notasRecolhidas.contains(id);
    }

    /**
     * Conta as notas recolhidas num nível (ids "N1-...", "N2-...").
     *
     * @param nivel Número do nível (ex.: 1)
     * @return número de notas desse nível já recolhidas (ex.: 3)
     */
    public int getNotasRecolhidas(int nivel)
    {
        int total = 0;
        for (String id : notasRecolhidas) {
            if (id.startsWith("N" + nivel + "-")) {
                total++;
            }
        }
        return total;
    }

    /**
     * Devolve o número total de notas recolhidas em todos os níveis.
     *
     * @return total de notas
     */
    public int getTotalNotasRecolhidas()
    {
        return notasRecolhidas.size();
    }

    /** Regista um inimigo neutralizado e soma 25 pontos. */
    public void registarNeutralizacao()
    {
        inimigosNeutralizados++;
        adicionarPontos(PONTOS_POR_NEUTRALIZACAO);
    }

    /**
     * Devolve o número total de inimigos neutralizados.
     *
     * @return inimigos neutralizados desde o início do jogo
     */
    public int getInimigosNeutralizados()
    {
        return inimigosNeutralizados;
    }

    //  ficheiro classificado

    /**
     * Regista o ficheiro classificado do nível (a extração passa a estar disponível).
     *
     * @param idDossier Identificador do dossier (ex.: "D1")
     */
    public void registarFicheiro(String idDossier)
    {
        ficheiroObtido = true;
        dossiers.add(idDossier);
    }

    /**
     * Indica se o ficheiro do nível atual já foi obtido.
     *
     * @return true se já foi obtido
     */
    public boolean isFicheiroObtido()
    {
        return ficheiroObtido;
    }

    /**
     * Devolve o número de ficheiros classificados obtidos.
     *
     * @return número de dossiers (0 a 5)
     */
    public int getNumeroDossiers()
    {
        return dossiers.size();
    }

    //  modo developer

    /**
     * Indica se o modo developer está ligado.
     *
     * @return true se estiver ligado
     */
    public boolean isModoDeveloper()
    {
        return modoDeveloper;
    }

    /**
     * Liga ou desliga o modo developer; ao ligar, repõe a sanidade máxima.
     *
     * @param ativo true para ligar (ex.: SistemaSave.isModoDeveloper())
     */
    public void setModoDeveloper(boolean ativo)
    {
        modoDeveloper = ativo;
        if (ativo) {
            vidaSanidade = SANIDADE_MAXIMA;
        }
    }

    //  gravação (SistemaSave)

    /**
     * Devolve os ids das notas recolhidas (só leitura).
     *
     * @return conjunto não modificável de ids
     */
    public Set<String> getIdsNotas()
    {
        return Collections.unmodifiableSet(notasRecolhidas);
    }

    /**
     * Devolve os ids dos ficheiros classificados obtidos (só leitura).
     *
     * @return conjunto não modificável de ids
     */
    public Set<String> getIdsDossiers()
    {
        return Collections.unmodifiableSet(dossiers);
    }

    /**
     * Reconstrói um estado gravado em disco (método de fábrica estático: cria o objeto com o
     * construtor sem argumentos e preenche-o; não é overloading do construtor).
     * Os valores são validados (pontos e neutralizações negativos são ignorados; só se aceitam
     * notas que existam em ArquivoLore).
     *
     * @param pontuacao Pontuação gravada (ex.: 425)
     * @param neutralizados Inimigos neutralizados gravados (ex.: 3)
     * @param notas Ids das notas gravadas (ex.: ["N1-1", "N1-3"])
     * @param idsDossiers Ids dos dossiers gravados (ex.: ["D1"])
     * @param developer true se o modo developer estava ligado (ex.: false)
     * @return novo estado com os valores gravados (sanidade cheia)
     */
    public static EstadoMissao restaurar(int pontuacao, int neutralizados, Collection<String> notas,
                                         Collection<String> idsDossiers, boolean developer)
    {
        EstadoMissao e = new EstadoMissao();
        e.adicionarPontos(pontuacao);
        e.inimigosNeutralizados = Math.max(0, neutralizados);
        for (String id : notas) {
            if (ArquivoLore.existeNota(id)) {
                e.notasRecolhidas.add(id);
            }
        }
        e.dossiers.addAll(idsDossiers);
        e.modoDeveloper = developer;
        return e;
    }
}
