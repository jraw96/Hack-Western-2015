package com.example.azorac.hackwestern4;

//Libraries for Pebble Comm
import java.util.UUID;

//Libraries for Speech to Text
import java.util.ArrayList;
import android.speech.RecognizerIntent;

//UI Libraries
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;
import android.view.Menu;

//Misc.
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;//Gets pebble dev kit
import com.getpebble.android.kit.util.PebbleDictionary;//Gets pebble dict dev kit



public class MainActivity extends Activity {

    protected static final int RESULT_SPEECH = 1;//not sure what this does

    private ImageButton btnSpeak;//button to initiate speech to text
    private TextView txtText;//text field to output converted speech

    private static final int KEY_BUTTON_SELECT = 0; //SELECT RETURNS 0
    private static final int KEY_BUTTON_UP = 1;//UP RETURNS 1

    //to store UUID (corresponds with CloudPebble UUID
    private static final UUID APP_UUID = UUID.fromString("7038bd5f-eb7c-4817-aece-bf6de2a6596c");

    //Used for data sending
    private static final int KEY_DATA = 5;

    //Initialized pebble data receiver
    private PebbleKit.PebbleDataReceiver mDataReceiver;

    //THIS IS WHAT WE NEED TO TRANSFER TO THE PEBBLE
    //=======================================================================
    //=======================================================================
    private static String result = "";//String to store the converted speech
    //=======================================================================
    //=======================================================================

    @Override
    protected void onResume()//processes for when connected to the watch (i think)
    {
        super.onResume();

       if(mDataReceiver == null){//if the data receiver even works this is true=1
           mDataReceiver = new PebbleKit.PebbleDataReceiver(APP_UUID){//initializes the receiver
               @Override

           public void receiveData(Context context, int transactionId, PebbleDictionary dict){
                   //ALWAYS ACK
                   PebbleKit.sendAckToPebble(context, transactionId);//basically tests the connection
                   Log.i("receiveData", "Got message from Pebble!");//pebble acknowledges it

                   //Select button was pressed
                   if(dict.getInteger(KEY_BUTTON_SELECT)!=null){
                       //do something
                       beginSpeech();
                   }
                   //Up button was pressed
                   if (dict.getInteger(KEY_BUTTON_UP) !=null){
                       //do something else
                       //eventually we'll make this close the speech field
                   }
               }
           };
           PebbleKit.registerReceivedDataHandler(getApplicationContext(), mDataReceiver);//not sure what this means
       }

        //construct output string
        StringBuilder builder = new StringBuilder();//the builder is basically cout for the android app
        //builder.append("Pebble Info:\n\n");

        //Is the watch connected?
        boolean isConnected = PebbleKit.isWatchConnected(this);//if the watch is even connected, this passes
        builder.append("Watch is connected: "+ (isConnected ? "true":"false")).append("\n");//condensed if/ifelse stment

        //What is the firmware version?
        //I dont think this shows up in the UI and needs to be fixed at the end (might be something in the xml files)
        PebbleKit.FirmwareVersionInfo info = PebbleKit.getWatchFWVersion(this);
        builder.append("Pebble Version: "+info.getMajor()).append(".");
        builder.append(info.getMinor()).append("\n");

        //Is AppMessage Supported?
        boolean appMessageSupported = PebbleKit.areAppMessagesSupported(this);
        builder.append("AppMessage supported: " + (appMessageSupported ? "true" : "false"));//condensed if/ifelse stment
    }

    //@Override
    public void beginSpeech() {


        Intent intent = new Intent(//Intents are what actually does shit
                RecognizerIntent.ACTION_RECOGNIZE_SPEECH);//THIS RECOGNIZER MAKES THE MIC DIALOGUE TO POP UP

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");//Engrish

        try {
            startActivityForResult(intent, RESULT_SPEECH);//Tries to access Speech to text services
            txtText.setText("");//ensures txtText is initialized
        } catch (ActivityNotFoundException a) {
            Toast t = Toast.makeText(getApplicationContext(),
                    "Opps! Your device doesn't support Speech to Text",//we won't run into this issue
                    Toast.LENGTH_SHORT);
            t.show();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        Log.d("MainActivity", "Called speech to text");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//sets the layout file? probably?

        txtText = (TextView) findViewById(R.id.txtText);//text field

        btnSpeak = (ImageButton) findViewById(R.id.btnSpeak);//button

        btnSpeak.setOnClickListener(new View.OnClickListener() {//waits for you to click it

            @Override
            public void onClick(View v) {

                //Intent intent = new Intent(
                  //      RecognizerIntent.ACTION_RECOGNIZE_SPEECH);//makes the dialogue pop up as a new intent
                //same code as in the other function recordSpeech
                //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");

                /*try {
                    startActivityForResult(intent, RESULT_SPEECH);
                    txtText.setText("");
                } catch (ActivityNotFoundException a) {
                    Toast t = Toast.makeText(getApplicationContext(),
                            "Opps! Your device doesn't support Speech to Text",
                            Toast.LENGTH_SHORT);
                    t.show();
                }*/
            }
        });
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        switch(requestCode){
            case RESULT_SPEECH:{
                if(resultCode == RESULT_OK && null != data){
                    PebbleDictionary textDict = new PebbleDictionary();
                    ArrayList<String>text = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    txtText.setText(text.get(0));
                    result = text.get(0);//STORES THE DATA IN A STRING
                    Log.d("MainActivity", result);

                    textDict.addString(1234, result);
                    PebbleKit.sendDataToPebble(getApplicationContext(), APP_UUID, textDict);
                }
                break;
            }
            case RESULT_CANCELED:{
                Log.d("MainActivity", "Child activity has crashed");
                break;
            }
            default:
            {
                Log.d("MainActivity", "Error, exiting child activity");
                break;
            }
        }
    }
}
