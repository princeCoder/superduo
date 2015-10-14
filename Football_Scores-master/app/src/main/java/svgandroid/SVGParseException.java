package svgandroid;

/**
 * Created by Prinzly Ngotoum on 10/13/15.
 */
public class SVGParseException extends RuntimeException {

    public SVGParseException(String s) {
        super(s);
    }

    public SVGParseException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public SVGParseException(Throwable throwable) {
        super(throwable);
    }
}