import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
import java.util.List;
/**
 * Write a description of class EntidadeAnimada here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class EntidadeAnimada extends Actor
{
    /**
     * Act - do whatever the EntidadeAnimada wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    private int vh;
    private int vv;
    private int Xinicial;
    private int Yinicial;
    public EntidadeAnimada(int vh){
        this.vh=vh;
        this.vv=0;
    }
    public void act()
    {
        // Add your action code here.
    }
    public void moving(int direcaoX, int deltaY)
    {
        int novoX=getX() +(direcaoX*vh);
        int novoY=getY() + deltaY;
        
        setLocation(novoX,novoY);
    }
    @Override
    protected void addedToWorld(World world){
        this.Xinicial=getX();
        this.Yinicial=getY();
    }
    public int getXinicial(){
        return Xinicial;
    }
    public int getYinicial(){
        return Yinicial;
    }
}
