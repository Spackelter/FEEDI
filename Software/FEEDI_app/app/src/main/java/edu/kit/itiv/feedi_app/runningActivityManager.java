package edu.kit.itiv.feedi_app;

public class runningActivityManager {

    //"Global instance"
    private static runningActivityManager instance = null;

    //variables to handle
    private int currentlyRunningMode = Const.NO_MODE;
    /*Because the point navigation contains a delayed function called cyclically
    *and it needs to be checked whether a new point navigation has been started before
    * the end of the delay or a new navigation is started while another navigation is still running
    */
    private int currentPointNavigationID = -1;

    private runningActivityManager(){

    }

    //To access a "global" instance of BleManager from anywhere
    public static runningActivityManager getInstance() {
        if (instance == null) {
            instance = new runningActivityManager();
        }
        return instance;
    }

    //called on the start point navigation button click
    public void incrementPointNavigationID(){
        currentPointNavigationID++;
    }

    //getters and setters
    public void setToNoActiveMode(){
        currentlyRunningMode = Const.NO_MODE;
    }

    public void setPointToNorthActive(){
        currentlyRunningMode = Const.MODE_POINT_TO_NORTH;
    }

    public void setPointNavigationCompassViewActive(){
        currentlyRunningMode = Const.MODE_POINT_NAVIGATION_COMPASSVIEW;
    }

    public void setPointNavigationMapViewActive(){
        currentlyRunningMode = Const.MODE_POINT_NAVIGATION_MAPVIEW;
    }

    public boolean getPointNavigationIsActive(){
        if(currentlyRunningMode==Const.MODE_POINT_NAVIGATION_COMPASSVIEW ||
                currentlyRunningMode==Const.MODE_POINT_NAVIGATION_MAPVIEW ){
            return true;
        }
        return false;
    }

    public boolean getPointNavigationCompassViewIsActive(){
        if(currentlyRunningMode==Const.MODE_POINT_NAVIGATION_COMPASSVIEW){
            return true;
        }
        return false;
    }

    public boolean getPointNavigationMapViewIsActive(){
        if(currentlyRunningMode==Const.MODE_POINT_NAVIGATION_MAPVIEW){
            return true;
        }
        return false;
    }

    public int getCurrentPointNavigationID(){
        return currentPointNavigationID;
    }

    public boolean getPointToNorthIsActive(){
        if(currentlyRunningMode==Const.MODE_POINT_TO_NORTH){
            return true;
        }
        return false;
    }

    public int getActiveMode(){
        return currentlyRunningMode;
    }


}
