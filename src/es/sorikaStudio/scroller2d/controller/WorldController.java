package es.sorikaStudio.scroller2d.controller;

import java.util.ArrayList;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;

import es.sorikaStudio.scroller2d.Bullet;
import es.sorikaStudio.scroller2d.BulletCollection;

/**
 * Controller for World Class
 * @author Juan Manuel Rodulfo Salcedo
 */
public class WorldController {

    private Vector2 gravity = new Vector2(0, -100f);
    private World world;
    private boolean doSleep = true;
    private BitmapFont font;
    private static OrthographicCamera camera;
    private BulletCollection<Bullet> bulletDestroyerArray;
    private ArrayList<Bullet> bullets;
    
    private int velocityIterations = 8;   //how strongly to correct velocity
    private int positionIterations = 3;   //how strongly to correct position
        
    private static WorldController INSTANCE = new WorldController(); //Singleton Pattern
    
    /**
     * private Contructor so only one instance of WorldController is allowed.
     */
    private WorldController() {
	    world = new World(gravity, doSleep);
	    font = new BitmapFont();
	    camera = CameraController.getInstance().getCamera();
	    bulletDestroyerArray = new BulletCollection<Bullet>();
    }
    
    /**
     * Returns the instance of the Controller
     * @return the only instance of the WorldController Class.
     */
    public static WorldController getInstance() {
        return INSTANCE;
    }

    /**
     * Disposes the world object.
     */
    public void dispose() {
            world.dispose();
    }
    
    public void render(float deltaTime) {
    	bullets = CharacterController.getInstance().getBullets();
        world.step(deltaTime, velocityIterations, positionIterations);
        if (!bulletDestroyerArray.isEmpty()) {
        	bullets.removeAll(bulletDestroyerArray);
        }
        
    }
    
    public void addBulletForDestroy(Bullet b) {
    	bulletDestroyerArray.add(b);
    }
        
        
        
        /**
         * Returns the World object used for all the Box2d physic.
         * @return the world object
         */
        public World getWorld() {
                if (world == null)
                        return null;
                else
                        return world;
        }
        
        /**
         * Changes the current gravity of the world with the new given.
         * @param gravity new gravity for the world.
         */
        public void setGravity(Vector2 gravity) {
                this.gravity = gravity;
                world.setGravity(gravity);
        }
        
        /**
         * Returns the current gravity of the world.
         * @return the current gravity
         */
        public Vector2 getGravity() {
                return gravity;
        }
        
        public static Vector2 fromScreenToWorld(Vector2 vector) {
                Vector3 temp = new Vector3(vector.x, vector.y, 0);
                camera.project(temp);
                
                Vector2 v2 = new Vector2(temp.x, temp.y);
                return v2;
        }
        
}