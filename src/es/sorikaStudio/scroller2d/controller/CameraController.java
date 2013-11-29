package es.sorikaStudio.scroller2d.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;

/**
 * Controller for Camera class.
 * @author JuanMa
 *
 */
public class CameraController {
        
        private OrthographicCamera camera;
        private CharacterController characterController;
        private boolean isGrounded = true;
        
        private ShapeRenderer renderer;
        
        private float topMargin = 0;
        private float bottomMargin = 0;
        private float rightMargin = 0;
        private float leftMargin = 0;
        
        private enum Moving{nope, top, down, right, left };
        private Moving moving = Moving.nope;
        private float unitScale;

        private static CameraController INSTANCE = new CameraController();
        
        private CameraController() {
                camera = new OrthographicCamera(1, Gdx.graphics.getHeight() / Gdx.graphics.getWidth());
                renderer = new ShapeRenderer();

                //stageController = StageController.getInstance();
        }
        
        /**
         * Inits the Controller class
         * @param characterController character Controller
         */
        public void initController(CharacterController characterController, float unitScale) {
                this.unitScale = unitScale;
                this.characterController = characterController;
        }
        
        public static CameraController getInstance() {
                return INSTANCE;
        }
        
        public OrthographicCamera getCamera() {
                return camera;
        }
        
        /**
         * The camera follows the character with a smooth move.
         */
        public void cameraControl() {
                float targetPos = characterController.getPosition().y;
                float posX = characterController.getPosition().x;
                
                float lerp = 0.1f;
                
                if (moving.equals(Moving.nope)) {
                        leftMargin = 0;
                        rightMargin = 0;
                        topMargin = camera.position.y + ( camera.viewportHeight / 4 );
                        bottomMargin = camera.position.y - ( 7 * camera.viewportHeight / 16 );
                        isGrounded = characterController.isGrounded();
                        if (isGrounded) {
                                if (targetPos > topMargin) {
                                        moving  = Moving.top;
                                }else if (targetPos < bottomMargin) {
                                        moving = Moving.down;
                                }
                        }
                }
                
                if (moving.equals(Moving.top)) {
                        float position = camera.position.y;
                        float aux = ((( targetPos ) - position) * lerp);
                        position += aux;
                        camera.position.set(posX, position, 0);
                        if ( aux < 0.0001f ) {
                                moving = Moving.nope;
                        }
                }else if(moving.equals(Moving.down)) {
                        float position = camera.position.y;
                        float aux = ((targetPos - position) * lerp);
                        position += aux;
                        camera.position.set(posX, position, 0);
                        if ( aux > -0.001f ) {
                                moving = Moving.nope;
                        }
                }
                
                if ((characterController.getPosition().y - (camera.viewportHeight / 2)) < 0 ) {
                        moving = Moving.nope;
                        camera.position.y = camera.viewportHeight / 2; 
                }
                if (characterController.getPosition().x  < (camera.viewportWidth / 2)) {
                        moving = Moving.nope;
                        posX = camera.viewportWidth / 2;
                }
                
                camera.translate((posX - camera.position.x) * lerp, 0);
        }
        
        /**
         * Renders 4 red lines representing the margins for the smooth camera movement.
         */
        public void shapeRender() {
                renderer.setProjectionMatrix(camera.combined);
                renderer.begin(ShapeType.Line);
                renderer.setColor(Color.RED);
                                
                //top side
                renderer.line( camera.position.x - ( camera.viewportWidth / 2 ),
                                camera.position.y + ( camera.viewportHeight / 4 ),
                                ( camera.position.x + camera.viewportWidth / 2 ),
                                camera.position.y + ( camera.viewportHeight / 4 ));
                //bottom side
                renderer.line( camera.position.x - ( camera.viewportWidth / 2 ),
                                camera.position.y - ( 7 * camera.viewportHeight / 16 ),
                                ( camera.position.x + camera.viewportWidth / 2 ),
                                camera.position.y - ( 7 * camera.viewportHeight / 16 ));
                //right side
                renderer.line( camera.position.x + ( 3 * camera.viewportWidth / 8 ),
                                camera.position.y - ( camera.viewportHeight / 2 ),
                                camera.position.x + ( 3 * camera.viewportWidth / 8 ),
                                camera.position.y + ( camera.viewportHeight / 2 ));
                //left side
                renderer.line( camera.position.x - ( 3 * camera.viewportWidth / 8 ),
                                camera.position.y - ( camera.viewportHeight / 2),
                                camera.position.x - ( 3 * camera.viewportWidth / 8 ),
                                camera.position.y + ( camera.viewportHeight / 2));
                renderer.end();
        }
}