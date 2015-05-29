package droid.pkg;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.PointF;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.Time;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;


public class TiltBallActivity extends Activity {
	
	BallView mHorse = null;
	BallView mBarellViews[] ;
	Handler RedrawHandler = new Handler(); //so redraw occurs in main thread
	Timer mTmr = null;
	TimerTask mTsk = null;
	int offsetHeight = 15, offsetWidth = 15;
	int mScreenLeft = 15 ,mScreenRight = 703 ,mScreenTop = 15, mScreenBottom = 724;
	int mGateLeft = 338, mGateRight = 380;
	PointF mBarrelLocations[]; 
	Time mTime = null;
	PointF mBallPos, mBallSpd;
	TextView timer;
	public long mGameStartTime;
	boolean mGameEnd = true,mGameStart = false ;
	double mTimeOffset;
	String mTimeString;
	List<PointF> mHorsePath;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE); //hide title bar
        getWindow().setFlags(0xFFFFFFFF,
        		LayoutParams.FLAG_FULLSCREEN|LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //create pointer to main screen
        final FrameLayout mainView = (FrameLayout) findViewById(R.id.main_view);
        final Button startButton = (Button) findViewById(R.id.btn_Start);
        timer = (TextView)findViewById(R.id.textTimer);
        
        startButton.setOnClickListener(startHandler);
        
      //Create the barrel objects and add it to the view
        mBarrelLocations = new PointF[3];
        mBarrelLocations[0] = new PointF(595,500);
        mBarrelLocations[1] = new PointF(365,140);
        mBarrelLocations[2] = new PointF(135,500);
        mBarellViews = new BallView[3];
        mBarellViews[0] = new BallView(this, mBarrelLocations[0], 20);
        mBarellViews[1] = new BallView(this, mBarrelLocations[1], 20);
        mBarellViews[2] = new BallView(this, mBarrelLocations[2], 20);
        
        for(int i=0;i<3;i++) {
        	mainView.addView(mBarellViews[i]);
        	mBarellViews[0].invalidate();
        }
    	
        mBallPos = new PointF();
    	mBallSpd = new PointF();
        
        //SET variables for ball position and speed
        mBallPos.x = (mGateLeft + mGateRight)/2; 
        mBallPos.y = mScreenBottom - 10; 
        mBallSpd.x = 0;
        mBallSpd.y = 0; 
        
        //create initial ball
        mHorse = new BallView(this,mBallPos.x,mBallPos.y,10);
                
        mainView.addView(mHorse); //add ball to main screen
        mHorse.invalidate(); //call onDraw in BallView
               		
        //Attach accelerometer listener
        attachAccelerometerListener();
        
        //listener for touch event 
        mainView.setOnTouchListener(new android.view.View.OnTouchListener() {
	        public boolean onTouch(android.view.View v, android.view.MotionEvent e) {
	        	//set ball position based on screen touch
	        	if((Math.abs((mBallPos.x - e.getX()))) < 10 && (Math.abs(mBallPos.y - e.getY()) < 10) && mGameStart == false) 
	        		startTheGame();
	        	return true;
	        }});
           
    }
    
    View.OnClickListener startHandler = new View.OnClickListener() {
        public void onClick(View v) {
        	if(mGameStart == false && mGameEnd == true)
        		startTheGame();
        }
      };
    
    
    /**
     * This function is called to start the game. Runs the timer that reads from the accelerometer
     */
    protected void startTheGame() {
    	mGameStart = true;
    	mGameEnd = false;
    	mGameStartTime = System.currentTimeMillis();
    	timer.setText("0:0:0");
    	mTimeOffset = 0;
    	mHorsePath = new ArrayList<PointF>();
		//Clean the UI to default before starting the game
        mBallPos.x = (mGateLeft + mGateRight)/2; 
        mBallPos.y = mScreenBottom - 10; 
        mBallSpd.x = 0;
        mBallSpd.y = 0; 
        mHorse.mX = mBallPos.x; 
		mHorse.mY = mBallPos.y;
		mHorsePath.add(new PointF(mBallPos.x, mBallPos.y));
		mHorse.invalidate();
		for(int i=0;i<3;i++) {
        	mBarellViews[i].error = false;
        	mBarellViews[i].invalidate();
        }
		
		runTimer();
		
	}

    
    
    private void endTheGame(String reason) {
    	
    	
    	mGameStart = false;
    	mGameEnd = true;
    	mTimeOffset = 0;
    	//timer.setText("00:00:000");
    	stopTimer();
    	
    	
    	
    	
    	List<int[]> listOfLoops = new ArrayList<int[]>();
    	int counter=0;
    	int[] temp ;
    	for(int i = 1; i< mHorsePath.size(); i++) 
    	{
    		for(int j= i + 10;j<mHorsePath.size();j++)
    		{
    			if(distance(mHorsePath.get(i),mHorsePath.get(j))< 3)
    			{
    				temp = new int[2];
    				temp[0] = i;
    				temp[1] = j;
    				listOfLoops.add(temp);
    				j = j+5;
    			}
    		}	
    	}
    	
    	boolean barrel1=false, barrel2 = false, barrel3 = false;
    	for(int i = 0; i<listOfLoops.size(); i++)
    	{
    		if(checkIfLoopContainsBarrel1(listOfLoops.get(i)) && checkIfLoopContainsBarrel2(listOfLoops.get(i)))
    			continue;
    		if(checkIfLoopContainsBarrel2(listOfLoops.get(i)) && checkIfLoopContainsBarrel3(listOfLoops.get(i)))
    			continue;
    		if(checkIfLoopContainsBarrel1(listOfLoops.get(i)) && checkIfLoopContainsBarrel3(listOfLoops.get(i)))
    			continue;
    		   		
    		if(checkIfLoopContainsBarrel1(listOfLoops.get(i)))
    			barrel1 = true;
    		if(checkIfLoopContainsBarrel2(listOfLoops.get(i)))
    			barrel2 = true;
    		if(checkIfLoopContainsBarrel3(listOfLoops.get(i)))
    			barrel3 = true;
    	}
    	if(barrel1 && barrel2 && barrel3)
    		gameCompleted();
    	else
    		gameFailed(reason);
    	// TODO  Calculate the loops made condition
	}
    
    private void gameFailed(String reason) {
		// TODO Auto-generated method stub
		
	}



	private void gameCompleted() {
		// TODO Auto-generated method stub
		
	}



	private boolean checkIfLoopContainsBarrel1(int[] endPoints) {
    	
    	PointF up,bottom,left,right;
    	up = bottom = left = right = mHorsePath.get(endPoints[0]);
    	for(int i = endPoints[0] ; i<= endPoints[1]; i++) 
    	{
    		if(up.y>mHorsePath.get(i).y)
    			up = mHorsePath.get(i);
    		if(bottom.y<mHorsePath.get(i).y)
    			bottom = mHorsePath.get(i);
        	if(left.x<mHorsePath.get(i).x)
        		left = mHorsePath.get(i);
        	if(right.y>mHorsePath.get(i).y)
        		right = mHorsePath.get(i);
    	}
		if(up.y<mBarrelLocations[0].y && bottom.y > mBarrelLocations[0].y && left.x<mBarrelLocations[0].x && right.x > mBarrelLocations[0].x)
			return true;
		else
			return false;
	}
    
	private boolean checkIfLoopContainsBarrel2(int[] endPoints) {
	    	
	    	PointF up,bottom,left,right;
	    	up = bottom = left = right = mHorsePath.get(endPoints[0]);
	    	for(int i = 0; i< endPoints.length; i++) 
	    	{
	    		if(up.y>mHorsePath.get(i).y)
	    			up = mHorsePath.get(i);
	    		if(bottom.y<mHorsePath.get(i).y)
	    			bottom = mHorsePath.get(i);
	        	if(left.x<mHorsePath.get(i).x)
	        		left = mHorsePath.get(i);
	        	if(right.y>mHorsePath.get(i).y)
	        		right = mHorsePath.get(i);
	    	}
			if(up.y<mBarrelLocations[1].y && bottom.y > mBarrelLocations[1].y && left.x<mBarrelLocations[1].x && right.x > mBarrelLocations[1].x)
				return true;
			else
				return false;
		}
	
	private boolean checkIfLoopContainsBarrel3(int[] endPoints) {
		
		PointF up,bottom,left,right;
		up = bottom = left = right = mHorsePath.get(endPoints[0]);
		for(int i = 0; i< endPoints.length; i++) 
		{
			if(up.y>mHorsePath.get(i).y)
				up = mHorsePath.get(i);
			if(bottom.y<mHorsePath.get(i).y)
				bottom = mHorsePath.get(i);
	    	if(left.x<mHorsePath.get(i).x)
	    		left = mHorsePath.get(i);
	    	if(right.y>mHorsePath.get(i).y)
	    		right = mHorsePath.get(i);
		}
		if(up.y<mBarrelLocations[2].y && bottom.y > mBarrelLocations[2].y && left.x<mBarrelLocations[2].x && right.x > mBarrelLocations[2].x)
			return true;
		else
			return false;
	}

	private void stopTimer() {
    	runOnUiThread(new Runnable() {
            @Override
            public void run() {
            	if(mTmr != null) {
	            	mTmr.cancel(); //kill\release timer (our only background thread)
	            	mTmr = null;
	            	mTsk = null;
            	}
            }
        });
	}



	/**
     * This function attaches the accelerometer a listener that changes the mBallSpd values 
     */
	private void attachAccelerometerListener() {
    	//listener for accelerometer, use anonymous class for simplicity
        ((SensorManager)getSystemService(Context.SENSOR_SERVICE)).registerListener(
    		new SensorEventListener() {    
    			@Override  
    			public void onSensorChanged(SensorEvent event) {  
    			    //set ball speed based on phone tilt (ignore Z axis)
    					
    				mBallSpd.x = -event.values[0];
    				mBallSpd.y = event.values[1];
    				//timer event will redraw ball
    			}
        		@Override  
        		public void onAccuracyChanged(Sensor sensor, int accuracy) {} //ignore this event
        	},
        	((SensorManager)getSystemService(Context.SENSOR_SERVICE))
        	.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0), SensorManager.SENSOR_DELAY_NORMAL);
	}

	//listener for menu button on phone
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add("Exit"); //only one menu item
        return super.onCreateOptionsMenu(menu);
    }
    
    //listener for menu item clicked
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	// Handle item selection    
    	if (item.getTitle() == "Exit") //user clicked Exit
    		finish(); //will call onPause
   		return super.onOptionsItemSelected(item);    
    }
    
    
    @Override
    public void onPause() //app moved to background, stop background threads
    {	
    	
    	if(mTmr != null) {
    		mTmr.cancel(); //kill\release timer (our only background thread)
    		mTmr = null;
    		mTsk = null;
    	}
    	super.onPause();
    }
    
    @Override
    public void onResume() //app moved to foreground (also occurs at app startup)
    {
    	mGameStart = false;
    	mGameEnd = true;
    	super.onResume();
    } // onResume
    
    @Override
    public void onDestroy() //main thread stopped
    {
    	super.onDestroy();
    	System.runFinalizersOnExit(true); //wait for threads to exit before clearing app
    	android.os.Process.killProcess(android.os.Process.myPid());  //remove app from memory 
    }
    
    //listener for config change. 
    //This is called when user tilts phone enough to trigger landscape view
    //we want our app to stay in portrait view, so bypass event 
    @Override 
    public void onConfigurationChanged(Configuration newConfig)
	{
       super.onConfigurationChanged(newConfig);
	}
    
    
    
    
    
    
    private void runTimer() {
    	
    	//create timer to move ball to new position
        mTmr = new Timer(); 
        mTsk = new TimerTask() {
			public void run() {
				
				if(mGameEnd == false && mGameStart == true)
				{
				    //move ball based on current speed
					mBallPos.x += mBallSpd.x;
					mBallPos.y += mBallSpd.y;
	
					checkForBoundaryViolations();
					
					if(!checkForBarrelHit()) {
						endTheGame("BarrelHit");
						return;
					}
					if(!checkForGateClose()) {
						endTheGame("GateClosed");
						return;
					}
					
					//update ball class instance
					mHorse.mX = mBallPos.x;
					mHorse.mY = mBallPos.y;
					
					double time = System.currentTimeMillis() - mGameStartTime;
					time = time + mTimeOffset;
					int min = (int) (time/(1000*60));
					int sec = (int) (time-(min*60*1000))/1000;
					int millisec = (int) (time-sec*1000-min*60*1000);
					 
					mTimeString = new String(min+":"+sec+":"+millisec);
					
					runOnUiThread(new Runnable() {
			            @Override
			            public void run() {
			            	timer.setText(mTimeString);
			            }
			        });
					if((mBallPos.x-mHorsePath.get(mHorsePath.size()-1).x)>0.3 || (mBallPos.y-mHorsePath.get(mHorsePath.size()-1).y)>0.3 )
						mHorsePath.add(new PointF(mBallPos.x, mBallPos.y));
					
					
					//redraw ball. Must run in background thread to prevent thread lock.
					RedrawHandler.post(new Runnable() {
					    public void run() {	
						   mHorse.invalidate();
						   mBarellViews[0].invalidate();
						   mBarellViews[1].invalidate();
						   mBarellViews[2].invalidate();
					  }});
				}
				}}; // TimerTask

        mTmr.schedule(mTsk,10,10); //start timer
	}

	protected boolean checkForGateClose() {
		
		if(mBallPos.x >= mGateLeft && mBallPos.x <= mGateRight && mBallPos.y >= mScreenBottom && (System.currentTimeMillis() - mGameStartTime) > 5000)
			return false;
		else
			return true;
		
	}

	protected boolean checkForBarrelHit() {
    	for(int i=0;i<3;i++) {
    		if(distance(mBallPos,mBarrelLocations[i]) < 961) {
    			mBarellViews[i].error = true;
    			return false;
    		}
    	}
    	return true;
	}

	protected float distance(PointF p, PointF q) {
    	return ((p.x-q.x)*(p.x-q.x)) + ((p.y-q.y)*(p.y-q.y));
    	
    }

	protected void checkForBoundaryViolations() {
    	//if ball goes off screen, reposition the ball
		if (mBallPos.x > mScreenRight) mBallPos.x=mScreenRight;
		if (mBallPos.y > mScreenBottom) mBallPos.y=mScreenBottom;
		if (mBallPos.x < mScreenLeft) mBallPos.x=mScreenLeft;
		if (mBallPos.y < mScreenTop) mBallPos.y=mScreenTop;
		
		//Make line red and add 5 seconds to time
		
	}

}