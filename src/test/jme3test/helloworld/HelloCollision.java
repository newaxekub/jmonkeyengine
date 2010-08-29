package jme3test.helloworld;

import com.jme3.app.SimpleBulletApplication;
import com.jme3.asset.plugins.ZipLocator;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CompoundCollisionShape;
import com.jme3.bullet.nodes.PhysicsCharacterNode;
import com.jme3.bullet.nodes.PhysicsNode;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.light.DirectionalLight;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Node;
import com.jme3.scene.Spatial;

/**
 * Example 9 - How to make walls and floors solid.
 * This version uses Physics and a custom Action Listener.
 * @author normen, with edits by Zathras
 */
public class HelloCollision
  extends SimpleBulletApplication
  implements ActionListener {

  private Spatial gameScene;
  private PhysicsCharacterNode player;
  private Vector3f walkDirection = new Vector3f();
  private boolean left = false, right = false, up = false, down = false;

  public static void main(String[] args) {
    HelloCollision app = new HelloCollision();
    app.start();
  }

  public void simpleInitApp() {
    renderer.setBackgroundColor(ColorRGBA.Cyan);
    
    // We re-use the flyby camera for rotation, while positioning is handled by physics
    flyCam.setMoveSpeed(100);
    setupKeys();

    // We add a light so we see the scene
    DirectionalLight dl = new DirectionalLight();
    dl.setColor(ColorRGBA.White.clone().multLocal(2));
    dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalize());
    rootNode.addLight(dl);

    // We load the scene from the zip file and adjust its size.
    assetManager.registerLocator("town.zip", ZipLocator.class.getName());
    gameScene = assetManager.loadModel("main.scene");
    gameScene.setLocalScale(2f); 

    // We set up collision detection by creating a 
    // compound collision shape and a physics node for the scene.
    CompoundCollisionShape sceneShape = CollisionShapeFactory.createMeshCompoundShape((Node) gameScene);
    PhysicsNode levelNode = new PhysicsNode(gameScene, sceneShape, 0);

    // Here we set up collision detection for the player by creating
    // a capsule collision shape and a physics character node.
    // The physics character node offers extra settings for
    // size, stepheight, jumping, falling, and gravity.
    // We also put the player in its starting position.
    player = new PhysicsCharacterNode(new CapsuleCollisionShape(1.5f, 6f, 1), .05f);
    player.setJumpSpeed(20);
    player.setFallSpeed(30);
    player.setGravity(30);
    player.setLocalTranslation(new Vector3f(0, 10, 0));
    player.updateGeometricState();

    // We attach the scene and the player to the rootnode and the physics space,
    // to make them appear in the game world.
    rootNode.attachChild(levelNode);
    rootNode.attachChild(player);
    rootNode.updateGeometricState();
    getPhysicsSpace().add(levelNode);
    getPhysicsSpace().add(player);
  }

  /** We over-write some navigational key mappings here, so we can
   * add physics-controlled walking and jumping: */
  private void setupKeys() {
    inputManager.addMapping("Lefts",  new KeyTrigger(KeyInput.KEY_A));
    inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
    inputManager.addMapping("Ups",    new KeyTrigger(KeyInput.KEY_W));
    inputManager.addMapping("Downs",  new KeyTrigger(KeyInput.KEY_S));
    inputManager.addMapping("Jumps",  new KeyTrigger(KeyInput.KEY_SPACE));
    inputManager.addListener(this, "Lefts");
    inputManager.addListener(this, "Rights");
    inputManager.addListener(this, "Ups");
    inputManager.addListener(this, "Downs");
    inputManager.addListener(this, "Jumps");
  }

  /** These are our custom actions triggered by key presses.
   * We do not walk yet, we just keep track of the direction the user pressed. */
  public void onAction(String binding, boolean value, float tpf) {
    if (binding.equals("Lefts")) {
      if (value) { left = true; }  else { left = false; }
    } else if (binding.equals("Rights")) {
      if (value) { right = true; } else { right = false; }
    } else if (binding.equals("Ups")) {
      if (value) { up = true; }    else { up = false; }
    } else if (binding.equals("Downs")) {
      if (value) { down = true; }  else { down = false; }
    } else if (binding.equals("Jumps")) {
      player.jump();
    }
  }

  /**
   * This is the main event loop--walking happens here.
   * We check in which direction the player is walking by interpreting
   * the camera direction forward (camDir) and to the side (camLeft).
   * The setWalkDirection() command is what lets a physics-controlled player walk.
   * We also make sure here that the camera moves with player.
   */
  @Override
  public void simpleUpdate(float tpf) {
    Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
    Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
    walkDirection.set(0, 0, 0);
    if (left)  { walkDirection.addLocal(camLeft); }
    if (right) { walkDirection.addLocal(camLeft.negate()); }
    if (up)    { walkDirection.addLocal(camDir); }
    if (down)  { walkDirection.addLocal(camDir.negate()); }
    player.setWalkDirection(walkDirection);
    cam.setLocation(player.getLocalTranslation());
  }
}
