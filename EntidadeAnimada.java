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
    public EntidadeAnimada(int vh){
        this.vh=vh;
        this.vv=0;
    }
    public void act()
    {
        // Add your action code here.
    }
    private int ySpeed;
    public void jump(String jumpKey)
    {
        int groundLevel = getWorld().getWidth() - getImage().getHeight()/2;
        boolean onGround = isTouching(ElementoInterativo.class);
        if (!onGround) // in middle of jump
        {
            ySpeed++; // adds gravity effect
            setLocation(getX(), getY()+ySpeed); // fall (rising slower or falling faster)
            if (getY()>=groundLevel) // has landed (reached ground level)
            {
                setLocation(getX(), groundLevel); // set on ground
                Greenfoot.getKey(); // clears any key pressed during jump
           }
        }
        else // on ground
        {
            if (Greenfoot.isKeyDown(jumpKey)) // jump key detected
            {
                ySpeed = -15; // add jump speed
                setLocation(getX(), getY()+ySpeed); // leave ground
            }
        }
    }
    public void moving(int direcaoX, int deltaY)
    {
        int novoX=getX() +(direcaoX*vh);
        int novoY=getY() + deltaY;
        
        setLocation(novoX,novoY);
    }
}
