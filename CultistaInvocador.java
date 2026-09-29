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
    public CultistaInvocador(int limite, int velocidade){
        super(velocidade);
        GreenfootImage placeholder = new GreenfootImage(40, 50);
        placeholder.setColor(Color.RED);
        placeholder.fill();
        setImage(placeholder);
        this.limite=limite;
    }
    public void act()
    {
        
    }
    //*public void limitMove(int limite){
        //()
    
    //}
}
