package com.bornintelligence.cellc;

public class EventDate {
    String Date;
    Integer Caps;
    Integer Holders;
    Integer Bottle;
    Integer Glasses;
    Integer UsedCaps = 0;
    Integer UsedHolders = 0;
    Integer UsedBottle = 0;
    Integer UsedGlasses = 0;

    public EventDate(String Date, Integer Holders, Integer Bottle, Integer Glasses, Integer Caps){
        this.Date = Date;
        this.Bottle = Bottle;
        this.Caps = Caps;
        this.Holders = Holders;
        this.Glasses = Glasses;
    }

    public String getDate(){
        return this.Date;
    }

    public Integer getBottle(){
        return this.Bottle;
    }
    public Integer getCaps(){
        return this.Caps;
    }
    public Integer getHolders(){
        return this.Holders;
    }
    public Integer getGlasses(){
        return this.Glasses;
    }

    public Integer getUsedBottle(){
        return this.UsedBottle;
    }
    public Integer getUsedCaps(){
        return this.UsedCaps;
    }
    public Integer getUsedHolders(){
        return this.UsedHolders;
    }
    public Integer getUsedGlasses(){
        return this.UsedGlasses;
    }

    public Integer getBottleLeft(){
        return (this.Bottle - this.UsedBottle);
    }
    public Integer getCapsLeft(){
        return (this.Caps - this.UsedCaps);
    }
    public Integer getHoldersLeft(){
        return (this.Holders - this.UsedHolders);
    }
    public Integer getGlassesLeft(){
        return (this.Glasses - this.UsedGlasses);
    }

    public void setUsedBottle(Integer used){
        this.UsedBottle = used;
    }

    public void increaseUsedBottle(){
        this.UsedBottle  ++;
    }

    public void setUsedCaps(Integer used){
        this.UsedCaps = used;
    }

    public void increaseUsedCaps(){
        this.UsedCaps ++;
    }

    public void setUsedHolders(Integer used){
        this.UsedHolders = used;
    }

    public void increaseUsedHolders(){
        this.UsedHolders ++;
    }

    public void setUsedGlasses(Integer used){
        this.UsedGlasses = used;
    }

    public void increaseUsedGlasses(){
        this.UsedGlasses ++;
    }

    public Integer getTotalLeft(){
        Integer left = getBottleLeft() + getCapsLeft() +  getHoldersLeft() +  getGlassesLeft();
        return left;
    }
}
