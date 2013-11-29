package es.sorikaStudio.scroller2d;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

import es.sorikaStudio.scroller2d.controller.WorldController;

/**
 * Bullet class, its the ammo for the laser gun
 * @author Juan Manuel Rodulfo Salcedo
 *
 */
public class Bullet {        
        private Texture texture;
        private float damage;
        private Vector2 speed;
        private Vector2 position;
        private float lifeTime;
        private World world;
        private Body body;
        private FixtureDef boxFixtureDef;
        private float stateTime = 0;
        private float unitScale;
        private boolean destroy = false;
        
        public Bullet(SpriteBatch batch, float deltaTime, Vector2 position, Texture texture,float damage,Vector2 speed, float lifeTime, float unitScale) {                        
                this.position = position;
                this.damage = damage;
                this.speed = speed;
                this.lifeTime = lifeTime;
                this.unitScale = unitScale;
                world = WorldController.getInstance().getWorld();
                
                initBullet(texture, batch, deltaTime);
        }
        
        /**
         * Texture crashes in constructor so i need this function for the proper work of the class
         * @param texture The bullet Texture
         * @param batch SpriteBatch
         * @param deltaTime deltaTime
         */
        public void initBullet(Texture texture, SpriteBatch batch, float deltaTime) {
                this.texture = texture;
                
                BodyDef bodyDef = new BodyDef();
                bodyDef.type = BodyType.DynamicBody; //this will our a dynamic body
                bodyDef.angle = 0; //set the starting angle
                bodyDef.gravityScale = 0;
                bodyDef.position.set(position.x, position.y); //set the starting position
                
                body = world.createBody(bodyDef); //adds the body to the world
                body.setBullet(true);
                
                body.setUserData(this); // store the bullet data for collisions
                
                PolygonShape boxShape = new PolygonShape(); //the shape of the fixture
                boxShape.setAsBox(texture.getWidth() / unitScale, texture.getHeight() / (2 * unitScale));
                
                boxFixtureDef = new FixtureDef(); //new fixture
                boxFixtureDef.shape = boxShape; //assigns the shape to the fixture
                boxFixtureDef.density = 1; // density
                boxFixtureDef.friction = 0.0f;
        
                body.setFixedRotation(true);
                body.createFixture(boxFixtureDef); //adds the fixture to the Body
                boxFixtureDef.filter.categoryBits = CategoryGroup.BULLET; //bullet category
        }
        
        public void render(SpriteBatch batch, float deltaTime) {
                stateTime += deltaTime;
                
                //force to the bullet
                body.applyLinearImpulse(speed, body.getLocalCenter(), true);
                
                position = body.getPosition();

                Vector2 vector = WorldController.fromScreenToWorld(new Vector2(position.x - (texture.getWidth() / unitScale),
                                position.y - (texture.getHeight() / (2 * unitScale))) );
                
                batch.draw(texture, vector.x, vector.y, 0, 0, 2 * texture.getWidth() / unitScale, texture.getHeight() / unitScale, unitScale, unitScale, 0, 0, 0, texture.getWidth(), texture.getHeight(), false, false);

                if ( stateTime > lifeTime) {
                        destroy = true;
                }
        }
        
        public boolean isDestroyable() {
                return this.destroy;
        }
        
        public float getStateTime() {
                return this.stateTime;
        }
        
        public void setForDestroy(boolean destroy) {
        	this.destroy = destroy;
        }
        
        public boolean isActive() {
            return this.isActive();
        }
        
        public void dispose() {
            world.destroyBody(body);
        }
}