package caiso.esb.exceptions;

public class NoServiceConfiguredException extends Exception {
    private String soapAction;

    public NoServiceConfiguredException(String soapAction) {
        super("ERROR: No Service configured for " + soapAction);
        this.soapAction = soapAction;
    }

    @Override
    public String toString() {
        return "NoServiceConfiguredException{" +
                "soapAction='" + soapAction + '\'' +
                "} " + super.toString();
    }
}
