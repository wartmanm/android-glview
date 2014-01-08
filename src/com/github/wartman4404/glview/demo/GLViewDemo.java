package com.github.wartman4404.glview.demo;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.github.wartman4404.glview.GLObjectView;
import com.github.wartman4404.glview.R;
import com.github.wartman4404.glview.animation.GLAnimation;
import com.github.wartman4404.glview.animation.GLAnimation.AnimateOngoingInstance;
import com.github.wartman4404.glview.animation.RotateAnimation;
import com.github.wartman4404.glview.gl.BoundingBox;
import com.github.wartman4404.glview.material.MaterialLoader;
import com.github.wartman4404.glview.time.PassiveStopwatch;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;

public class GLViewDemo extends Activity {

    private GLObjectView mGLView;
    
    private PassiveStopwatch timer;
    
    private long getTime() {
    	return System.currentTimeMillis();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mGLView = (GLObjectView) findViewById(R.id.glview);
        mGLView.load();
        BoundingBox bounds = mGLView.getRenderer().getBounds();
        float centerX = bounds.centerX(), centerY = bounds.centerY(), centerZ = bounds.centerZ();
        final GLAnimation spin = new RotateAnimation(centerX, centerY, centerZ, 0, 1, 0, 0, 360);
        timer = new PassiveStopwatch();
        AnimateOngoingInstance rotateAnim = spin.new AnimateOngoingInstance(timer, 3000);
        String[] names = mGLView.getRenderer().getElementNames();
        for (String name: names) {
        	mGLView.getRenderer().addAnimation(name, rotateAnim);
        }
        mGLView.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				timer.reset(getTime());
				return true;
			}
		});
        mGLView.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				long time = getTime();
				if (timer.isRunning()) {
					Log.i("onclick", "pausing anims");
					timer.stop(time);
				} else {
					Log.i("onclick", "starting anims");
					timer.start(time);
				}
			}
        });
        timer.start(getTime());
    }

	@Override
    protected void onPause() {
        super.onPause();
        mGLView.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        mGLView.onResume();
    }
}
    

class AssetMaterialLoader implements MaterialLoader {
	final Context context;
	public AssetMaterialLoader(Context context) {
		this.context = context;
	}

	@Override
	public InputStream getMaterialStream(String name) throws FileNotFoundException, IOException {
		return context.getAssets().open(name);
	}
	
}