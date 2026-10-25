import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Ficheiro CLASSIFICADO da Ordem Abissal — o objetivo de cada nível.
 * Sem ele a ZonaExtracao não deixa sair. Está sempre protegido por puzzles
 * (cofre hackeado, selo de placas, câmaras...).
 *
 * Hierarquia: FicheiroSecreto estende ItemRecolhivel, que estende ElementoInterativo, que estende Actor
 * (3 níveis além de Actor).
 * Conceitos de POO: herança, implementação do método abstrato aoSerRecolhido (overriding),
 * encapsulamento.
 */
public class FicheiroSecreto extends ItemRecolhivel
{
    /** Identificador do dossier em ArquivoLore (ex.: "D1"). */
    private final String idDossier;

    /**
     * Cria o ficheiro com o ícone "ficheiro_secreto.png" e brilho vermelho.
     *
     * @param idDossier identificador em ArquivoLore (ex.: "D1")
     */
    public FicheiroSecreto(String idDossier)
    {
        super("ficheiro_secreto.png", new Color(255, 70, 70));
        this.idDossier = idDossier;
    }

    /**
     * Implementa ItemRecolhivel.aoSerRecolhido: pede ao Cenario para registar o ficheiro e mostrar o dossier.
     *
     * @param agente Agente que apanhou o ficheiro (ex.: o Zig)
     * @param cenario Nível atual (ex.: um Cenario1)
     */
    @Override
    protected void aoSerRecolhido(AgenteInvestigador agente, Cenario cenario)
    {
        cenario.recolherFicheiro(this);
    }

    /**
     * Devolve o identificador do dossier.
     *
     * @return id em ArquivoLore (ex.: "D1")
     */
    public String getIdDossier()
    {
        return idDossier;
    }
}
