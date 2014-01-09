GLView is a widget intended to make incorporating 3D objects in your app as painless as possible, on the level of including 2D images with ImageView.  

Some sample [layout XML](https://github.com/wartman4404/android-glview/blob/demo/res/layout/main.xml):

    <com.github.wartman4404.glview.GLObjectView
     android:id="@+id/glview"
     android:layout_width="match_parent"
     android:layout_height="match_parent"
     app:light_ambient_intensity=".2"
     app:light_dir_x="-0.5"
     app:light_dir_y="-0.5"
     app:light_dir_z="0.0"
     app:light_directional_intensity=".5"
     app:src_material="@string/teapot_material"
     app:src_object="@string/teapot_object" />

And the corresponding [code](https://github.com/wartman4404/android-glview/blob/demo/src/com/github/wartman4404/glview/demo/GLViewDemo.java):

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        GLObjectView glview = (GLObjectView) findViewById(R.id.glview);
        glview.load();
    }

That's all you need!
