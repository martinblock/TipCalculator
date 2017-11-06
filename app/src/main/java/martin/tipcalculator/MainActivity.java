package martin.tipcalculator;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.view.View.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;
import java.text.NumberFormat;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class MainActivity extends AppCompatActivity implements OnEditorActionListener, OnKeyListener, OnSeekBarChangeListener {


//hi


    private static final String TAG = "MainActivity";
    // define memeber variables for the widgets
    private TextView percentTV;
    private TextView tipPercentTV;
    private TextView totalTV;
    private EditText billET;
    private Button resetButton;
    private RadioButton noneRadio;
    private RadioButton tipRadio;
    private RadioButton totalRaido;
    private Spinner splitSpinner;
    private TextView perPersonLabel;
    private TextView perPersonTV;
    private RadioGroup radioGroup;
    private SeekBar seekBar;
    private Button applyButton;


    //define Rounding Constants
    private final int ROUND_NONE = 0;
    private final int ROUND_TIP = 1;
    private final int ROUND_TOTAL = 2;

    // define instance variables
    private String billAmountString = "";
    private float tipPercent = .15f;
    private SharedPreferences savedValues;
    private int rounding = ROUND_NONE;
    private int split = 1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //get references to the widget
        percentTV = (TextView) findViewById(R.id.percentTV);
        tipPercentTV = (TextView) findViewById(R.id.tipPercentTV);
        totalTV = (TextView) findViewById(R.id.totalTV);
        billET = (EditText) findViewById(R.id.billET);
        resetButton = (Button) findViewById(R.id.resetButton);
        noneRadio = (RadioButton) findViewById(R.id.NoneRadioButton);
        tipRadio = (RadioButton) findViewById(R.id.tipRadioButton);
        totalRaido = (RadioButton) findViewById(R.id.totalRaidoButton);
        splitSpinner = (Spinner) findViewById(R.id.spinner);
        perPersonLabel = (TextView) findViewById(R.id.perPersonLabel);
        perPersonTV = (TextView) findViewById(R.id.perPersonTV);
        radioGroup = (RadioGroup) findViewById(R.id.roundingRadioGroup);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        applyButton = (Button) findViewById(R.id.applyButton);

        //set the seek bar to OnProgresschange
        seekBar.setOnSeekBarChangeListener(this);

        //set array adapter for spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this, R.array.split_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        splitSpinner.setAdapter(adapter);
        //set listeners

        //current class
        billET.setOnEditorActionListener(this);
        billET.setOnKeyListener(this);

        //Named Class As Listener
        ClickListener clickListener = new ClickListener();

        resetButton.setOnClickListener(clickListener);
        applyButton.setOnClickListener(clickListener);

        //Anonymous Class as Listener
        radioGroup.setOnCheckedChangeListener(checkedChangeListener);

        //Anonymous inner class as the listener
        splitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                split = position + 1;
                calculateAndDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        billET.setOnKeyListener(this);
        radioGroup.setOnKeyListener(this);

        //get the shared preferences
        savedValues = getSharedPreferences("SavedValues", MODE_PRIVATE);




    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        switch (keyCode){

            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_DPAD_CENTER:
                calculateAndDisplay();
                imm.hideSoftInputFromInputMethod(billET.getWindowToken(),0);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                calculateAndDisplay();
                imm.hideSoftInputFromInputMethod(billET.getWindowToken(), 0);
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                if(v.getId() == R.id.percentTV) {
                    calculateAndDisplay();
                }
        }


        return false;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        percentTV.setText(progress + "%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        int progress = seekBar.getProgress();
        tipPercent = (float) progress / 100;
    }

    class ClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {

            switch(v.getId()){
                case R.id.resetButton:
                    tipPercent = .15f;
                    billET.setText("");
                    tipPercentTV.setText("");
                    totalTV.setText("");
                    calculateAndDisplay();
                    break;
                case R.id.applyButton:
                    calculateAndDisplay();

            }

        }
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_UNSPECIFIED) {

            calculateAndDisplay();
        }

        Toast.makeText(getApplicationContext(), "Action ID: " + actionId, Toast.LENGTH_LONG).show();
        return false;
    }

    private OnCheckedChangeListener checkedChangeListener = new OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

            switch (checkedId){
                case R.id.NoneRadioButton:
                    rounding = ROUND_NONE;
                    break;
                case R.id.tipRadioButton:
                    rounding = ROUND_TIP;
                    break;
                case R.id.totalRaidoButton:
                    rounding = ROUND_TOTAL;
                    break;

            }
            calculateAndDisplay();
        }
    };




    private void calculateAndDisplay() {

        // get bill amount
        billAmountString = billET.getText().toString();
        float billAmount;


        if(billAmountString.equals("")){
            billAmount = 0;
        }
        else{
            billAmount = Float.parseFloat(billAmountString);
        }

        Log.d(TAG, "BillAmount: " + billAmount);
        // calculate tip
        float tipAmount = 0;
        float totalAmount = 0;
        float percentAmount = tipPercent;
        if(rounding == ROUND_NONE){
            tipAmount = billAmount * tipPercent;
            totalAmount = billAmount + tipAmount;
        }else if(rounding == ROUND_TIP){
            tipAmount = StrictMath.round(billAmount * tipPercent);
            totalAmount = billAmount + tipAmount;
        }else if(rounding == ROUND_TOTAL) {
            float tipNotRounded = billAmount * tipPercent;
            totalAmount = StrictMath.round(billAmount + tipNotRounded);
            tipAmount = totalAmount - billAmount;

        }

        //calculate split amount and show it
        float splitAmount = 0;
        if(split == 1){
            perPersonLabel.setVisibility(View.GONE);
            perPersonTV.setVisibility(View.GONE);
        }else{
            splitAmount = tipAmount / split;
            perPersonLabel.setVisibility(View.VISIBLE);
            perPersonTV.setVisibility(View.VISIBLE);
        }


        // display the formatted results
        NumberFormat percent = NumberFormat.getPercentInstance();
        percentTV.setText(percent.format(percentAmount));

        NumberFormat currency = NumberFormat.getCurrencyInstance();
        tipPercentTV.setText(currency.format(tipAmount));
        totalTV.setText(currency.format(totalAmount));
        perPersonTV.setText(currency.format(splitAmount));
    }

    @Override
    protected void onPause() {
        //save the instance variables
        Editor editor = savedValues.edit();
        editor.putString("billAmountString", billAmountString);
        editor.putFloat("tipPercent", tipPercent);


        editor.putInt("rounding", rounding);
        editor.putInt("split", split);

        editor.apply();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //get the instance variables
        billAmountString = savedValues.getString("billAmountString", "");
        tipPercent = savedValues.getFloat("tipPercent", .15f);
        rounding = savedValues.getInt("rounding", ROUND_NONE);
        split = savedValues.getInt("split", 1);

        //set the bill amounton its widget
        billET.setText(billAmountString);
        if(rounding == ROUND_NONE){
            noneRadio.setChecked(true);
        }else if(rounding == ROUND_TIP){
            tipRadio.setChecked(true);
        }else if(rounding == ROUND_TOTAL) {
            totalRaido.setChecked(true);
        }

        //set the split on spinner
        int position = split - 1;
        splitSpinner.setSelection(position);
    }


}
