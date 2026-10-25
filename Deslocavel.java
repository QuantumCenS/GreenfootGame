/**
 * Interface dos objetos que guardam coordenadas x internas (origem de patrulha,
 * última posição segura, posição exata de um projétil...).
 *
 * Os níveis são maiores do que o ecrã: quando a câmara avança, o Cenario desloca TODOS
 * os atores dx píxeis para a esquerda e chama este método para que cada objeto corrija
 * também as coordenadas que guardou.
 *
 * Implementada por: EntidadeAnimada, PlataformaMovel e Projectil.
 * Conceitos de POO: interface (contrato comum a classes de hierarquias diferentes) e polimorfismo
 * (o Cenario trata todos os objetos como Deslocavel, sem conhecer a classe concreta).
 */
public interface Deslocavel
{
    /**
     * Corrige as coordenadas x guardadas pelo objeto depois de a câmara se mover.
     * Contrato: quem implementa deve subtrair dx a todas as coordenadas x que guardou
     * (a posição no ecrã já foi atualizada pelo Cenario).
     *
     * @param dx quanto a câmara avançou para a direita, em píxeis; os objetos recuam dx no ecrã
     *           (ex.: 10, o passo máximo da câmara por ciclo; negativo quando a câmara recua)
     */
    void deslocarReferencias(int dx);
}
