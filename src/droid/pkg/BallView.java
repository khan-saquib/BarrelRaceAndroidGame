package droid.pkg;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.view.View;

public class BallView extends View {

	public float mX;
    public float mY;
    private final int mR;
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    public boolean error = false;
    
    //construct new ball object
    public BallView(Context context, float x, float y, int r) {
        super(context);
        //color hex is [transparency][red][green][blue]
        mPaint.setColor(0xFF33B5E5);//not transparent. color is green
        this.mX = x;
        this.mY = y;
        this.mR = r; //radius
    }
    
    public BallView(Context context, PointF point, int r) {
        super(context);
        //color hex is [transparency][red][green][blue]
        mPaint.setColor(0xFF33B5E5);//not transparent. color is green
        this.mX = point.x;
        this.mY = point.y;
        this.mR = r; //radius
    }
    
    
    //called by invalidate()	
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(!error) {
        	mPaint.setColor(0xFF33B5E5);
        	canvas.drawCircle(mX, mY, mR, mPaint);
        }
        else
        {
        	mPaint.setColor(0xFFFF0000);
        	canvas.drawCircle(mX, mY, mR, mPaint);
        }
        
    } 
}
