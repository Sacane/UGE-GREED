package fr.ramatellier.greed.server;

import fr.ramatellier.greed.server.util.ComputeCommandParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ComputeCommandParserTest {

    @Test
    public void ensureNullCommandThrows() {
        assertThrows(NullPointerException.class, () -> new ComputeCommandParser(null));
    }
    @Test
    public void ensureCheckInvalidCommandReturnsFalse() {
        var parser = new ComputeCommandParser("invalid");
        assertFalse(parser.check());
        assertThrows(IllegalStateException.class, parser::get);
    }

    @Test
    public void validCommandCheckReturnsTrue(){
        var parser = new ComputeCommandParser("http://localhost:8080/Compute.jar Compute 0 100");
        assertTrue(parser.check());
        var computeInfo = parser.get();
        assertEquals("http://localhost:8080/Compute.jar", computeInfo.url());
        assertEquals("Compute", computeInfo.className());
        assertEquals(0, computeInfo.start());
        assertEquals(100, computeInfo.end());
    }
}
