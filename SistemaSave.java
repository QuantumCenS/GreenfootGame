import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Sistema de gravação (save system). Guarda o progresso num ficheiro de texto
 * "progresso.properties" na pasta pessoal do utilizador (~/.agentes_zigzag/), por isso
 * o progresso sobrevive a fechar o Greenfoot.
 *
 * O que é guardado:
 *  - nivel: o nível onde continuar (checkpoint = início desse nível);
 *  - nivelMaximo: o nível mais avançado desbloqueado (para "Escolher missão" no menu);
 *  - pontuacao, notas, dossiers, neutralizados: o estado da missão nesse checkpoint;
 *  - melhorPontuacao: recorde;
 *  - som e modoDeveloper: preferências.
 *
 * Gravação automática: ao começar cada nível e ao concluí-lo; manual no menu de pausa.
 *
 * Classe utilitária final, só com métodos estáticos (não se criam objetos).
 * Conceitos de POO: construtor privado, encapsulamento (o ficheiro e o formato ficam escondidos
 * atrás de métodos públicos), tratamento de exceções (IOException, NumberFormatException).
 */
public final class SistemaSave
{
    /** Ficheiro de gravação (~/.agentes_zigzag/progresso.properties). */
    private static File ficheiro = new File(new File(System.getProperty("user.home"), ".agentes_zigzag"), "progresso.properties");

    /** Construtor privado: classe só com métodos estáticos. */
    private SistemaSave()
    {
    }

    /**
     * Muda o local do ficheiro de gravação. Pensado para testes, mas o projeto não tem testes
     * e o método não é chamado.
     *
     * @param novo Novo ficheiro (ex.: new File("teste.properties"))
     */
    public static void setFicheiro(File novo)
    {
        ficheiro = novo;
    }

    /**
     * Devolve o ficheiro de gravação.
     *
     * @return ficheiro atual
     */
    public static File getFicheiro()
    {
        return ficheiro;
    }

    //  progresso

    /**
     * Grava o checkpoint: continuar no nível indicado com este estado. Atualiza também o nível
     * máximo desbloqueado e o recorde.
     *
     * @param estado Estado da missão a gravar (ex.: o estado inicial do nível, no checkpoint)
     * @param nivel Nível onde continuar, limitado a 1..5 (ex.: getNumeroNivel() + 1 ao concluir)
     * @return true se gravou com sucesso
     */
    public static boolean guardar(EstadoMissao estado, int nivel)
    {
        Properties p = ler();
        int nivelValido = Math.max(1, Math.min(Niveis.TOTAL, nivel));
        p.setProperty("nivel", String.valueOf(nivelValido));
        int maximo = Math.max(nivelValido, lerInt(p, "nivelMaximo", 1));
        p.setProperty("nivelMaximo", String.valueOf(maximo));
        p.setProperty("pontuacao", String.valueOf(estado.getPontuacao()));
        p.setProperty("neutralizados", String.valueOf(estado.getInimigosNeutralizados()));
        p.setProperty("notas", String.join(",", estado.getIdsNotas()));
        p.setProperty("dossiers", String.join(",", estado.getIdsDossiers()));
        p.setProperty("modoDeveloper", String.valueOf(estado.isModoDeveloper()));
        registarPontuacao(p, estado.getPontuacao());
        return escrever(p);
    }

    /**
     * Fim do jogo: limpa o checkpoint mas mantém o recorde, o nível máximo e as preferências.
     *
     * @param estado Estado final da missão (para atualizar o recorde)
     */
    public static void registarFimDeJogo(EstadoMissao estado)
    {
        Properties p = ler();
        registarPontuacao(p, estado.getPontuacao());
        p.setProperty("nivelMaximo", String.valueOf(Niveis.TOTAL));
        p.remove("nivel");
        escrever(p);
    }

    /**
     * Atualiza o recorde nas propriedades, se os pontos o ultrapassarem.
     *
     * @param p Propriedades lidas do ficheiro
     * @param pontos Pontuação a comparar (ex.: 1250)
     */
    private static void registarPontuacao(Properties p, int pontos)
    {
        if (pontos > lerInt(p, "melhorPontuacao", 0)) {
            p.setProperty("melhorPontuacao", String.valueOf(pontos));
        }
    }

    /**
     * Indica se há um checkpoint gravado.
     *
     * @return true se existir a chave "nivel" no ficheiro
     */
    public static boolean existeProgresso()
    {
        return ler().getProperty("nivel") != null;
    }

    /**
     * Devolve o nível do checkpoint (ou 1 se não houver).
     *
     * @return nível de 1 a 5
     */
    public static int getNivelGuardado()
    {
        return Math.max(1, Math.min(Niveis.TOTAL, lerInt(ler(), "nivel", 1)));
    }

    /**
     * Devolve o nível mais avançado desbloqueado.
     *
     * @return nível de 1 a 5
     */
    public static int getNivelMaximo()
    {
        return Math.max(1, Math.min(Niveis.TOTAL, lerInt(ler(), "nivelMaximo", 1)));
    }

    /**
     * Devolve o recorde gravado.
     *
     * @return melhor pontuação (0 se não houver)
     */
    public static int getMelhorPontuacao()
    {
        return lerInt(ler(), "melhorPontuacao", 0);
    }

    /**
     * Recria o estado da missão gravado (ou um novo, se não houver gravação).
     *
     * @return estado reconstruído com EstadoMissao.restaurar
     */
    public static EstadoMissao carregarEstado()
    {
        Properties p = ler();
        return EstadoMissao.restaurar(lerInt(p, "pontuacao", 0), lerInt(p, "neutralizados", 0),
                                      lista(p.getProperty("notas", "")), lista(p.getProperty("dossiers", "")),
                                      Boolean.parseBoolean(p.getProperty("modoDeveloper", "false")));
    }

    /** Apaga o checkpoint e o recorde (mantém só as preferências). */
    public static void apagarProgresso()
    {
        Properties p = ler();
        for (String chave : new String[] { "nivel", "nivelMaximo", "pontuacao", "neutralizados", "notas", "dossiers", "melhorPontuacao" }) {
            p.remove(chave);
        }
        escrever(p);
    }

    //  preferências

    /**
     * Indica se o som está ligado nas preferências.
     *
     * @return true se ligado (por omissão true)
     */
    public static boolean isSomAtivo()
    {
        return Boolean.parseBoolean(ler().getProperty("som", "true"));
    }

    /**
     * Grava a preferência de som.
     *
     * @param ativo true para som ligado (ex.: Audio.isSomAtivo())
     */
    public static void setSomAtivo(boolean ativo)
    {
        Properties p = ler();
        p.setProperty("som", String.valueOf(ativo));
        escrever(p);
    }

    /**
     * Indica se o modo developer está ligado nas preferências.
     *
     * @return true se ligado (por omissão false)
     */
    public static boolean isModoDeveloper()
    {
        return Boolean.parseBoolean(ler().getProperty("modoDeveloper", "false"));
    }

    /**
     * Grava a preferência do modo developer.
     *
     * @param ativo true para ligar (ex.: !SistemaSave.isModoDeveloper() no botão do menu)
     */
    public static void setModoDeveloper(boolean ativo)
    {
        Properties p = ler();
        p.setProperty("modoDeveloper", String.valueOf(ativo));
        escrever(p);
    }

    //  ficheiro

    /**
     * Lê o ficheiro de gravação.
     *
     * @return propriedades lidas; vazias se o ficheiro não existir ou não se conseguir ler
     */
    private static Properties ler()
    {
        Properties p = new Properties();
        if (ficheiro.exists()) {
            try (InputStream in = new FileInputStream(ficheiro)) {
                p.load(in);
            } catch (IOException e) {
                // ficheiro ilegível: começa do zero
            }
        }
        return p;
    }

    /**
     * Escreve as propriedades no ficheiro (cria a pasta se for preciso).
     *
     * @param p Propriedades a gravar
     * @return true se gravou; false se não houver permissões (o jogo continua sem gravar)
     */
    private static boolean escrever(Properties p)
    {
        try {
            File pasta = ficheiro.getParentFile();
            if (pasta != null && !pasta.exists()) {
                pasta.mkdirs();
            }
            try (OutputStream out = new FileOutputStream(ficheiro)) {
                p.store(out, "Agentes Zig & Zag - progresso");
            }
            return true;
        } catch (IOException e) {
            return false;   // sem permissões: o jogo continua sem gravar
        }
    }

    /**
     * Lê um inteiro das propriedades, com valor por omissão se faltar ou for inválido.
     *
     * @param p Propriedades lidas
     * @param chave Nome da chave (ex.: "nivel")
     * @param omissao Valor a devolver se a chave faltar ou não for um número (ex.: 1)
     * @return valor lido ou o valor por omissão
     */
    private static int lerInt(Properties p, String chave, int omissao)
    {
        try {
            return Integer.parseInt(p.getProperty(chave, String.valueOf(omissao)).trim());
        } catch (NumberFormatException e) {
            return omissao;
        }
    }

    /**
     * Parte um texto separado por vírgulas numa lista, ignorando elementos vazios.
     *
     * @param texto Texto gravado (ex.: "N1-1,N1-3")
     * @return lista de elementos (ex.: ["N1-1", "N1-3"])
     */
    private static List<String> lista(String texto)
    {
        List<String> r = new ArrayList<String>();
        for (String s : texto.split(",")) {
            if (!s.trim().isEmpty()) {
                r.add(s.trim());
            }
        }
        return r;
    }
}
