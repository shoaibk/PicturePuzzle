package com.shoaib.dragdrop;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ClipData;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnDragListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class DragDropActivity extends Activity{
	
	private final String TAG = "DRAG_DROP";
	private final long COUNT_DOWN_TIME_MILLIS = 120000;
	private final long MILLIS_IN_SECOND = 1000;
	private final int NUM_ROWS = 4;
	private final int NUM_COLS = 3;
	private final int NUM_IMAGES = 33;
	private final int NUM_TILES = NUM_ROWS * NUM_COLS;
	private final int IMAGE_WIDTH = 768;
	private final int IMAGE_HEIGHT = 1280;
	private static final int DIALOG_ALERT = 1;
	
	private TextView mTextField;
	private CountDownTimer mCountDownTimer;
	
	private boolean mIsTimeOut = false;
	private long mTimeTaken;
	
	private boolean mGameDone = false;
	
	//original bitmap
	private Bitmap mImageBitmap;
	
	//scaled bitmap
	private Bitmap mScaledBitmap;
	
	//pieces of the scaled bitmap
	private Bitmap[] mTileBitmaps;
	
	// some pointers to keep track of the bitmap business
	private int[] mTileOriginalRef = new int[NUM_TILES];
	private int[] mTileCurrentRef = new int[NUM_TILES];
	private int[] mContainers = new int[NUM_TILES];
	private int[] mImage = new int[NUM_IMAGES];

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    
    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        WindowManager.LayoutParams.FLAG_FULLSCREEN); 
    setContentView(R.layout.activity_drag_drop);     
    startGame();
    
  } 
  
  @Override
  public void onDestroy() {
	super.onDestroy();
	freeBitmaps();
  }
  
  
  /*
   * The main game loop
   */
  public void startGame() {
  	initParams();
  	attachListeners();
  	loadBitmap();
    drawTiles();    
    shuffleTiles();
    startTimer();
    showStartToast();
  }
  
  /*
   * Attaches the touch and drag listeners to the layout elements
   */
  
  private void attachListeners() {
	  for (int i = 0; i < NUM_TILES; i++) {
	    	findViewById(mTileOriginalRef[i]).setOnTouchListener(new MyTouchListener());
	    	findViewById(mContainers[i]).setOnDragListener(new MyDragListener());
	    	mTileCurrentRef[i] = mTileOriginalRef[i];
	    }
  }
  
  /*
   * Loads a random drawable and scales it to the size IMAGE_WIDTH x IMAGE_HEIGHT
   */
  private void loadBitmap() {
	  
	  int drawablePointer = (int)Math.floor(Math.random() * NUM_IMAGES);
	  mTileBitmaps = new Bitmap[NUM_COLS * NUM_ROWS];
	  mImageBitmap = BitmapReducer.reduce(getResources(), mImage[drawablePointer], IMAGE_WIDTH, IMAGE_HEIGHT);
	  mScaledBitmap = Bitmap.createScaledBitmap(mImageBitmap, IMAGE_WIDTH, IMAGE_HEIGHT, false);  
	  Log.d(TAG, "Image height: " + IMAGE_WIDTH + ", Image Width: " + IMAGE_HEIGHT + "\n");
  }
  
  /*
   * Draws parts of the original bitmap into the tiles. 
   * The whole image is divided into NUM_ROWS x NUM_COLS pieces.
   * 
   */
  private void drawTiles() {
	        
	    for (int i = 0; i < NUM_COLS; i++) {
	    	for (int j = 0; j < NUM_ROWS; j++) {
	    		
	        	ImageView tileImageView = (ImageView)findViewById(mTileOriginalRef[i + j*NUM_COLS]);
	        	
	        	Log.d(TAG, "i: " + i + ", j: " + j + "\n");
	        	mTileBitmaps[i + j*NUM_COLS] = Bitmap.createBitmap(mScaledBitmap, IMAGE_WIDTH*i/NUM_COLS, IMAGE_HEIGHT*j/NUM_ROWS, IMAGE_WIDTH/NUM_COLS, IMAGE_HEIGHT/NUM_ROWS);
	        	
	        	tileImageView.setImageBitmap(mTileBitmaps[i + j*NUM_COLS]);
	        	
	        	Log.d(TAG, "Tile height: " + mTileBitmaps[i + j*NUM_COLS].getHeight() + ", Tile width: " + mTileBitmaps[i + j*NUM_COLS].getWidth() + "\n");
	        	       	
	    	}
	    }
  }
  
  /*
   * A countdown timer is started to keep track of how long has elapsed since the start of the game.
   */
  private void startTimer() {
	  mCountDownTimer = new CountDownTimer(COUNT_DOWN_TIME_MILLIS, MILLIS_IN_SECOND) {

	        public void onTick(long millisUntilFinished) {
	            mTextField.setText(" " + millisUntilFinished / MILLIS_IN_SECOND + "s");
	            mTimeTaken = (COUNT_DOWN_TIME_MILLIS - millisUntilFinished)/MILLIS_IN_SECOND;
	        }

	        public void onFinish() {
	            mTextField.setText(" Keep trying! You can do it!");
	            mTextField.setBackgroundColor(0xDDAA1100);
	            mTimeTaken = COUNT_DOWN_TIME_MILLIS/MILLIS_IN_SECOND;
	            mIsTimeOut = true;
	        }
	  }.start();
  }
  
  /*
   * Randomly shuffle the tiles of the image. 
   */
  public void shuffleTiles() {
		int counter = mContainers.length, temp, index;
		LinearLayout ll1, ll2;
		ImageView iv1, iv2;
		
		temp = 0;
		index = 0;

		// While there are elements in the array
		while (counter > 0) {
		      // Pick a random index
		      index = (int)Math.floor(Math.random() * counter);
		      counter--;
		      if (index == counter) continue;
		      
		      ll1 = (LinearLayout)findViewById(mContainers[counter]);
		      ll2 = (LinearLayout)findViewById(mContainers[index]);
		      
		      iv1 = (ImageView)findViewById(mTileCurrentRef[counter]);
		      iv2 = (ImageView)findViewById(mTileCurrentRef[index]);
		      
		      ll1.removeView(iv1);
		      ll2.removeView(iv2);	      
		      ll1.addView(iv2);		      
		      ll2.addView(iv1);
	            
		      // update the pointers
		      temp = mTileCurrentRef[counter];
		      mTileCurrentRef[counter] = mTileCurrentRef[index];
		      mTileCurrentRef[index] = temp;		      
		}
	}
  
  /*
   * Touchlistener to be attached to the imageviews which contain the bitmap pieces.
   */
  private final class MyTouchListener implements OnTouchListener {
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        ClipData data = ClipData.newPlainText("", "");
        DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
        view.startDrag(data, shadowBuilder, view, 0);
        view.setAlpha((float) 0.5);
        return true;
      } else {
        return false;
      }
    }
  }

  /*
   * Draglistener to be attached to the linearlayout containers
   * When the drags are completed, the imageviews inside the linearlayouts are swapped.
   */
  private final class MyDragListener implements OnDragListener {
    Drawable normalShape = getResources().getDrawable(R.drawable.shape);

	@Override
    public boolean onDrag(View v, DragEvent event) {
      
      View visitorView = (View) event.getLocalState();
      ViewGroup visitorOwner = (ViewGroup) visitorView.getParent();
      ViewGroup visitedOwner = (ViewGroup) v;
      View visitedImage = (View)visitedOwner.getChildAt(0);  
      
      switch (event.getAction()) {
      case DragEvent.ACTION_DRAG_STARTED:

        break;
      case DragEvent.ACTION_DRAG_ENTERED:
    	v.setAlpha((float)0.7);
        v.setBackground(normalShape);
        break;
      case DragEvent.ACTION_DRAG_EXITED:
    	v.setAlpha((float)1.0);
        v.setBackground(normalShape);
        break;
      case DragEvent.ACTION_DROP:        
        visitorView.setAlpha((float)1.0);
        visitedImage.setAlpha((float)1.0);
        
        if (visitorOwner != visitedOwner) {
        	/*
        	 * swap the imageviews from the containers
        	 */
        	
            visitedOwner.removeView(visitedImage);   
            visitorOwner.removeView(visitorView);            
            visitorOwner.addView(visitedImage);
            LinearLayout container = (LinearLayout) v;            
            container.addView(visitorView);
            
            int index1 = findIndex(mTileCurrentRef, visitedImage.getId());
            int index2 = findIndex(mTileCurrentRef, visitorView.getId());
            
            // update the references
		    int temp = mTileCurrentRef[index1];
		    mTileCurrentRef[index1] = mTileCurrentRef[index2];
		    mTileCurrentRef[index2] = temp;
		      
        } else {
           
        }
        checkIfDone();
        break;
      case DragEvent.ACTION_DRAG_ENDED:
        v.setBackgroundDrawable(normalShape);
        v.setAlpha((float)1.0);
        visitedImage.setAlpha((float)1.0);
      default:
        break;
      }
      return true;
    }
  }
  
  /*
   * Find the first index matching the integer in an integer array
   */
  private int findIndex(int[] numArray, int num) {
	  for (int i = 0; i < numArray.length; i++) {
		  if (numArray[i] == num) return i;
	  }
	  return -1;
  }
  
  /*
   * Checks if the the player is done putting all the bitmap pieces to their original positions
   */
  private void checkIfDone() {
	  for (int i = 0; i < NUM_TILES; i++) {
		  if (mTileOriginalRef[i] != mTileCurrentRef[i]) return;
	  }
	  if (!mGameDone) {
		  mGameDone = true;
		  mCountDownTimer.cancel();
		  mTextField.setBackgroundColor(0xDD1F7F42);
		  mTextField.setText(" Done!");
		  showDialog(DIALOG_ALERT);
	  }
  }
  
  /*
   * Dialog to be shown after a game has been finished. Ideally, this should be extended from a DialogFragment.
   * 
   */
  
  @Override
  protected Dialog onCreateDialog(int id) {
    switch (id) {
      case DIALOG_ALERT:
        Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Awesome! Play again?");
        builder.setCancelable(true);
        builder.setPositiveButton("Yes", new OkOnClickListener());
        builder.setNegativeButton("No", new CancelOnClickListener());
        AlertDialog dialog = builder.create();
        dialog.show();
    }
    return super.onCreateDialog(id);
  }

  private final class CancelOnClickListener implements
      DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int which) {
      DragDropActivity.this.finish();
    }
  }

  private final class OkOnClickListener implements
      DialogInterface.OnClickListener {
    public void onClick(DialogInterface dialog, int which) {
      startGame();
      
    }
  }

  
  /*
   * clean up bitmap resources
   */
  private void freeBitmaps() {
	  mImageBitmap.recycle();
	  mScaledBitmap.recycle();
	  for (int i = 0; i < NUM_COLS * NUM_ROWS; i++) {
	    mTileBitmaps[i].recycle();
	  }
  }
  
  /*
   * Shows a toast at the beginning of the game play, to indicate the game has started.
   */
  private void showStartToast() {
	LayoutInflater inflater = getLayoutInflater();
    View layout = inflater.inflate(R.layout.toast_layout,
                                   (ViewGroup) findViewById(R.id.toast_layout_root));

    TextView toastText = (TextView) layout.findViewById(R.id.toastText);
    toastText.setText("Can you put it back together?");

    Toast toast = new Toast(getApplicationContext());
    toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
    toast.setDuration(Toast.LENGTH_LONG);
    toast.setView(layout);
    toast.show();
  }
  
  private void initParams() {
	  	mIsTimeOut = false;
	  	mTimeTaken = 0;
	  	mGameDone = false;

	  	mTileOriginalRef[0] = R.id.myimage1;
	    mContainers[0] = R.id.ll1;
	    
	    mTileOriginalRef[1] = R.id.myimage2;
	    mContainers[1] = R.id.ll2;
	    
	    mTileOriginalRef[2] = R.id.myimage3;
	    mContainers[2] = R.id.ll3;
	    
	    mTileOriginalRef[3] = R.id.myimage4;
	    mContainers[3] = R.id.ll4;
	    
	    mTileOriginalRef[4] = R.id.myimage5;
	    mContainers[4] = R.id.ll5;
	    
	    mTileOriginalRef[5] = R.id.myimage6;
	    mContainers[5] = R.id.ll6;
	    
	    mTileOriginalRef[6] = R.id.myimage7;
	    mContainers[6] = R.id.ll7;
	    
	    mTileOriginalRef[7] = R.id.myimage8;
	    mContainers[7] = R.id.ll8;
	    
	    mTileOriginalRef[8] = R.id.myimage9;
	    mContainers[8] = R.id.ll9;
	    
	    mTileOriginalRef[9] = R.id.myimage10;
	    mContainers[9] = R.id.ll10;
	    
	    mTileOriginalRef[10] = R.id.myimage11;
	    mContainers[10] = R.id.ll11;
	    
	    mTileOriginalRef[11] = R.id.myimage12;
	    mContainers[11] = R.id.ll12;
	    	    
	    mTextField = (TextView)findViewById(R.id.textview);
	    mTextField.setBackgroundColor(0xDD1F7F42); //light green with some alpha
	    
	    mImage[0] = R.drawable.pic0;
	    mImage[1] = R.drawable.pic1;
	    mImage[2] = R.drawable.pic2;
	    mImage[3] = R.drawable.pic3;
	    mImage[4] = R.drawable.pic4;
	    mImage[5] = R.drawable.pic5;
	    mImage[6] = R.drawable.pic6;
	    mImage[7] = R.drawable.pic7;
	    mImage[8] = R.drawable.pic8;
	    mImage[9] = R.drawable.pic9;
	    
	    mImage[10] = R.drawable.pic10;
	    mImage[11] = R.drawable.pic11;
	    mImage[12] = R.drawable.pic12;
	    mImage[13] = R.drawable.pic13;
	    mImage[14] = R.drawable.pic14;
	    mImage[15] = R.drawable.pic15;
	    mImage[16] = R.drawable.pic16;
	    mImage[17] = R.drawable.pic17;
	    mImage[18] = R.drawable.pic18;
	    mImage[19] = R.drawable.pic19;
	    
	    mImage[20] = R.drawable.pic20;
	    mImage[21] = R.drawable.pic21;
	    mImage[22] = R.drawable.pic22;
	    mImage[23] = R.drawable.pic23;
	    mImage[24] = R.drawable.pic24;
	    mImage[25] = R.drawable.pic25;
	    mImage[26] = R.drawable.pic26;
	    mImage[27] = R.drawable.pic27;
	    mImage[28] = R.drawable.pic28;
	    mImage[29] = R.drawable.pic29;
	    
	    mImage[30] = R.drawable.pic30;
	    mImage[31] = R.drawable.pic31;
	    mImage[32] = R.drawable.pic32;
  }
} 

	