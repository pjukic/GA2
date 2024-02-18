package guardianangel.exceptions;

public class RecorderIsAlreadyRecordingException extends Exception{
    public RecorderIsAlreadyRecordingException(String errormsg){
        super(errormsg);
    }
}
