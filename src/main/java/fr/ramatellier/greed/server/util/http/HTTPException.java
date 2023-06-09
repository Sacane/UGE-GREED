package fr.ramatellier.greed.server.util.http;

import java.io.IOException;
import java.io.Serial;
/**
 * Property of Gustave eiffel University.
 */
public class HTTPException extends IOException {

    @Serial
    private static final long serialVersionUID = -1810727803680020453L;

    public HTTPException() {
        super();
    }

    public HTTPException(String s) {
        super(s);
    }

    public static void ensure(boolean b, String string) throws HTTPException {
        if (!b){
            throw new HTTPException(string);
        }
    }
}