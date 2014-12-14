package cz.hofmanladislav.paaplayer;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;

public class MyDragShadowBuilder extends View.DragShadowBuilder {

    private Drawable mShadow;

    public MyDragShadowBuilder(View v) {
        super(v);
        //mShadow = v.getResources().getDrawable(R.drawable.btn_default_pressed); // nastavení pozadí row_song při drag and drop
        mShadow = v.getResources().getDrawable(R.drawable.songrow);
        mShadow.setCallback(v);
        mShadow.setBounds(0, 0, v.getWidth(), v.getHeight());
    }

    @Override
    public void onDrawShadow(Canvas canvas) {
        super.onDrawShadow(canvas);
        mShadow.draw(canvas);
        getView().draw(canvas);
    }
}
