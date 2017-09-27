package mudit.com.drivsafe;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.Locale;

import mudit.com.drivsafe.Adapters.MyFragmentPageAdapter;

import static mudit.com.drivsafe.R.id.parent;

public class Main2Activity extends AppCompatActivity {
    ViewPager mviewPager;
    FloatingActionButton fabSearch;
    private BroadcastReceiver smsReceiver;
    TextToSpeech t1;
    private TabLayout allTabs;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        fabSearch= (FloatingActionButton) findViewById(R.id.fab);
        allTabs=(TabLayout)findViewById(R.id.tabs);
        mviewPager=(ViewPager)findViewById(R.id.viewPager);
        mviewPager.setOffscreenPageLimit(2);
        mviewPager.setAdapter(new MyFragmentPageAdapter(getSupportFragmentManager()));
        allTabs.setupWithViewPager(mviewPager);
        allTabs.getTabAt(0).setIcon(R.drawable.ic_library_books_pink_800_24dp);
        allTabs.getTabAt(1).setIcon(R.drawable.ic_equalizer_pink_800_24dp);
        allTabs.getTabAt(2).setIcon(R.drawable.ic_place_pink_800_24dp);
        t1=new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.UK);
                }
            }
        });
        Log.d("My", "textToSpeach: ");
        initializeSMSReceiver();
        registerSMSReceiver();
        fabSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                LayoutInflater li=Main2Activity.this.getLayoutInflater();
                final View itemView=li.inflate(R.layout.activity_custom_dialog,null);
                builder.setView(itemView)
                        .setPositiveButton("Find", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText etPlace=(EditText)itemView.findViewById(R.id.etPlace);
                                Uri gmmIntentUri = Uri.parse("geo:0,0?q="+etPlace.getText().toString());
                                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                                mapIntent.setPackage("com.google.android.apps.maps");
                                startActivity(mapIntent);
                            }
                        })
                        .setNegativeButton("Cancel",null);
                AlertDialog alert = builder.create();
                alert.show();
                Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                nbutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                pbutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
            }
        });
//        allTabs.getTabAt(0).setIcon(R.drawable.camera);
//        allTabs.getTabAt(1).setIcon(R.drawable.newsfeed);
//        allTabs.getTabAt(2).setIcon(R.drawable.chat);
//        allTabs.getTabAt(3).setIcon(R.drawable.myprofile);
    }
    private void initializeSMSReceiver(){
        smsReceiver = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {

                Bundle bundle = intent.getExtras();           //---get the SMS message passed in---
                SmsMessage[] msgs = null;
                String msg_from;
                if (bundle != null) {
                    //---retrieve the SMS message received---
                    try {
                        Object[] pdus = (Object[]) bundle.get("pdus");
                        msgs = new SmsMessage[pdus.length];
                        Log.d("My", "onReceive: "+msgs.length+"  pdus:"+pdus.length);
                        for (int i = 0; i < msgs.length; i++) {
                            msgs[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
                            msg_from = msgs[i].getOriginatingAddress();
                            final String msgBody = msgs[i].getMessageBody();
                            Log.d("My", "onReceive: msg_from : "+msg_from);
                            String text="Message recieved from";
                            Log.d("My", "onReceive: ");
                            String contactName=getContactName(msg_from);
                            Log.d("My", "onReceive: ");
                            String mesg=text+contactName;
                            Log.d("My", "onReceive: ");
                            t1.speak(mesg, TextToSpeech.QUEUE_FLUSH, null);
                            AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
                            builder.setTitle("Yes");
                            builder.setMessage("Do you want to listen the message?");
//                            builder.setIcon(ContextCompat.getDrawable(this, R.drawable.ic_logout_aler));
                            builder.setCancelable(false);
                            builder.setNegativeButton("CANCEL", null);
                            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("My", "onReceive: msg_body : "+msgBody);
                                    t1.speak(msgBody, TextToSpeech.QUEUE_FLUSH, null);

                                }
                            });

                            AlertDialog alert = builder.create();
                            alert.show();
                            Button nbutton = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
                            nbutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));
                            Button pbutton = alert.getButton(DialogInterface.BUTTON_POSITIVE);
                            pbutton.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary));

                        }
                    } catch (Exception e) {
//                            Log.d("Exception caught",e.getMessage());
                    }
                }

            }
        };
    }



    private void registerSMSReceiver() {
        IntentFilter intentFilter = new IntentFilter("android.provider.Telephony.SMS_RECEIVED");
        registerReceiver(smsReceiver, intentFilter);
    }
    private String getContactName(String phone){
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phone));
        String projection[] = new String[]{ContactsContract.Data.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor.moveToFirst()){
            return cursor.getString(0);
        }else {
            return "unknown number";
        }
    }
}