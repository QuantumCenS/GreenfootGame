import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)

/**
 * Write a description of class Agents here.
 * 
 * @author (your name) 
 * @version (a version number or a date)
 */
public class Agents extends EntidadeAnimada
{
    /**
     * Act - do whatever the Agents wants to do. This method is called whenever
     * the 'Act' or 'Run' button gets pressed in the environment.
     */
    private int ySpeed;
    
    public Agents(int velocidade)
    {
        super(velocidade);
    }
    public void act()
    {
        
    }
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
    public void movingLR(String Lkey,String Rkey){
        if (Greenfoot.isKeyDown(Rkey)){
            moving(1,0);
        }
        if (Greenfoot.isKeyDown(Lkey)){
            moving(-1,0);
        }
    }
}
