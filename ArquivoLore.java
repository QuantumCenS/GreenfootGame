/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira (textos da história)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Todos os textos da história: prólogo, briefings, notas espalhadas pelos níveis,
 * ficheiros classificados, ecrã de insanidade e epílogo (final em aberto).
 * Ficam aqui juntos para serem fáceis de rever.
 *
 * A HISTÓRIA — Operação Sombras de Zanarkin
 *  1. Docas: a Conserveira Atlântida esconde a Ordem Abissal. Manifesto "Maré Negra".
 *  2. Catacumbas: o Códice do Selo explica o contra-rito, mas faltam as páginas do Nome.
 *  3. Biblioteca: o Reitor Abel Corvo é o Sumo-Sacerdote; o agente Vasco Rosa é o "Vaso".
 *  4. Observatório: o ritual é esta noite, às 03:33, no Coração da Fossa.
 *  5. Fossa: o Relatório Ómega-Zero revela o preço do Selo. Os agentes reforjam-no...
 *     mas o epílogo deixa a pergunta: quem ficou do lado de dentro?
 *
 * Classe utilitária final, só com constantes e métodos estáticos (não se criam objetos).
 * Conceitos de POO: construtor privado, encapsulamento (as tabelas de textos são privadas e só
 * se acede a elas pelos métodos de consulta), constantes públicas estáticas.
 */
public final class ArquivoLore
{
    /** Construtor privado: classe só com dados e métodos estáticos. */
    private ArquivoLore()
    {
    }

    /** Notas de lore, uma por linha: { id, título, texto } (ids "N1-1" a "N5-5", 5 por nível). */
    private static final String[][] NOTAS = {
        // ---------------------------------------------------------------- Nível 1 — Docas
        { "N1-1", "Diário de um pescador — 3 de março",
          "As redes vieram cheias outra vez, mas não de peixe que eu conheça. Escamas negras como alcatrão "
          + "e olhos a mais, olhos onde não devia haver olhos. Os homens da Conserveira Atlântida pagaram o "
          + "dobro por cada um. Não perguntaram de onde vinham. Pareciam já saber." },
        { "N1-2", "Memorando interno — Conserveira Atlântida",
          "A TODO O PESSOAL DO TURNO DA NOITE: é proibido descer ao Tanque 4. Os cânticos que se ouvem no "
          + "cais são ruído das máquinas. Quem fizer perguntas será transferido para o turno das catacumbas. "
          + "— A Administração" },
        { "N1-3", "Carta de uma mãe",
          "Tomás, meu filho. Desde que aceitaste o trabalho na fábrica chegas de madrugada a cheirar a "
          + "salmoura e cantarolas uma canção sem vogais. Ontem não voltaste. Deixaram-me à porta um "
          + "envelope com dinheiro e uma escama negra. Por favor, volta." },
        { "N1-4", "Relatório do Agente Vasco Rosa — Divisão Ómega",
          "Infiltrei-me na conserveira. Os símbolos nos contentores coincidem com os mapas das catacumbas "
          + "sob a capital. Desconfiem das câmaras: eles veem através delas. E há algo que vê através deles. "
          + "Se eu não regressar, continuem pelo Tanque 4." },
        { "N1-5", "Página arrancada de um hinário",
          "Quando a maré recuar até ao osso e as estrelas tomarem o seu lugar, Nhal'Vothrum, Aquele que "
          + "Sonha Sob a Fossa, abrirá o olho que nunca fechou. E os que O servirem verão. E os que não O "
          + "servirem serão vistos." },
        // ---------------------------------------------------------------- Nível 2 — Catacumbas
        { "N2-1", "Inscrição gravada na pedra",
          "OS PRIMEIROS DE ZANARKIN NÃO ESCAVARAM ESTES TÚNEIS. ENCONTRARAM-NOS. E O QUE OS ESCAVOU AINDA "
          + "AQUI ESTÁ, EM BAIXO, À ESPERA QUE ALGUÉM SE LEMBRE DO SEU NOME." },
        { "N2-2", "Caderno de Vasco Rosa (II)",
          "Conto os passos. Trezentos e doze até à câmara das estátuas. No regresso, quatrocentos e trinta. "
          + "Os corredores são mais compridos quando se volta para trás. As estátuas viram-se quando não "
          + "estou a olhar. Tenho a certeza. Quase a certeza." },
        { "N2-3", "Ordem do Sumo-Sacerdote",
          "Os iniciados que perderam os olhos durante a Vigília não serão punidos. Serão entregues ao fosso "
          + "e regressarão como crias: sem olhos e sem nome, mas com ouvidos para escutar até o mais leve "
          + "passo. Nada se perde no culto." },
        { "N2-4", "Notas do Dr. Elias Mendonça, geólogo",
          "Medição n.º 44: a cavidade é maior do que a ilha que a contém. A bússola aponta para baixo. O "
          + "chão da fenda não é rocha — move-se num ritmo de quarenta segundos, como uma respiração. As "
          + "estrelas que se veem através dela não constam de nenhum atlas." },
        { "N2-5", "Caderno de Vasco Rosa (III)",
          "Encontrei o Selo. Os cultistas não o estão a forçar: o Selo está a ser aberto por dentro, e eles "
          + "só lhe abrem caminho. Ouço-O sonhar. Sonha connosco. Zig, Zag, se lerem isto: não confiem no "
          + "que virem depois de olharem para a fenda." },
        // ---------------------------------------------------------------- Nível 3 — Biblioteca
        { "N3-1", "Regulamento da Sala de Reservados",
          "Os volumes da secção 0-Ω não podem ser requisitados, lidos em voz alta ou deixados abertos depois "
          + "do pôr do sol. Os leitores que ouvirem as páginas a virar sozinhas devem abandonar a sala sem "
          + "olhar para trás. — O Reitor" },
        { "N3-2", "Diário de uma estudante de Teologia",
          "Hoje vi o Reitor Corvo na sala proibida. Não tinha livro nas mãos — lia as paredes. Quando me viu, "
          + "sorriu com demasiados dentes e convidou-me para o grupo de estudo das quintas-feiras. Disse que "
          + "não. Acho que disse que não." },
        { "N3-3", "Ficha de requisição (rasgada)",
          "Leitor: V. ROSA. Obra: «Pseudo-Cartografia da Fossa», cota 0-Ω-17. Observações do bibliotecário: "
          + "o leitor devolveu o livro com as margens cheias de desenhos de olhos. Insiste que era o livro "
          + "que o andava a ler a ele." },
        { "N3-4", "Carta do bibliotecário-chefe",
          "Querida Inês, os guardas novos não dormem nem falam. A biblioteca cresce de noite: hoje contei "
          + "mais um corredor do que ontem. Se eu deixar de escrever, não venhas procurar-me entre as "
          + "estantes. Há coisas que se arrumam sozinhas." },
        { "N3-5", "Sermão das quintas-feiras",
          "Irmãos: a maré-morta aproxima-se. O Vaso já foi escolhido — um homem da Divisão que veio procurar "
          + "a verdade e a encontrou. Quando o Nome for dito na Fossa, ele abrirá a porta por dentro, e nós "
          + "entraremos." },
        // ---------------------------------------------------------------- Nível 4 — Observatório
        { "N4-1", "Aviso na estrada da montanha",
          "ESTRADA ENCERRADA. Queda de rochas e neve. Por ordem do Observatório, nenhum veículo deve subir "
          + "depois do anoitecer. (Por baixo, escrito a giz:) as estrelas aqui estão erradas." },
        { "N4-2", "Registo do radioamador",
          "02:13 — sinal na frequência morta, vindo de BAIXO do mar. Uma voz em código Ómega repete: «aqui "
          + "Rosa, aqui Rosa, não venham». 02:14 — a mesma voz, a rir. 02:15 — desliguei o rádio. "
          + "Continua a falar." },
        { "N4-3", "Diário do astrónomo Duarte Lemos",
          "Há trinta anos que mapeio o céu de Zanarkin. Esta noite apareceram sete estrelas novas, alinhadas "
          + "sobre a Fossa como um olho a abrir. O Reitor diz que é a conjunção. Eu digo que não são "
          + "estrelas. Mexem-se quando ninguém as observa." },
        { "N4-4", "Cálculos rabiscados",
          "Maré-morta: 03:33. Conjunção: 03:33. Respiração do chão da fenda: 40 s — agora 20 s — agora 10 s. "
          + "Está a acordar mais depressa do que previmos. O Vaso tem de estar no limiar antes das 03:00." },
        { "N4-5", "Bilhete escondido no telescópio",
          "Se a Divisão chegar até aqui: o contra-rito funciona, mas só com dois portadores que confiem um no "
          + "outro sem reservas. Um só, e o Selo leva-o. Foi assim em 1887. Procurem o Relatório Ómega-Zero. "
          + "— D.L." },
        // ---------------------------------------------------------------- Nível 5 — Fossa
        { "N5-1", "Marcas na rocha",
          "Riscos contados em grupos de cinco. Centenas. Por cima, gravado com uma unha: «dias sem sol». "
          + "A última marca é recente. Ainda tem pó de pedra." },
        { "N5-2", "Oração do iniciado",
          "Que o olho se abra. Que a maré recue. Que os portadores falhem. Que o Vaso se parta e Ele beba de "
          + "nós como o mar bebe a chuva." },
        { "N5-3", "Página do Relatório Ómega-Zero (1887)",
          "Os agentes C. e M. desceram à Fossa às 03:00. Às 03:33 o Selo fechou-se. Às 04:10 o agente M. "
          + "regressou à superfície, sozinho e sem memória do parceiro. Nunca mais falou. Fundou-se nesse dia "
          + "a Divisão Ómega." },
        { "N5-4", "Ordem final do Sumo-Sacerdote",
          "Que ninguém toque no Vaso. Que ninguém se aproxime do limiar além de mim. Se dois estranhos "
          + "chegarem juntos, separem-nos. Um portador sozinho não é ameaça: é alimento." },
        { "N5-5", "Última mensagem de Vasco Rosa",
          "Zig. Zag. Eu abri a porta porque Ele me mostrou o que está do outro lado: não é um deus, é um "
          + "prisioneiro. E nós somos os carcereiros. Fechem-na. Mas antes de fecharem, perguntem-se quem está "
          + "realmente do lado de dentro." },
    };

    /** { id, título, texto } dos ficheiros classificados (objetivo de cada nível; ids "D1" a "D5"). */
    private static final String[][] DOSSIERS = {
        { "D1", "PROCESSO 7-B — MANIFESTO \"MARÉ NEGRA\"",
          "Registos de carga da Conserveira Atlântida: 214 caixas de «conserva especial» enviadas para as "
          + "catacumbas sob a capital. Lista de 37 pescadores desaparecidos. Data do ritual: a próxima "
          + "maré-morta. Acesso às catacumbas: alçapão do Tanque 4, atrás do cofre." },
        { "D2", "O CÓDICE DO SELO",
          "O ritual da Ordem Abissal exige que o Selo Cósmico seja aberto na maré-morta. O contra-rito existe: "
          + "o Selo pode ser reforjado por dois portadores, lado a lado, no limiar da fenda, se pronunciarem o "
          + "Nome. As páginas do Nome foram arrancadas. Carimbo na contracapa: «Biblioteca da Universidade de "
          + "Zanarkin — Reservados, cota 0-Ω»." },
        { "D3", "LISTA DE INICIADOS DA ORDEM ABISSAL",
          "Sumo-Sacerdote: Prof. Abel Corvo, Reitor da Universidade de Zanarkin. Arquivista: [nome riscado]. "
          + "Vaso: Agente Vasco Rosa, Divisão Ómega. Anexo: as páginas do Nome seguiram para o Observatório "
          + "do Pico Negro, onde os astrónomos da Ordem calculam a hora exata da conjunção." },
        { "D4", "CARTA DA CONJUNÇÃO",
          "Hora do ritual: 03:33 desta noite. Local: o Coração da Fossa, sob as catacumbas. Oficiante: o "
          + "Sumo-Sacerdote Abel Corvo. O Vaso será colocado no limiar. Nota à margem, noutra caligrafia: a "
          + "Divisão Ómega selou a porta em 1887 com dois agentes. Só um regressou." },
        { "D5", "RELATÓRIO ÓMEGA-ZERO (ORIGINAL, 1887)",
          "Selo Cósmico reforjado pelos agentes C. e M. O agente C. permaneceu no limiar como garantia do "
          + "Selo. Recomendação: o próximo par de portadores deve ser escolhido entre agentes que nunca se "
          + "separem, em nenhuma circunstância. Classificação: Ómega. Destruir após leitura." },
    };

    /** Briefing antes de cada missão (índice 0 = missão 1). */
    private static final String[] BRIEFINGS = {
        "Infiltrem-se na Conserveira Atlântida, nas docas da capital, e encontrem o manifesto de carga "
        + "da Ordem Abissal. Cuidado com as câmaras: dizem que algo vê através delas.",
        "O alçapão do Tanque 4 leva às catacumbas sob a capital. Algures no templo está o Códice do Selo, "
        + "o livro que explica como a Ordem tenciona abrir a porta — e talvez como fechá-la.",
        "As páginas do Nome foram levadas para a secção 0-Ω da Biblioteca da Universidade. O Reitor Abel "
        + "Corvo tem guardas novos que não dormem. Encontrem a lista de iniciados.",
        "No Observatório do Pico Negro, os astrónomos da Ordem calculam a hora da conjunção. Subam a "
        + "montanha, cuidado com o gelo, e descubram quando e onde vai ser o ritual.",
        "São 03:00. Desçam ao Coração da Fossa, passem pelo Sumo-Sacerdote, encontrem o Relatório "
        + "Ómega-Zero e reforjem o Selo antes das 03:33. Juntos. Não se separem.",
    };

    /** Prólogo da história, mostrado no briefing da missão 1. */
    public static final String TEXTO_INTRODUCAO =
        "Zanarkin, 1987. Pescadores desaparecem e o mar devolve coisas com olhos a mais. O agente Vasco "
        + "Rosa, da Divisão Ómega, sumiu a investigar a Conserveira Atlântida. Os agentes Zig e Zag são "
        + "enviados atrás dele: recolham os ficheiros CLASSIFICADOS da Ordem Abissal e saiam vivos — e sãos.";

    /** Epílogo com final em aberto, mostrado no EcraFim de VITORIA. */
    public static final String TEXTO_EPILOGO =
        "Às 03:33, Zig e Zag pronunciaram o Nome lado a lado. O Selo fechou-se com o som de mil portas e a "
        + "Fossa ficou em silêncio. O Reitor Corvo foi encontrado na praia, a contar estrelas que já não "
        + "existem. Do agente Vasco Rosa, nem sinal.\n"
        + "Na Divisão Ómega, o processo foi arquivado com uma nota a lápis: «Dois portadores desceram. "
        + "Dois portadores subiram. Confirmem.» Ninguém confirmou.\n"
        + "Três semanas depois, um pescador de Zanarkin trouxe nas redes um peixe de escamas negras. Tinha "
        + "olhos a mais. E piscavam em código Ómega: Z-I-G. Z-A-G.";

    /** Igual a TEXTO_EPILOGO; mantido por compatibilidade com uma versão anterior. Atualmente não é usado no projeto. */
    public static final String TEXTO_VITORIA = TEXTO_EPILOGO;

    /** Texto do EcraFim de INSANIDADE (sanidade a zero). */
    public static final String TEXTO_INSANIDADE =
        "A sanidade quebrou-se. Nas docas, alguém jura ter ouvido dois agentes a cantarolar "
        + "uma canção sem vogais.";

    // ------------------------------------------------------------------ consultas

    /**
     * Devolve o título de uma nota de lore.
     *
     * @param id Identificador da nota (ex.: "N1-1")
     * @return título (ex.: "Diário de um pescador — 3 de março")
     * @throws IllegalArgumentException se a nota não existir
     */
    public static String getTituloNota(String id)
    {
        return procurar(NOTAS, id)[1];
    }

    /**
     * Devolve o texto de uma nota de lore.
     *
     * @param id Identificador da nota (ex.: "N1-1")
     * @return texto completo da nota
     * @throws IllegalArgumentException se a nota não existir
     */
    public static String getTextoNota(String id)
    {
        return procurar(NOTAS, id)[2];
    }

    /**
     * Devolve o título de um ficheiro classificado.
     *
     * @param id Identificador do dossier (ex.: "D1")
     * @return título (ex.: "O CÓDICE DO SELO" para "D2")
     * @throws IllegalArgumentException se o dossier não existir
     */
    public static String getTituloDossier(String id)
    {
        return procurar(DOSSIERS, id)[1];
    }

    /**
     * Devolve o texto de um ficheiro classificado.
     *
     * @param id Identificador do dossier (ex.: "D1")
     * @return texto completo do dossier
     * @throws IllegalArgumentException se o dossier não existir
     */
    public static String getTextoDossier(String id)
    {
        return procurar(DOSSIERS, id)[2];
    }

    /**
     * Devolve o briefing de uma missão.
     *
     * @param nivel Número da missão, de 1 a 5 (ex.: 1)
     * @return texto do briefing, ou "" se o número não existir
     */
    public static String getBriefing(int nivel)
    {
        return (nivel >= 1 && nivel <= BRIEFINGS.length) ? BRIEFINGS[nivel - 1] : "";
    }

    /**
     * Conta quantas notas existem num nível.
     *
     * @param nivel Número do nível (ex.: 1)
     * @return número de notas desse nível (5 em todos os níveis atuais)
     */
    public static int getTotalNotasNivel(int nivel)
    {
        int total = 0;
        for (String[] nota : NOTAS) {
            if (nota[0].startsWith("N" + nivel + "-")) {
                total++;
            }
        }
        return total;
    }

    /**
     * Devolve o número total de notas de lore do jogo.
     *
     * @return total de notas (25)
     */
    public static int getTotalNotas()
    {
        return NOTAS.length;
    }

    /**
     * Verifica se existe uma nota com o id indicado.
     *
     * @param id Identificador a procurar (ex.: "N3-2")
     * @return true se a nota existir
     */
    public static boolean existeNota(String id)
    {
        for (String[] nota : NOTAS) {
            if (nota[0].equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Procura uma linha { id, título, texto } numa tabela.
     *
     * @param tabela Tabela onde procurar (NOTAS ou DOSSIERS)
     * @param id Identificador a procurar (ex.: "D3")
     * @return linha encontrada
     * @throws IllegalArgumentException se o id não existir na tabela
     */
    private static String[] procurar(String[][] tabela, String id)
    {
        for (String[] linha : tabela) {
            if (linha[0].equals(id)) {
                return linha;
            }
        }
        throw new IllegalArgumentException("Texto de lore inexistente: " + id);
    }
}
