package com.bornintelligence.cellc;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.content.ContentValues.TAG;

public class MainActivity extends AppCompatActivity {

    private ArrayList<Location> mLocations;
    private LocationAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        hideSystemUI();
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mLocations = new ArrayList<Location>();
        holderList();

        Spinner locations = findViewById(R.id.locations);

        mAdapter = new LocationAdapter(this, mLocations);
        locations.setAdapter(mAdapter);
        locations.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getBaseContext(), game.class);
                Location clickedItem = (Location) parent.getItemAtPosition(position);

                if(!clickedItem.getName().equals("SELECT A REGION") && !clickedItem.getName().equals("Loading...")) {
                    Gson gson = new Gson();
                    String json = gson.toJson(clickedItem);
                    intent.putExtra("selected", json);
                    startActivity(intent);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        initFirestore();
    }


    private void hideSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }


    private void initFirestore(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseFirestoreSettings settings = new FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build();
        db.setFirestoreSettings(settings);
        db.enableNetwork();
        db.collection("locations")
                .get()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("BI Fail", e.toString());
                    }
                })
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            mLocations.clear();
                            ArrayList<EventDate> dt = new ArrayList<>();
                            mLocations.add(new Location("","SELECT A REGION",dt));
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d(TAG, document.getId() + " => " + document.getData());
                                if (document.exists()) {
                                    String doc_id = document.getId();
                                    String name = document.getData().get("name").toString();
                                    Object dates = document.getData().get("dates");
                                    ArrayList DateList = new ArrayList();;
                                    if(dates instanceof ArrayList){
                                        DateList = (ArrayList) dates;
                                    }
                                    else
                                    if(dates instanceof HashMap){
                                        Object values = ((HashMap) dates).values();
                                        Iterator iterator = ((Collection) values).iterator();
                                        HashMap items = new HashMap();
                                        while(iterator.hasNext()){
                                            HashMap item =  (HashMap) iterator.next();
                                            DateList.add(item);
                                        }
                                    }

                                    ArrayList<EventDate> dts = (dates != null)?datesFromHashmap(DateList):new ArrayList<EventDate>();
                                    try {
                                        if(containToday(dts)) {;
                                            mLocations.add(new Location(doc_id,name, dts));
                                        }
                                    } catch (ParseException e) {
                                        e.printStackTrace();
                                    }
                                } else {
                                    Log.d(TAG, "N   o such document");
                                }
                            }
                        } else {

                        }
                        mAdapter.notifyDataSetChanged();
                    }
                });
    }


    private ArrayList<EventDate> datesFromHashmap(ArrayList<HashMap<String, String>> map){
        ArrayList<EventDate> output = new ArrayList<EventDate>();

        Iterator<HashMap<String, String>> its = map.iterator();
        while (its.hasNext()) {
            HashMap<String, String> item = its.next();
            String glasses = item.get("glasses");
            String cardholders = item.get("cardholders");
            String rings = item.get("rings");
            String caps = item.get("caps");
            EventDate ev = new EventDate(item.get("date"),Integer.parseInt(cardholders),Integer.parseInt(rings),Integer.parseInt(glasses),Integer.parseInt(caps));
            if(item.containsKey("caps_used"))
                ev.setUsedCaps(Integer.parseInt(item.get("caps_used")));
            if(item.containsKey("cardholders_used"))
                ev.setUsedHolders(Integer.parseInt(item.get("cardholders_used")));
            if(item.containsKey("glasses_used"))
                ev. setUsedGlasses(Integer.parseInt(item.get("glasses_used")));
            if(item.containsKey("rings_used"))
                ev.setUsedBottle(Integer.parseInt(item.get("rings_used")));
            output.add(ev);
        }

        return output;
    }

    private void holderList(){
        ArrayList<EventDate> dates = new ArrayList<>();
        mLocations.add(new Location("","Loading...",dates));
    }

    private Boolean containToday(ArrayList<EventDate> map) throws ParseException {
        Boolean hasToday = false;
        Iterator<EventDate> its = map.iterator();

        SimpleDateFormat df = new SimpleDateFormat("dd/MM/yyyy");
        Date today = df.parse(df.format(new Date()));

        while (its.hasNext()) {
            EventDate item = its.next();
            String dateString = item.getDate();
            Date date = df.parse(dateString);
            if(today.compareTo(date) == 0 && item.getTotalLeft() >0) {
                hasToday = true;

            }
        }

        return hasToday;
    }

    public void startSpin(){

    }
}


