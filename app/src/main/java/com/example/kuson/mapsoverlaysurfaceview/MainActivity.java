package com.example.kuson.mapsoverlaysurfaceview;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView(){
        Button btn1 = (Button) findViewById(R.id.button1);
        btn1.setOnClickListener(clickListener);
        Button btn2 = (Button) findViewById(R.id.button2);
        btn2.setOnClickListener(clickListener);

        ImageView heart = (ImageView) findViewById(R.id.heart);
        Animator animator_scale = AnimatorInflater.loadAnimator(this, R.animator.scale);
        animator_scale.setTarget(heart);
        animator_scale.start();

        TextView description = (TextView) findViewById(R.id.description);
        Animator animator_alpha = AnimatorInflater.loadAnimator(this, R.animator.alpha);
        animator_alpha.setTarget(description);
        animator_alpha.start();
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            switch (view.getId()){
                case R.id.button1:
                    intent.setClass(MainActivity.this, MapsDemoActivity.class);
                    startActivity(intent);
                    break;

                case R.id.button2:

                    break;
            }
        }
    };

}
