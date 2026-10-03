import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class InimigoCosmico here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class InimigoCosmico extends EntidadeAnimada
{
    /**
     * Act - do whatever the InimigoCosmico wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    private int limite;
    private int direcao;
    public InimigoCosmico(int limite,int velocidade){
        super(velocidade);
        this.limite=limite;
        direcao=1;
        GreenfootImage placeholder = new GreenfootImage(40, 50);
        placeholder.setColor(Color.RED);
        placeholder.fill();
        setImage(placeholder);
    }
    public void act()
    {
        limitMove();
    }
    public void limitMove(){
        if((getXinicial()+limite)<=getX()){
            direcao=-1;
        }
        if(getXinicial()>=getX()){
            direcao=1;
        }
        moving(direcao,0);
    }
}
