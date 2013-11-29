package es.sorikaStudio.scroller2d.controller;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

/**
 * Class for listening for collisions.
 * @author JuanMa
 *
 */
public class CollisionListener implements ContactListener {

        @Override
        public void beginContact(Contact contact) {
        	CharacterController.getInstance().beginContact(contact);

        }

        @Override
        public void endContact(Contact contact) {
        	CharacterController.getInstance().endContact(contact);
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
                // TODO Auto-generated method stub
                
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
                // TODO Auto-generated method stub
                
        }
}
