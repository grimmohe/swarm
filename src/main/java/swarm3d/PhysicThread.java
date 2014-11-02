package swarm3d;

import java.util.List;

import javax.vecmath.Matrix4f;
import javax.vecmath.Quat4f;
import javax.vecmath.Vector3f;

import com.bulletphysics.collision.broadphase.BroadphaseInterface;
import com.bulletphysics.collision.broadphase.DbvtBroadphase;
import com.bulletphysics.collision.dispatch.CollisionDispatcher;
import com.bulletphysics.collision.dispatch.DefaultCollisionConfiguration;
import com.bulletphysics.collision.shapes.CollisionShape;
import com.bulletphysics.collision.shapes.SphereShape;
import com.bulletphysics.collision.shapes.StaticPlaneShape;
import com.bulletphysics.dynamics.DiscreteDynamicsWorld;
import com.bulletphysics.dynamics.RigidBody;
import com.bulletphysics.dynamics.RigidBodyConstructionInfo;
import com.bulletphysics.dynamics.constraintsolver.SequentialImpulseConstraintSolver;
import com.bulletphysics.linearmath.DefaultMotionState;
import com.bulletphysics.linearmath.Transform;

public class PhysicThread extends Thread {

	DiscreteDynamicsWorld dynamicsWorld;
	RigidBody fallRigidBody;
	List<Box> boxes;
	boolean stopIssued = false;
	
	public PhysicThread(List<Box> boxes) {

		this.boxes = boxes;
		
		// Build the broadphase
		BroadphaseInterface broadphase = new DbvtBroadphase();

	    // Set up the collision configuration and dispatcher
		DefaultCollisionConfiguration collisionConfiguration = new DefaultCollisionConfiguration();
		CollisionDispatcher dispatcher = new CollisionDispatcher(collisionConfiguration);

	    // The actual physics solver
		SequentialImpulseConstraintSolver solver = new SequentialImpulseConstraintSolver();

	    // The world.
	    dynamicsWorld = new DiscreteDynamicsWorld(dispatcher, broadphase, solver, collisionConfiguration);
	    dynamicsWorld.setGravity(new Vector3f(0, -10, 0));


	    CollisionShape groundShape = new StaticPlaneShape(new Vector3f(0, 1, 0), 0.25f);
	    CollisionShape fallShape = new SphereShape(1);


	    DefaultMotionState groundMotionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, 0, 0), 1)));
	    RigidBodyConstructionInfo groundRigidBodyCI = new RigidBodyConstructionInfo(0, groundMotionState, groundShape, new Vector3f(0, 0, 0));
	    groundRigidBodyCI.restitution = 0.5f;
	    RigidBody groundRigidBody = new RigidBody(groundRigidBodyCI);

	    dynamicsWorld.addRigidBody(groundRigidBody);

	    DefaultMotionState fallMotionState = new DefaultMotionState(new Transform(new Matrix4f(new Quat4f(0, 0, 0, 1), new Vector3f(0, 50, 0), 1)));

	    Vector3f fallInertia = new Vector3f(0, 0, 0);
        fallShape.calculateLocalInertia(1F, fallInertia);

        RigidBodyConstructionInfo fallRigidBodyCI = new RigidBodyConstructionInfo(1, fallMotionState, fallShape, fallInertia);
        fallRigidBodyCI.restitution = 0.75f;
        fallRigidBody = new RigidBody(fallRigidBodyCI);
        dynamicsWorld.addRigidBody(fallRigidBody);
		
	}

	@Override
	public void run() {
		
        while (!stopIssued) {

            dynamicsWorld.stepSimulation(1 / 60.f, 10);

//            System.out.println("sphere height: " + fallRigidBody.getMotionState().getWorldTransform(new Transform()).origin);
            boxes.get(0).setY( fallRigidBody.getMotionState().getWorldTransform(new Transform()).origin.y);
        }
	}
	
	public RigidBody getFallRigidBody() {
		return fallRigidBody;
	}
	
	public void issueStop() {
		stopIssued = true;
	}
	
}
