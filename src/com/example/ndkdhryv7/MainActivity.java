package com.example.ndkdhryv7;

import android.os.Bundle;
import android.os.Handler;
import android.app.Activity;
import android.content.Context;
import android.view.KeyEvent;
//import android.view.Menu;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private DhryThread dThread = null;
	private Handler handle;
	private EditText rField;
	private final String SAVELABEL_RUNLOGS="log";
	private final String SAVELABEL_NRUNS="nrun";
	private final String SAVELABEL_SCROLLPOS="scrpos";
	private TextView lField;
	private ScrollView scrV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        handle=new Handler();
        setContentView(R.layout.activity_main);
		lField=(TextView) findViewById(R.id.logField);
		scrV=(ScrollView) findViewById(R.id.scrollView);
		rField=(EditText) findViewById(R.id.numRun);
		rField.setOnEditorActionListener(new EditText.OnEditorActionListener() {
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				RunButtonClicked(v);
				return true;
			}
		});
		if(savedInstanceState!=null) {
			lField.setText(savedInstanceState.getCharSequence(SAVELABEL_RUNLOGS));
			rField.setText(savedInstanceState.getCharSequence(SAVELABEL_NRUNS));
			final int scrollY= savedInstanceState.getInt(SAVELABEL_SCROLLPOS,0);
			scrV.post(new Runnable() {
				@Override
				public void run() {
					scrV.scrollTo(0,scrollY);
				}
			});
		}
    }

	@Override
	public void onSaveInstanceState(Bundle savedInstanceState) {
		savedInstanceState.putCharSequence(SAVELABEL_RUNLOGS, lField.getText());
		savedInstanceState.putCharSequence(SAVELABEL_NRUNS, rField.getText());
		savedInstanceState.putInt(SAVELABEL_SCROLLPOS, scrV.getScrollY());
	}
    public synchronized void RunButtonClicked(View v) {
		hideKeyboard();
    	setRunButtonState(dThread==null);
    	if(dThread==null) {
    		int nLoops;
    		if((nLoops=getNumLoops())<1) {
    			Toast.makeText(getApplicationContext(), R.string.invalid_input, Toast.LENGTH_SHORT).show();
    	    	setRunButtonState(false);
    	    	return;
    		}
    		dThread=new DhryThread(this,handle,nLoops);
    		dThread.start();
    	} else {
    		// No, we cannot interrupt JNI native function. Sorry.
    		dThread.interrupt();
    		//dThread=null;
    	}
    }
    public synchronized void DhryThreadFinished(boolean success, String resultText) {
    	if(success) {
    		lField.append(resultText);
    		scrV.post(new Runnable() {
    			public void run() {
    	    		scrV.smoothScrollTo(0, lField.getHeight());
    			}
    		});
    	}
    	dThread=null;
    	setRunButtonState(false);
    }
    private int getNumLoops() {
    	int nLoops;
    	try { 
    		nLoops=Integer.valueOf(rField.getText().toString()).intValue();
    	} catch (NumberFormatException e) {
    		nLoops=-1;
    	}
    	return nLoops;
    }
    private void setRunButtonState(boolean isAbort) {
    	Button rButton=(Button) findViewById(R.id.button1);
    	EditText rField=(EditText) findViewById(R.id.numRun);
    	int newText=isAbort?R.string.abort_dhry:R.string.run_dhrystone;
    	rButton.setText(newText);
    	// JNI was not interruptible. Sorry.
    	rButton.setEnabled(!isAbort);
    	rField.setEnabled(!isAbort);
    }
	private void hideKeyboard() {
		InputMethodManager imm=(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if(imm!=null) {
			imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
}
