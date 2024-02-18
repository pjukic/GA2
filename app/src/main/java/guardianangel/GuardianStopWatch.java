package guardianangel;

public class GuardianStopWatch {
    private long startTimeMillis = 0;
    private long endTimeMillis = 0;

    public void startTracking(){
        startTimeMillis = System.currentTimeMillis();
    }

    public void stopTracking(){
        endTimeMillis = System.currentTimeMillis();
    }

    public boolean passedTimeWasLessThen(long passedTimeInMillis) {
        long diff = endTimeMillis - startTimeMillis;
        return passedTimeInMillis > diff;
    }

    public void reset(){
        startTimeMillis = 0;
        endTimeMillis = 0;
    }
}
