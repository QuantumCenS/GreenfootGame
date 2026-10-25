import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/*
 * NOTA – USO DE IA
 * Foi utilizado Gemini aqui.
 * Âmbito: classe inteira (os sons que usa são gerados por ferramentas/gerar_sons.py)
 * Nota: anexar a conversa com a IA ao relatório, como exige o enunciado.
 */
/**
 * Gestão do som do jogo (classe utilitária, só métodos estáticos).
 *  - efeitos curtos: Audio.tocar("salto.wav");
 *  - música/ambiente em ciclo: Audio.tocarMusica("ambiente_1.wav") — só uma de cada vez;
 *  - som ligado/desligado (tecla M ou menu), lembrado pelo SistemaSave.
 * Todos os sons ficam na pasta "sounds" e foram gerados por síntese (ferramentas/gerar_sons.py).
 * Se o computador não tiver placa de som, os erros são ignorados e o jogo continua.
 *
 * Classe utilitária final (não se criam objetos).
 * Conceitos de POO: construtor privado, atributos e métodos estáticos, encapsulamento (a música
 * atual fica escondida), tratamento de exceções.
 */
public final class Audio
{
    /** Volume da música, de 0 a 100 (55). */
    private static final int VOLUME_MUSICA = 55;

    /** Música em ciclo atualmente carregada, ou null. */
    private static GreenfootSound musica;
    /** Nome do ficheiro da música atual, ou null. */
    private static String ficheiroMusica;
    /** true se o som está ligado. */
    private static boolean somAtivo = true;

    /** Construtor privado: classe só com métodos estáticos. */
    private Audio()
    {
    }

    /**
     * Toca um efeito sonoro uma vez (se o som estiver ligado).
     *
     * @param ficheiro Nome do ficheiro na pasta sounds (ex.: "salto.wav")
     */
    public static void tocar(String ficheiro)
    {
        if (!somAtivo) {
            return;
        }
        try {
            Greenfoot.playSound(ficheiro);
        } catch (Exception e) {
            // sem som disponível: ignorar
        }
    }

    /**
     * Começa uma música em ciclo (se já estiver a tocar a mesma, não faz nada; se estiver
     * em pausa, retoma-a). Pára a música anterior.
     *
     * @param ficheiro Nome do ficheiro na pasta sounds (ex.: "musica_menu.wav" ou "ambiente_1.wav")
     */
    public static void tocarMusica(String ficheiro)
    {
        if (ficheiro.equals(ficheiroMusica) && musica != null) {
            if (somAtivo && !musica.isPlaying()) {
                retomarMusica();
            }
            return;
        }
        pararMusica();
        ficheiroMusica = ficheiro;
        try {
            musica = new GreenfootSound(ficheiro);
            musica.setVolume(VOLUME_MUSICA);
            if (somAtivo) {
                musica.playLoop();
            }
        } catch (Exception e) {
            musica = null;
        }
    }

    /** Pára e esquece a música atual. */
    public static void pararMusica()
    {
        if (musica != null) {
            try {
                musica.stop();
            } catch (Exception e) {
                // ignorar
            }
        }
        musica = null;
        ficheiroMusica = null;
    }

    /** Pausa a música atual. Usado quando o jogador carrega em Pause no Greenfoot ou desliga o som. */
    public static void pausarMusica()
    {
        if (musica != null) {
            try {
                musica.pause();
            } catch (Exception e) {
                // ignorar
            }
        }
    }

    /** Retoma a música atual em ciclo, se o som estiver ligado. */
    public static void retomarMusica()
    {
        if (musica != null && somAtivo) {
            try {
                musica.playLoop();
            } catch (Exception e) {
                // ignorar
            }
        }
    }

    /**
     * Indica se o som está ligado.
     *
     * @return true se ligado
     */
    public static boolean isSomAtivo()
    {
        return somAtivo;
    }

    /**
     * Liga ou desliga o som (retoma ou pausa a música).
     *
     * @param ativo true para ligar (ex.: SistemaSave.isSomAtivo() ao abrir o menu)
     */
    public static void setSomAtivo(boolean ativo)
    {
        somAtivo = ativo;
        if (ativo) {
            retomarMusica();
        } else {
            pausarMusica();
        }
    }

    /**
     * Devolve o ficheiro da música atual.
     *
     * @return nome do ficheiro (ex.: "ambiente_2.wav"), ou null se não houver música
     */
    public static String getMusicaAtual()
    {
        return ficheiroMusica;
    }
}
