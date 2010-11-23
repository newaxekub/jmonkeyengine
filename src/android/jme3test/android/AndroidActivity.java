
/*
 *
 * Android Activity for OpenGL ES2 based tests
 * requires Android 2.2+
 *
 * created: Mon Nov  8 00:08:07 EST 2010
 */

package jme3test.android;

import android.app.Activity;
import android.content.res.Resources;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import com.jme3.R;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeSystem;
import com.jme3.system.android.OGLESContext;
import com.jme3.app.Application;


public class AndroidActivity extends Activity {

	private final static java.util.logging.Logger logger = java.util.logging.Logger.getLogger(AndroidActivity.class.getName());


	private OGLESContext ctx;
	private GLSurfaceView view;


	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);

		JmeSystem.setResources(getResources());

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		WindowManager.LayoutParams.FLAG_FULLSCREEN);

		AppSettings settings = new AppSettings(true);

		String appClass = "jme3test.android.SimpleTexturedTest";

		Application app = null;

		try {
			Class<? extends Application> clazz = (Class<? extends Application>) Class.forName(
				appClass
			);
			app = clazz.newInstance();

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		logger.info("setting settings ...");
		app.setSettings(settings);
		logger.info("setting settings ... done.");

		logger.info("starting app ...");
		app.start();
		logger.info("starting app ... done.");

		logger.info("creating context ...");
		ctx = (OGLESContext) app.getContext();
		logger.info("creating context ... done.");

		logger.info("creating view ...");
		view = ctx.createView(this);
		logger.info("creating view ... done.");

		logger.info("setting content view ...");
		setContentView(view);
		logger.info("setting content done ...");
	}

	@Override
	protected void onResume() {
		logger.info("onResume ...");
		super.onResume();
		logger.info("view.onResume ...");

		view.onResume();

		logger.info("view.onResume ... done.");
		logger.info("onResume ... done.");
	}

	@Override
	protected void onPause() {
		super.onPause();
		view.onPause();
	}

//	@Override
//	protected void onDestroy(){
//		super.onDestroy();

//		Debug.stopMethodTracing();
//	}

}

