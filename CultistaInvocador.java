import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class CultistaInvocador here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class CultistaInvocador extends InimigoCosmico
{
    /**
     * Act - do whatever the CultistaInvocador wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    private int limite;
    private int direcao;
    public CultistaInvocador(int limite, int velocidade){
        super(limite,velocidade);
        this.limite=limite;
        direcao=1;
    }
    public void act()
    {
    }
    //Passar para inimigo cosmico
}
