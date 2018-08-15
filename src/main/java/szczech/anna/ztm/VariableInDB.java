/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package szczech.anna.ztm;

/**
 *
 * @author Ania
 */
public class VariableInDB {

    private String conveyance;
    private String lineNumber;
    private int routeNumber;
    private String time;
    private int routeTime;
    private String initialStop;
    private String finalStop;
    private String initialStopNumber;
    private String finalStopNumber;

    public String getInitialStopNumber() {
        return initialStopNumber;
    }

    public String getFinalStopNumber() {
        return finalStopNumber;
    }

    public VariableInDB(String conveyance, String lineNumber, int routeNumber, String time, int routeTime, String initialStop, String finalStop, String initialStopNumber, String finalStopNumber) {
        this.conveyance = conveyance;
        this.lineNumber = lineNumber;
        this.initialStop = initialStop;
        this.finalStop = finalStop;
        this.initialStopNumber = initialStopNumber;
        this.finalStopNumber = finalStopNumber;
        this.routeNumber = routeNumber;
        this.time = time;
        this.routeTime = routeTime;
    }

    public String getConveyance() {
        return conveyance;
    }

    public String getLineNumber() {
        return lineNumber;
    }

    public int getRouteNumber() {
        return routeNumber;
    }

    public String getTime() {
        return time;
    }

    public int getRouteTime() {
        return routeTime;
    }

    public String getInitialStop() {
        return initialStop;
    }

    public String getFinalStop() {
        return finalStop;
    }

}
