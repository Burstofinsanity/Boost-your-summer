package com.bornintelligence.cellc;

import java.util.ArrayList;

public class Location {
    private String name;
    private ArrayList<EventDate> Dates;
    private String id;

    public Location(String id,String name, ArrayList<EventDate> Dates){
        this.name = name;
        this.Dates = Dates;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public ArrayList<EventDate> getDates() {return Dates;}

    public String getId() {return id;}

    public void setId(String id) {
        this.id = id;
    }

    public void setDates(ArrayList<EventDate> dates) {
        Dates = dates;
    }

    public void setSpecificDates(int Index, EventDate event) {
        Dates.set(Index,event);
    }
}
