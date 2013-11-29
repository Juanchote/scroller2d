package es.sorikaStudio.scroller2d.controller;

import java.util.ArrayList;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import es.sorikaStudio.scroller2d.Bullet;
import es.sorikaStudio.scroller2d.CategoryGroup;
 
/**
 * Controller for Main Character, controlling movement, rigidbody and all
 * the stuff related to the Character.
 * @author Juan Manuel Rodulfo Salcedo
 *
 */

public class CharacterController extends InputAdapter {
        
    private WorldController worldController; //the world controller
    private World world;
    
    private OrthographicCamera camera;
    
    private Rectangle sprite;
    private Rectangle srcSprite;
    
    private Animation idleAnimationRight;
    private Animation idleAnimationLeft;
    private Texture walkSheet;              
    private TextureRegion[] idleFramesRight;    
    private TextureRegion[] idleFramesLeft;             

    private SpriteBatch spriteBatch;            
    private TextureRegion currentFrame; 
    
    private Texture tmp;
    private TextureRegion texture;
    private Sprite sprite1;
    private ArrayList<Bullet> bullets;
    
    private float stateTime;                       
    private float attackDelta = 0;
    private float tempAttack = 0;
    
    private Vector2 position; //Character Position
    private float MAX_VELOCITY = 10f;
    private boolean jump = false;
    private float impulse = 30f;
    private enum MoveStates {MS_LEFT, MS_RIGHT, MS_JUMP,MS_ATTACK, MS_STOP};
    private MoveStates moveState = MoveStates.MS_STOP;
    private boolean isGrounded = false;
    private boolean facingRight = true;
    private boolean lastState = false;

    private BitmapFont font;
    
    private Body charBody; //Body definition
    private PolygonShape boxShape; //Fisture Shape
    private CollisionListener collisionListener;
    private FixtureDef footFixture; // feet sensor
        
    private ShapeRenderer renderer;
    private boolean debugMode = true;
    private float unitScale = 0;
    private Vector2 RESPAWN;
    private int FRAME_COLS = 1;
    private int FRAME_ROWS = 5;
    
    private static CharacterController INSTANCE = new CharacterController(); //Singleton Pattern
    
    /**
     * Private Constructor preventing from multiple instances of CharacterController Class
     */
    private CharacterController() {
	    worldController = WorldController.getInstance();
	    world = worldController.getWorld();
	    
	    camera = CameraController.getInstance().getCamera();
	    lastState = facingRight;
	    
	    sprite = new Rectangle();
	    srcSprite = new Rectangle();
	    
	    //tmp = new Texture(Gdx.files.internal("data/Animations/Player/Idle/player_idle_1.png"));        
	    //sprite1 = new Sprite(tmp);
	    
	    //texture = new TextureRegion(tmp);
	    bullets = new ArrayList<Bullet>();
	    
	    walkSheet = new Texture(Gdx.files.internal("data/Animations/Player/Idle/player_idle.png")); 
	    
	    TextureRegion[][] tmp = TextureRegion.split(walkSheet, walkSheet.getWidth() / 
	            8, walkSheet.getHeight() / FRAME_COLS);
	    idleFramesRight = new TextureRegion[FRAME_ROWS * FRAME_COLS];
	    idleFramesLeft = new TextureRegion[FRAME_ROWS * FRAME_COLS];
    
	    int index = 0;
	    for (int i = 0; i < FRAME_COLS; i++) {
	            for (int j = 0; j < FRAME_ROWS; j++) {
	                    idleFramesRight[index] = tmp[i][j];
	                    index++;
	            }
	    }
	    
	    for (int i = 0; i < idleFramesLeft.length; i++) {
	            idleFramesLeft[i] = new TextureRegion(idleFramesRight[i]);
	            idleFramesLeft[i].flip(true, false);
	    }
	    
	    idleAnimationRight = new Animation(0.15f, idleFramesRight);
	    idleAnimationLeft = new Animation(0.15f, idleFramesLeft);
	    stateTime = 0f; 
	            
	    Gdx.input.setInputProcessor(this);
	    renderer = new ShapeRenderer();
	    font = new BitmapFont();
    
    }
    
    /**
     * Returns the Instance of the Controller
     * @return the Instance of the CharacterController class.
     */
    public static CharacterController getInstance() {
            return INSTANCE;
    }
    
    /**
     * Changes the current position of the Main Character with a new position.
     * @param position the new position of the Character.
     */
    public void changePosition(Vector2 position) {
            charBody.setTransform(position, 0);
    }
    
    /**
     * Returns the position of the Body
     * @return the position of the Body
     */
    public Vector2 getPosition() {
            return new Vector2(position.x - sprite.width / 2, position.y - sprite.height / 2);
    }
    
    /**
     * Sets the Initial values needed for the correct working of the controller
     * @param position the initial position for the character
     */
    public void initController(boolean debug, Vector2 position, OrthographicCamera camera, float unitScale, Vector2 RESPAWN) {
            this.position = position;
            this.unitScale  = unitScale;
            this.RESPAWN  = RESPAWN;
            this.debugMode = debug;
            this.attackDelta = 0.5f;
            
            sprite.x = position.x;
            sprite.y = position.y;
            sprite.height = ((2 *idleAnimationRight.getKeyFrame(0).getRegionHeight()) - (1 / unitScale )) / unitScale;
            sprite.width = ((2 *idleAnimationRight.getKeyFrame(0).getRegionWidth()) - (1 / unitScale )) / unitScale;

            
            BodyDef bodyDef = new BodyDef();
            bodyDef.type = BodyType.DynamicBody; //this will our a dynamic body
            bodyDef.angle = 0; //set the starting angle
            
            bodyDef.position.set(position.x + ( sprite.width / 2 ), position.y + (sprite.height / 2)); //set the starting position
            
            charBody = world.createBody(bodyDef); //adds the body to the world
            
            boxShape = new PolygonShape(); //the shape of the fixture
            boxShape.setAsBox((sprite.width / 2) - (1 / unitScale), sprite.height); // fixture dimension
              
            FixtureDef boxFixtureDef = new FixtureDef(); //new fixture
            boxFixtureDef.shape = boxShape; //assigns the shape to the fixture
            boxFixtureDef.density = 1; // density
            boxFixtureDef.friction = 0.0f;
    
            charBody.setFixedRotation(true);
            charBody.createFixture(boxFixtureDef); //adds the fixture to the Body
            boxFixtureDef.filter.groupIndex = CategoryGroup.CHARACTER;
            boxFixtureDef.filter.categoryBits = CategoryGroup.CHARACTER; //Character group
            
            PolygonShape footSensor = new PolygonShape();
            footSensor.setAsBox((sprite.width - (5 / unitScale)) / 2, 1 / unitScale, new Vector2(0, -(sprite.height) + (1 / unitScale)), 0);
            footFixture = new FixtureDef();
            footFixture.shape = footSensor;
            footFixture.density = 1;
            footFixture.friction = 0.0f;
            footFixture.isSensor = true;
            
            footFixture.filter.categoryBits = CategoryGroup.CHARACTER_FEET; //foot category
            footFixture.filter.groupIndex = CategoryGroup.CHARACTER;
            charBody.createFixture(footFixture);
    }
    
    /**
     * sets the debug mode enabled/disabled. (adds extra data to Box2DDebugRenderer)
     * @param state the new state (true/false)
     */
    public void setDebugMode(boolean state) {
            this.debugMode = state;
    }
    
    public boolean isGrounded() {
            return this.isGrounded;
    }
    
    public ArrayList<Bullet> getBullets() {
    	return this.bullets;
    }
    
    /**
     * Handles the Player Animations
     */
    public void AnimationHandler(SpriteBatch batch, float deltaTime) {
        stateTime += deltaTime;              

	    if ( facingRight ) {
	        currentFrame = idleAnimationRight.getKeyFrame(stateTime, true);
	    }else{
	        currentFrame = idleAnimationLeft.getKeyFrame(stateTime, true);
	    }

        Vector3 vector = new Vector3(position.x - (sprite.width), position.y - (sprite.height), 0);
        camera.project(vector);
            
        batch.draw(currentFrame, vector.x, vector.y, 0, 0, sprite.width, sprite.height,2 * unitScale, 2 * unitScale, 0);
    }
    
    /**
     * Handles the User input (keyboard now)
     * @param batch the SpriteBatch
     */
    private void keyHandler(SpriteBatch batch, float deltaTime) {
            
            Vector2 vel = charBody.getLinearVelocity(); //get the current linear Vel
            
            // sets the move states
            if(Gdx.input.isKeyPressed(Keys.A)) {
                    moveState = MoveStates.MS_LEFT;
                    facingRight = false;
                    vel.x = -MAX_VELOCITY;
                    if(isGrounded) {
                            if(Gdx.input.isKeyPressed(Keys.W)) {
                                    jump = true;
                                    moveState = MoveStates.MS_JUMP;
                                    vel.y = impulse;
                                    if(Gdx.input.isKeyPressed(Keys.SPACE)) {
                                            moveState = MoveStates.MS_ATTACK;
                                    }
                            }
                    }
            }else if(Gdx.input.isKeyPressed(Keys.D)) {
                    moveState = MoveStates.MS_RIGHT;
                    facingRight = true;
                    vel.x = MAX_VELOCITY;
                    if(isGrounded) {
                            if(Gdx.input.isKeyPressed(Keys.W)) {
                                    jump = true;
                                    moveState = MoveStates.MS_JUMP;
                                    vel.y = impulse;
                                    if(Gdx.input.isKeyPressed(Keys.SPACE)) {
                                            moveState = MoveStates.MS_ATTACK;
                                    }
                            }
                    }
            }else if(Gdx.input.isKeyPressed(Keys.W)){
                    if(isGrounded) {
                            moveState = MoveStates.MS_JUMP;
                            jump = true;
                            vel.y = impulse;
                    }
            }else{
                    moveState = MoveStates.MS_STOP;
                    vel.x = 0;
            }
            
            if(Gdx.input.isKeyPressed(Keys.SPACE)) {
                    moveState = MoveStates.MS_ATTACK;
                    if(!jump)
                            vel.x = 0;
            }

            charBody.setLinearVelocity( vel ); //applies the new linear vel        
            sprite.setPosition(new Vector2(charBody.getPosition().x - ( sprite.width / 2 ), charBody.getPosition().y - ( sprite.height / 2)));
            position = charBody.getPosition();
                            
            charBody.setAwake(true);
            
            //displays key state in debug mode
            if (debugMode) {
                    if (jump)
                            font.setColor(Color.GREEN);
                    else
                            font.setColor(Color.WHITE);
                    font.draw(batch,"[up]",Gdx.graphics.getWidth() - 80, 50);
                    
                    if (moveState.equals(MoveStates.MS_LEFT))
                            font.setColor(Color.GREEN);
                    else
                            font.setColor(Color.WHITE);
                    font.draw(batch,"[left]",Gdx.graphics.getWidth() - 110, 30);
                    
                    if (moveState.equals(MoveStates.MS_RIGHT))
                            font.setColor(Color.GREEN);
                    else
                            font.setColor(Color.WHITE);
                    font.draw(batch,"[right]", Gdx.graphics.getWidth() - 50, 30);
            
                    if (moveState.equals(MoveStates.MS_ATTACK))
                            font.setColor(Color.GREEN);
                    else
                            font.setColor(Color.WHITE);
                    font.draw(batch,"[attack]", Gdx.graphics.getWidth() - 200, 30);
                    
                    font.setColor(Color.WHITE);
                    font.draw(batch, "[facing ", Gdx.graphics.getWidth() - 200, 50);
                    if (facingRight)
                            font.draw(batch, "right]", Gdx.graphics.getWidth() - 155, 50);
                    else
                            font.draw(batch, "left]", Gdx.graphics.getWidth() - 155, 50);

                    if (tempAttack == 0) {
                    	font.setColor(Color.GREEN);
                    	font.draw(batch, "SHOOT", 10, 45);
                    }else{
                    	font.setColor(Color.WHITE);
                    	font.draw(batch, "Reloading.. " + (attackDelta - tempAttack), 10, 45);
                    }
                	font.setColor(Color.WHITE);
                    font.draw(batch, "[" + (int) position.x + "," + (int) position.y + "]", Gdx.graphics.getWidth() - 81, 30);
            }
    }
    
    /**
     * Checks when the character falls from the screen.
     */
    private void deadHandler() {
            if ( position.y < 1) {
                    moveState = MoveStates.MS_STOP;
                    charBody.setTransform(RESPAWN, 0);
            }
    }
    
    /**
     * Handles the shoot logic
     * @param deltaTime deltaTime
     * @param batch spritebatch
     */
    private void shootHandler(float deltaTime, SpriteBatch batch) {
            Vector2 speed = null;
            Texture texture = new Texture(Gdx.files.internal("data/Animations/Bullets/bullet.png"));
            Vector2 vector = null;
            if (moveState.equals(MoveStates.MS_ATTACK)) {
                    if (facingRight) {
                            vector = new Vector2( position.x + ( ( texture.getWidth() + unitScale ) / (2 * unitScale) ),
                                            ( position.y + ( (texture.getHeight() - unitScale) / (2 * unitScale))) );
                            speed = new Vector2(8 / unitScale, 0);
                    }else{
                            vector = new Vector2(position.x - (texture.getWidth() /(unitScale)),
                                            ( position.y + ( (texture.getHeight() - unitScale) / ( 2 * unitScale ))) );
                            speed = new Vector2(-8 / unitScale, 0);
                    }
                    
                    if (tempAttack == 0) {
                            Bullet bullet = new Bullet(batch, deltaTime, vector, texture, 10f, speed, 1f, unitScale);
                            bullets.add(bullet);
                            tempAttack += deltaTime;
                    }
            }
            
            if ( tempAttack >= attackDelta) {
                    tempAttack = 0;
            }
            if ( tempAttack > 0) {
                tempAttack += deltaTime;
            }
    }
    
    @Override
    public boolean keyDown(int keycode) {
            return true;
    }
    
    @Override
    public boolean keyUp(int keycode) {
            moveState = MoveStates.MS_STOP;
            //jump = false;
            return true;
    }
    
    /**
     * Draws the MainCharacter
     */ 
    public void render(SpriteBatch batch, float deltaTime) {
            if(deltaTime == 0) return;
            position = charBody.getPosition();
            
            keyHandler(batch, deltaTime);
            shootHandler(deltaTime, batch);
            AnimationHandler(batch, deltaTime);
            
            for(Bullet bull : bullets) {
            	if(bull.isDestroyable()) {
            		worldController.addBulletForDestroy(bull);
            	}else{
                	bull.render(batch, deltaTime);
            	}
            }
            
            deadHandler();
            
            font.setColor(Color.WHITE);
            font.draw(batch, "[" + (int) charBody.getPosition().x + "," + (int) charBody.getPosition().y + "]", charBody.getPosition().x / unitScale, charBody.getPosition().y / unitScale);
    }

    
    public void beginContact(Contact contact) {
    	//System.out.println("contactA:" + contact.getFixtureA().getFilterData().categoryBits + " contactB:" + contact.getFixtureB().getFilterData().categoryBits);
        if ((contact.getFixtureA().getFilterData().categoryBits == CategoryGroup.CHARACTER_FEET)
        		&&(contact.getFixtureB().getFilterData().categoryBits == CategoryGroup.GROUND)) {                                
            isGrounded = true;
            jump = false;
        }
    }
    
    public void endContact(Contact contact) {
        if ((contact.getFixtureA().getFilterData().categoryBits == CategoryGroup.CHARACTER_FEET)
        		&&(contact.getFixtureB().getFilterData().categoryBits == CategoryGroup.GROUND)) {
            isGrounded = false;
        }
    }
}