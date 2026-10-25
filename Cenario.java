import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.*;
/*
 * NOTA – USO DE IA
 * Foi utilizado Claude (Anthropic) e Gemini aqui.
 * Âmbito: classe inteira (segundo o comentário da classe)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Classe abstrata base dos níveis.
 * NÍVEIS MAIORES DO QUE O ECRÃ (scroll horizontal):
 *  o mundo Greenfoot tem 1024x572 e é "unbounded" (os atores podem estar fora do ecrã).
 *  O nível tem larguraNivel píxeis; a câmara segue o ponto médio entre o Zig e o Zag.
 *  Quando a câmara avança dx, TODOS os atores (exceto o HUD, ElementoEcra) recuam dx e
 *  os que guardam coordenadas (Deslocavel) corrigem-nas. O fundo é um panorama mais
 *  largo do que o ecrã que se move a metade da velocidade (efeito de paralaxe).
 *  Os agentes nunca saem do ecrã, por isso têm de avançar juntos.
 *
 *  Também gere o ruído, o alarme das câmaras, a sanidade (vida) partilhada, as notas, o ficheiro classificado e o fim do nível.
 *
 *  As coordenadas usadas para construir um nível são coordenadas do Nível (a câmara começa em 0, por isso coincidem com as do ecrã no construtor).
 *
 *  Teclas gerais: P / ESC pausa, M som. Em modo developer: 9 conclui o nível,
 *  8 dá o ficheiro classificado (a vida nunca desce).
 *  O progresso é gravado automaticamente ao começar e ao concluir cada nível.
 *
 *  Passagem entre ecrãs: ao concluir o nível vai para EcraFim (NIVEL_CONCLUIDO); com a sanidade a
 *  zero vai para EcraFim (INSANIDADE); P/ESC abre o EcraPausa; recolher uma nota ou o ficheiro abre o
 *  EcraLeitura (que depois volta a este mesmo mundo).
 *
 *  Nota: desenvolvido com apoio de IA (Claude, Anthropic (Imagens) e Gemini, Google (Auxiliar ao Funcionamento)) — conversas em anexos no relatório.
 *
 *  Hierarquia: Cenario estende World; subclasses: Cenario1, Cenario2, Cenario3, Cenario4, Cenario5.
 *  Conceitos de POO: classe abstrata com métodos abstratos (getNomeNivel, getNumeroNivel), overriding
 *  (act, started, stopped), overloading (criarNovaTentativa x2, adicionarPlataforma x3), métodos
 *  genéricos, encapsulamento (estado privado com consultas públicas) e polimorfismo (emitirRuido
 *  avisa cada inimigo, que reage à sua maneira; deslocarMundo trata todos como Deslocavel).
 */
public abstract class Cenario extends World
{
    /** Largura do ecrã/mundo Greenfoot, em píxeis (1024). */
    public static final int LARGURA = 1024;
    /** Altura do ecrã/mundo Greenfoot, em píxeis (572). */
    public static final int ALTURA = 572;
    /** Deslocamento máximo da câmara por ciclo, em píxeis (10). */
    private static final int VELOCIDADE_MAXIMA_CAMARA = 10;
    /** Duração do alarme, em ciclos (420, cerca de 7 segundos). */
    private static final int DURACAO_ALARME = 420;      // ~7 segundos

    /** Ordem de pintura (primeiro = por cima). Também usada para "fotografar" o ecrã. */
    private static final Class<?>[] ORDEM_PINTURA = {
        MensagemEcra.class, PainelHacking.class, ElementoEcra.class, Balao.class, EfeitoVisual.class,
        ZonaSombra.class, Projectil.class, AgenteInvestigador.class, InimigoCosmico.class, PlacaPressao.class,
        ItemRecolhivel.class, TerminalHacking.class, Dica.class, SinoAncestral.class, Plataforma.class,
        CamaraVigilancia.class, ZonaExtracao.class, ConeVisao.class };

    /** Largura total do nível, em píxeis (ex.: 3072, três ecrãs). */
    private final int larguraNivel;
    /** Imagem de fundo do nível (pode ser mais larga do que o ecrã). */
    private final GreenfootImage panorama;
    /** Estado da missão atual (sanidade, pontos, notas...). */
    private final EstadoMissao estado;
    /** Cópia do estado à entrada do nível (para recomeçar e gravar o checkpoint). */
    private final EstadoMissao estadoInicial;
    /** Posição da câmara: quantos píxeis do nível estão à esquerda do ecrã. */
    private int camaraX;
    /** Ciclos de alarme restantes. */
    private int framesAlarme;
    /** true depois de o nível terminar (concluído ou insanidade). */
    private boolean terminado;
    /** true depois de mostrar o nome do nível, tocar a música e gravar o checkpoint. */
    private boolean tituloMostrado;
    /** Mensagem temporária atualmente no ecrã, ou null. */
    private MensagemEcra mensagem;
    /** Estado da tecla de pausa no ciclo anterior (começa a true para ignorar teclas já premidas). */
    private boolean pausaAntes = true;
    /** Estado da tecla de som no ciclo anterior. */
    private boolean somAntes = true;
    /** Estado das teclas do modo developer no ciclo anterior. */
    private boolean devAntes = true;

    /**
     * Cria o nível: mundo 1024x572 sem limites, fundo, ordem de execução e de pintura, e o HUD.
     * Também chama estado.iniciarNivel() (sanidade cheia, sem ficheiro).
     *
     * @param imagemPanorama fundo (pode ser mais largo do que o ecrã) (ex.: "panorama_nivel1.png")
     * @param larguraNivel   largura total do nível em píxeis, mínimo 1024 (ex.: 3072)
     * @param estado         estado da missão que vem do nível anterior (ex.: new EstadoMissao() num jogo novo)
     */
    public Cenario(String imagemPanorama, int larguraNivel, EstadoMissao estado)
    {
        super(LARGURA, ALTURA, 1, false);
        this.larguraNivel = Math.max(LARGURA, larguraNivel);
        this.panorama = new GreenfootImage(imagemPanorama);
        this.estadoInicial = estado.copia();
        this.estado = estado;
        this.estado.iniciarNivel();
        setBackground(new GreenfootImage(LARGURA, ALTURA));
        desenharFundo();
        setActOrder(Plataforma.class, PlacaPressao.class, AgenteInvestigador.class, InimigoCosmico.class,
                    Projectil.class);
        // Primeiro da lista = desenhado por cima.
        setPaintOrder(ORDEM_PINTURA);
        addObject(new Hud(), LARGURA / 2, 30);
    }

    /**
     * Contrato: nome mostrado no início do nível.
     *
     * @return nome completo (ex.: "Nível 1 — Docas de Zanarkin")
     */
    public abstract String getNomeNivel();

    /**
     * Contrato: número do nível, 1, 2, ... (usado para contar as notas do nível e para gravar).
     *
     * @return número do nível (ex.: 1)
     */
    public abstract int getNumeroNivel();

    /**
     * Cria o nível seguinte com o estado indicado. Atualmente não é chamado (o EcraFim passa pelo
     * EcraHistoria e usa Niveis.criar).
     *
     * @param estadoAtual Estado da missão a levar para o nível seguinte (ex.: getEstado())
     * @return nível seguinte, ou null se este for o último
     */
    public Cenario criarProximoNivel(EstadoMissao estadoAtual)
    {
        return Niveis.criar(getNumeroNivel() + 1, estadoAtual);
    }

    /**
     * Recomeça este nível com o estado que tinha à entrada (overloading: versão sem parâmetros).
     * Ver também criarNovaTentativa(EstadoMissao).
     *
     * @return novo objeto deste nível
     */
    public Cenario criarNovaTentativa()
    {
        return criarNovaTentativa(estadoInicial.copia());
    }

    /**
     * Cria uma nova tentativa deste nível com o estado indicado (overloading).
     *
     * @param estadoAtual Estado com que o nível recomeça (ex.: estadoInicial.copia())
     * @return novo objeto deste nível
     */
    public Cenario criarNovaTentativa(EstadoMissao estadoAtual)
    {
        return Niveis.criar(getNumeroNivel(), estadoAtual);
    }

    /**
     * Devolve a música ambiente do nível (ambiente_1.wav ... ambiente_5.wav).
     *
     * @return nome do ficheiro de som (ex.: "ambiente_1.wav")
     */
    protected String getMusica()
    {
        return "ambiente_" + getNumeroNivel() + ".wav";
    }

    /**
     * Chamado pelo Greenfoot em cada ciclo (redefine World.act). No primeiro ciclo mostra o nome do
     * nível, toca a música e grava o checkpoint; depois atualiza a câmara, o alarme e as teclas gerais.
     */
    @Override
    public void act()
    {
        if (!tituloMostrado) {
            tituloMostrado = true;
            mostrarMensagem(getNomeNivel(), new Color(200, 180, 255), 180);
            Audio.tocarMusica(getMusica());
            SistemaSave.guardar(estadoInicial, getNumeroNivel());    // checkpoint
        }
        atualizarCamara();
        if (framesAlarme > 0) {
            framesAlarme--;
        }
        lerTeclasGerais();
    }

    /** Greenfoot em pausa (botão Pause): pára a música (redefine World.stopped). */
    @Override
    public void stopped()
    {
        Audio.pausarMusica();
    }

    /** Greenfoot a correr (botão Run): retoma a música do nível (redefine World.started). */
    @Override
    public void started()
    {
        Audio.tocarMusica(getMusica());
    }

    /** Trata as teclas gerais: P/ESC pausa, M som, e as teclas do modo developer (9 e 8). */
    private void lerTeclasGerais()
    {
        boolean pausa = Greenfoot.isKeyDown("p") || Greenfoot.isKeyDown("escape");
        boolean som = Greenfoot.isKeyDown("m");
        boolean dev = Greenfoot.isKeyDown("9") || Greenfoot.isKeyDown("8");
        if (pausa && !pausaAntes && !terminado) {
            Greenfoot.setWorld(new EcraPausa(this));
        }
        if (som && !somAntes) {
            Audio.setSomAtivo(!Audio.isSomAtivo());
            SistemaSave.setSomAtivo(Audio.isSomAtivo());
            mostrarMensagem(Audio.isSomAtivo() ? "Som ligado" : "Som desligado", new Color(200, 200, 220), 60);
        }
        if (dev && !devAntes && estado.isModoDeveloper()) {
            if (Greenfoot.isKeyDown("8")) {
                estado.registarFicheiro("D" + getNumeroNivel());
                mostrarMensagem("[DEV] ficheiro obtido", new Color(255, 200, 80), 90);
            } else {
                concluirNivel();
            }
        }
        pausaAntes = pausa;
        somAntes = som;
        devAntes = dev;
    }

    // ------------------------------------------------------------------ fotografia do ecrã

    /**
     * Compõe uma imagem com o fundo e todos os atores (para os ecrãs de pausa e leitura),
     * respeitando a ordem de pintura e sem as mensagens temporárias.
     *
     * @return nova imagem do ecrã atual
     */
    public GreenfootImage capturarEcra()
    {
        GreenfootImage img = new GreenfootImage(getBackground());
        List<Actor> atores = new ArrayList<Actor>(getObjects(Actor.class));
        Collections.sort(atores, new Comparator<Actor>() {
            @Override
            public int compare(Actor a, Actor b)
            {
                return posicaoNaPintura(b) - posicaoNaPintura(a);   // os de baixo primeiro
            }
        });
        for (Actor a : atores) {
            GreenfootImage im = a.getImage();
            if (im != null && !(a instanceof MensagemEcra)) {
                img.drawImage(im, a.getX() - im.getWidth() / 2, a.getY() - im.getHeight() / 2);
            }
        }
        return img;
    }

    /**
     * Calcula o índice da classe mais específica do ator na ordem de pintura (como faz o Greenfoot).
     *
     * @param a Ator a classificar (ex.: um Zig, que conta como AgenteInvestigador)
     * @return índice em ORDEM_PINTURA, ou o tamanho da lista se a classe não estiver lá
     */
    private static int posicaoNaPintura(Actor a)
    {
        for (Class<?> c = a.getClass(); c != null; c = c.getSuperclass()) {
            for (int i = 0; i < ORDEM_PINTURA.length; i++) {
                if (ORDEM_PINTURA[i] == c) {
                    return i;
                }
            }
        }
        return ORDEM_PINTURA.length;
    }

    // ------------------------------------------------------------------ câmara e scroll

    /** A câmara segue o ponto médio dos agentes, suavemente (1/6 da distância, no máximo 10 px) e sem sair do nível. */
    private void atualizarCamara()
    {
        List<AgenteInvestigador> agentes = getAgentes();
        if (agentes.isEmpty()) {
            return;
        }
        int soma = 0;
        for (AgenteInvestigador a : agentes) {
            soma += a.getX();
        }
        int media = soma / agentes.size();
        int alvo = limitarCamara(camaraX + media - LARGURA / 2);
        int diferenca = alvo - camaraX;
        if (diferenca == 0) {
            return;
        }
        int passo = diferenca / 6;
        if (passo == 0) {
            passo = Integer.signum(diferenca);
        }
        passo = Math.max(-VELOCIDADE_MAXIMA_CAMARA, Math.min(VELOCIDADE_MAXIMA_CAMARA, passo));
        deslocarMundo(passo);
    }

    /**
     * Limita uma posição de câmara ao intervalo válido do nível.
     *
     * @param x Posição pretendida da câmara, em píxeis do nível (ex.: 2500)
     * @return posição entre 0 e larguraNivel - 1024 (ex.: 2048 num nível de 3072)
     */
    private int limitarCamara(int x)
    {
        return Math.max(0, Math.min(larguraNivel - LARGURA, x));
    }

    /**
     * Posiciona a câmara de imediato (ex.: para começar a meio do nível). Atualmente não é chamado no projeto.
     *
     * @param xNivel Coordenada x do nível que deve ficar no centro do ecrã (ex.: 1536)
     */
    public void centrarCamaraEm(int xNivel)
    {
        int alvo = limitarCamara(xNivel - LARGURA / 2);
        if (alvo != camaraX) {
            deslocarMundo(alvo - camaraX);
        }
    }

    /**
     * Move a câmara dx píxeis: todos os objetos do nível (exceto os ElementoEcra) recuam dx no ecrã
     * e os Deslocavel corrigem as coordenadas guardadas; depois redesenha o fundo.
     *
     * @param dx Deslocamento da câmara em píxeis, positivo = para a direita (ex.: 10)
     */
    private void deslocarMundo(int dx)
    {
        camaraX += dx;
        for (Object o : getObjects(null)) {
            Actor ator = (Actor) o;
            if (ator instanceof ElementoEcra) {
                continue;
            }
            ator.setLocation(ator.getX() - dx, ator.getY());
            if (ator instanceof Deslocavel) {
                ((Deslocavel) ator).deslocarReferencias(dx);
            }
        }
        desenharFundo();
    }

    /** Desenha a parte do panorama que corresponde à câmara (paralaxe proporcional à largura do panorama). */
    private void desenharFundo()
    {
        int maximo = Math.max(0, panorama.getWidth() - LARGURA);
        int deslocamento = 0;
        if (larguraNivel > LARGURA) {
            deslocamento = (int) ((long) camaraX * maximo / (larguraNivel - LARGURA));
        }
        getBackground().drawImage(panorama, -deslocamento, 0);
    }

    /**
     * Devolve a posição da câmara.
     *
     * @return píxeis do nível à esquerda do ecrã (0 no início)
     */
    public int getCamaraX()
    {
        return camaraX;
    }

    /**
     * Devolve a largura total do nível.
     *
     * @return largura em píxeis (ex.: 3072)
     */
    public int getLarguraNivel()
    {
        return larguraNivel;
    }

    /**
     * Devolve o limite esquerdo do nível em coordenadas de ecrã.
     *
     * @return x do início do nível no ecrã (0 ou negativo)
     */
    public int getLimiteEsquerdo()
    {
        return -camaraX;
    }

    /**
     * Devolve o limite direito do nível em coordenadas de ecrã.
     *
     * @return x do fim do nível no ecrã
     */
    public int getLimiteDireito()
    {
        return larguraNivel - camaraX;
    }

    /**
     * Converte um x do ecrã num x do nível. Atualmente não é chamado no projeto.
     *
     * @param xEcra Coordenada x no ecrã, em píxeis (ex.: 512)
     * @return coordenada x correspondente no nível
     */
    public int paraXNivel(int xEcra)
    {
        return xEcra + camaraX;
    }

    // ------------------------------------------------------------------ ruído e alarme

    /**
     * Avisa todos os inimigos dentro do raio do ruído (polimorfismo: cada um reage à sua maneira).
     *
     * @param x Coordenada x da origem do ruído, em píxeis (ex.: getX() do agente)
     * @param y Coordenada y da origem do ruído, em píxeis (ex.: getBaseColisao() do agente)
     * @param tipo Tipo de ruído, que define o raio (ex.: TipoRuido.PASSOS, 160 px)
     */
    public void emitirRuido(int x, int y, TipoRuido tipo)
    {
        long raio2 = (long) tipo.getRaio() * tipo.getRaio();
        for (InimigoCosmico inimigo : getObjects(InimigoCosmico.class)) {
            long dx = inimigo.getX() - x;
            long dy = inimigo.getY() - y;
            if (dx * dx + dy * dy <= raio2) {
                inimigo.ouvirRuido(x, y, tipo);
            }
        }
    }

    /**
     * Dispara o alarme quando uma câmara (ou um hacking falhado) deteta os agentes: mensagem,
     * ruído ALARME para todo o nível e -1 sanidade. Ignorado se já houver um alarme ativo.
     *
     * @param x Coordenada x do local detetado, em píxeis (ex.: agente.getX())
     * @param y Coordenada y do local detetado, em píxeis (ex.: agente.getY())
     */
    public void dispararAlarme(int x, int y)
    {
        if (framesAlarme > 0 || terminado) {
            return;
        }
        framesAlarme = DURACAO_ALARME;
        Audio.tocar("alarme.wav");
        mostrarMensagem("DETETADOS! -1 sanidade", new Color(255, 70, 70), 120);
        emitirRuido(x, y, TipoRuido.ALARME);
        perderSanidade(1);
    }

    /**
     * Indica se há um alarme ativo.
     *
     * @return true durante os 420 ciclos do alarme
     */
    public boolean isAlarmeAtivo()
    {
        return framesAlarme > 0;
    }

    // ------------------------------------------------------------------ sanidade, pontos, itens

    /**
     * Regista que um agente caiu ao mar / abismo: som, mensagem e -1 sanidade.
     *
     * @param agente Agente que caiu (ex.: o Zig)
     */
    public void registarQueda(AgenteInvestigador agente)
    {
        Audio.tocar("queda.wav");
        mostrarMensagem(agente.getNomeCodigo() + " caiu!" + (estado.isModoDeveloper() ? "" : " -1 sanidade"), new Color(255, 160, 80), 100);
        perderSanidade(1);
    }

    /**
     * Regista que um agente foi ferido por um inimigo ou projétil: som e -1 sanidade.
     *
     * @param agente Agente ferido (ex.: o Zag)
     */
    public void registarDano(AgenteInvestigador agente)
    {
        Audio.tocar("dano.wav");
        perderSanidade(1);
    }

    /**
     * Regista a neutralização de um inimigo no estado da missão (+25 pontos).
     *
     * @param inimigo Inimigo neutralizado (ex.: um CultistaPatrulheiro)
     */
    public void registarNeutralizacao(InimigoCosmico inimigo)
    {
        estado.registarNeutralizacao();
    }

    /**
     * Retira sanidade e, se chegar a zero, termina o nível com o ecrã de INSANIDADE.
     *
     * @param quantidade Sanidade a retirar (ex.: 1)
     */
    private void perderSanidade(int quantidade)
    {
        if (terminado) {
            return;
        }
        estado.perderSanidade(quantidade);
        if (estado.isInsano()) {
            terminado = true;
            Greenfoot.setWorld(new EcraFim(EcraFim.Tipo.INSANIDADE, this, estadoInicial.copia()));
        }
    }

    /**
     * Regista uma nota de lore e, se for nova, abre o EcraLeitura com o texto.
     *
     * @param nota Nota apanhada (ex.: a NotaLore "N1-1")
     */
    public void recolherNota(NotaLore nota)
    {
        if (estado.registarNota(nota.getId())) {
            Audio.tocar("nota.wav");
            Greenfoot.setWorld(new EcraLeitura(this, ArquivoLore.getTituloNota(nota.getId()), ArquivoLore.getTextoNota(nota.getId()), false));
        }
    }

    /**
     * Regista o ficheiro classificado do nível e abre o EcraLeitura com o dossier.
     *
     * @param ficheiro Ficheiro apanhado (ex.: o FicheiroSecreto "D1")
     */
    public void recolherFicheiro(FicheiroSecreto ficheiro)
    {
        estado.registarFicheiro(ficheiro.getIdDossier());
        Audio.tocar("ficheiro.wav");
        mostrarMensagem("Ficheiro obtido! Os dois à EXTRAÇÃO!", new Color(110, 255, 160), 180);
        Greenfoot.setWorld(new EcraLeitura(this, ArquivoLore.getTituloDossier(ficheiro.getIdDossier()),
                                           ArquivoLore.getTextoDossier(ficheiro.getIdDossier()), true));
    }

    /**
     * Conclui o nível (os dois agentes chegaram à extração com o ficheiro, ou tecla 9 em modo developer):
     * grava o checkpoint do nível seguinte, se existir, e abre o EcraFim (NIVEL_CONCLUIDO).
     */
    public void concluirNivel()
    {
        if (terminado) {
            return;
        }
        terminado = true;
        if (Niveis.existe(getNumeroNivel() + 1)) {
            SistemaSave.guardar(estado, getNumeroNivel() + 1);
        }
        Greenfoot.setWorld(new EcraFim(EcraFim.Tipo.NIVEL_CONCLUIDO, this, estado));
    }

    // ------------------------------------------------------------------ mensagens no ecrã

    /**
     * Mostra uma mensagem temporária no ecrã (substitui a anterior), centrada a y = 250.
     *
     * @param texto Texto a mostrar (ex.: "O selo abriu-se!")
     * @param cor Cor das riscas e do texto (ex.: new Color(190, 130, 255))
     * @param duracao Duração em ciclos (ex.: 120)
     */
    public void mostrarMensagem(String texto, Color cor, int duracao)
    {
        if (mensagem != null && mensagem.getWorld() != null) {
            removeObject(mensagem);
        }
        mensagem = new MensagemEcra(texto, cor, duracao);
        addObject(mensagem, LARGURA / 2, 250);
    }

    // ------------------------------------------------------------------ construção de níveis

    /**
     * Coloca uma plataforma pelo seu canto superior esquerdo (overloading: versão base).
     *
     * @param plataforma Plataforma já criada (ex.: o Portao do Cenario1)
     * @param esquerda Coordenada x do lado esquerdo, em píxeis do nível (ex.: 2240)
     * @param topo Coordenada y do topo, em píxeis (ex.: CHAO - 220 = 300)
     * @return a mesma plataforma, para encadear
     */
    protected Plataforma adicionarPlataforma(Plataforma plataforma, int esquerda, int topo)
    {
        addObject(plataforma, esquerda + plataforma.getLargura() / 2, topo + plataforma.getAltura() / 2);
        return plataforma;
    }

    /**
     * Atalho: plataforma estática com a altura padrão do tipo (overloading).
     *
     * @param esquerda Coordenada x do lado esquerdo, em píxeis do nível (ex.: 256)
     * @param topo Coordenada y do topo, em píxeis (ex.: CHAO - 48 = 472)
     * @param largura Largura em píxeis (ex.: 96)
     * @param tipo Tipo de plataforma (ex.: TipoPlataforma.CAIXOTE)
     * @return a plataforma criada
     */
    protected Plataforma adicionarPlataforma(int esquerda, int topo, int largura, TipoPlataforma tipo)
    {
        return adicionarPlataforma(new Plataforma(largura, tipo), esquerda, topo);
    }

    /**
     * Atalho: plataforma estática com altura à escolha (overloading).
     *
     * @param esquerda Coordenada x do lado esquerdo, em píxeis do nível (ex.: 1330)
     * @param topo Coordenada y do topo, em píxeis (ex.: CHAO - 96 = 424)
     * @param largura Largura em píxeis (ex.: 96)
     * @param altura Altura em píxeis (ex.: 96)
     * @param tipo Tipo de plataforma (ex.: TipoPlataforma.CAIXOTE)
     * @return a plataforma criada
     */
    protected Plataforma adicionarPlataforma(int esquerda, int topo, int largura, int altura, TipoPlataforma tipo)
    {
        return adicionarPlataforma(new Plataforma(largura, altura, tipo), esquerda, topo);
    }

    /**
     * Cria um chão desde o topo indicado até ao fundo do ecrã.
     *
     * @param esquerda Coordenada x do lado esquerdo, em píxeis do nível (ex.: 0)
     * @param largura Largura em píxeis (ex.: 608)
     * @param topo Coordenada y do topo do chão, em píxeis (ex.: 520)
     * @param tipo Textura do chão (ex.: TipoPlataforma.CAIS)
     * @return o chão criado
     */
    protected Chao adicionarChao(int esquerda, int largura, int topo, TipoPlataforma tipo)
    {
        Chao chao = new Chao(largura, ALTURA - topo, tipo);
        adicionarPlataforma(chao, esquerda, topo);
        return chao;
    }

    /**
     * Cria uma zona de sombra pelo canto superior esquerdo.
     *
     * @param esquerda Coordenada x do lado esquerdo, em píxeis do nível (ex.: 40)
     * @param topo Coordenada y do topo, em píxeis (ex.: CHAO - 110 = 410)
     * @param largura Largura em píxeis (ex.: 160)
     * @param altura Altura em píxeis (ex.: 110)
     * @return a sombra criada
     */
    protected ZonaSombra adicionarSombra(int esquerda, int topo, int largura, int altura)
    {
        ZonaSombra sombra = new ZonaSombra(largura, altura);
        addObject(sombra, esquerda + largura / 2, topo + altura / 2);
        return sombra;
    }

    /**
     * Coloca qualquer ator com a parte de baixo da imagem na altura indicada (método genérico).
     *
     * @param <A> Tipo concreto do ator (o valor devolvido mantém esse tipo)
     * @param ator Ator a colocar (ex.: new CultistaPatrulheiro(250))
     * @param x Coordenada x do centro, em píxeis do nível (ex.: 940)
     * @param base Coordenada y onde fica a parte de baixo da imagem, em píxeis (ex.: CHAO = 520)
     * @return o mesmo ator, para encadear
     */
    protected <A extends Actor> A adicionarSobre(A ator, int x, int base)
    {
        addObject(ator, x, base - ator.getImage().getHeight() / 2);
        return ator;
    }

    /**
     * Coloca um elemento pelo canto superior esquerdo. Faz o mesmo que adicionarPlataforma para
     * qualquer ElementoInterativo (método genérico; não é overloading, porque o nome é diferente).
     *
     * @param <E> Tipo concreto do elemento (o valor devolvido mantém esse tipo)
     * @param elemento Elemento a colocar (ex.: uma PlacaPressao)
     * @param esquerda Coordenada x do lado esquerdo, em píxeis do nível (ex.: 2100)
     * @param topo Coordenada y do topo, em píxeis (ex.: CHAO - placa.getAltura())
     * @return o mesmo elemento, para encadear
     */
    protected <E extends ElementoInterativo> E adicionarElemento(E elemento, int esquerda, int topo)
    {
        addObject(elemento, esquerda + elemento.getLargura() / 2, topo + elemento.getAltura() / 2);
        return elemento;
    }

    /**
     * Cria o Zig e o Zag com os pés nas posições indicadas.
     *
     * @param xZig Coordenada x do Zig, em píxeis do nível (ex.: 50)
     * @param baseZig Coordenada y dos pés do Zig (ex.: CHAO = 520)
     * @param xZag Coordenada x do Zag, em píxeis do nível (ex.: 110)
     * @param baseZag Coordenada y dos pés do Zag (ex.: CHAO = 520)
     */
    protected void adicionarAgentes(int xZig, int baseZig, int xZag, int baseZag)
    {
        adicionarSobre(new Zig(), xZig, baseZig);
        adicionarSobre(new Zag(), xZag, baseZag);
    }

    // ------------------------------------------------------------------ consultas

    /**
     * Devolve o estado da missão atual.
     *
     * @return estado (sanidade, pontos, notas, ficheiro...)
     */
    public EstadoMissao getEstado()
    {
        return estado;
    }

    /**
     * Devolve o estado com que o nível começou (para recomeçar ou gravar o checkpoint).
     *
     * @return cópia independente do estado inicial
     */
    public EstadoMissao getEstadoInicial()
    {
        return estadoInicial.copia();
    }

    /**
     * Devolve o Zig.
     *
     * @return o Zig deste nível, ou null se não existir
     */
    public Zig getZig()
    {
        List<Zig> lista = getObjects(Zig.class);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /**
     * Devolve o Zag.
     *
     * @return o Zag deste nível, ou null se não existir
     */
    public Zag getZag()
    {
        List<Zag> lista = getObjects(Zag.class);
        return lista.isEmpty() ? null : lista.get(0);
    }

    /**
     * Devolve os agentes presentes no nível.
     *
     * @return lista com o Zig e o Zag (se existirem)
     */
    public List<AgenteInvestigador> getAgentes()
    {
        return getObjects(AgenteInvestigador.class);
    }

    /**
     * Indica se o nível já terminou.
     *
     * @return true depois de concluído ou perdido por insanidade
     */
    public boolean isTerminado()
    {
        return terminado;
    }
}
