package fr.ramatellier.greed.server.util.http;


import fr.ramatellier.greed.server.reader.Reader;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * <p>
 * Tests suit for the class HTTPReader
 */
public class HTTPReaderTest {

    @Test
    public void readHeaderTest() throws IOException{
        var reader = new HTTPHeaderReader();
        var bb = ByteBuffer.allocate(1024);
        bb.put("HTTP/1.1 200 OK\nDate: Thu, 01 Mar 2018 17:28:07 GMT\nServer: Apache\nLast-Modified: Thu, 15 Sep 2016 09:02:49 GMT\nETag: \"254441f-3d0a-53c881c25a040\"\nAccept-Ranges: bytes\nContent-Length: 15626\nContent-Type: text/html\r\n\r\n"
                .getBytes(StandardCharsets.US_ASCII));
        var response = reader.process(bb);
        assertEquals(response, Reader.ProcessStatus.DONE);
        var header = reader.get();
        assertNotNull(header);
        assertEquals(200, header.getCode());
        assertEquals(15626, header.getContentLength());
        assertTrue(header.getContentType().isPresent());
        assertEquals("text/html", header.getContentType().get());
    }

    @Test
    public void readMoreThanJustHeaderTest() throws IOException{
        var reader = new HTTPHeaderReader();
        var bb = ByteBuffer.allocate(1024);
        bb.put("HTTP/1.1 200 OK\nDate: Thu, 01 Mar 2018 17:28:07 GMT\nServer: Apache\nLast-Modified: Thu, 15 Sep 2016 09:02:49 GMT\nETag: \"254441f-3d0a-53c881c25a040\"\nAccept-Ranges: bytes\nContent-Length: 15626\nContent-Type: text/html\r\n\r\nBlablablablablablablaIGNORED"
                .getBytes(StandardCharsets.UTF_8));
        var response = reader.process(bb);
        assertEquals(Reader.ProcessStatus.DONE, response);
        var header = reader.get();
        assertNotNull(header);
        assertEquals(200, header.getCode());
        assertEquals(15626, header.getContentLength());
        assertTrue(header.getContentType().isPresent());
        assertEquals("text/html", header.getContentType().get());
        ByteBuffer buffFinal = ByteBuffer.wrap("BlablablablablablablaIGNORED".getBytes(StandardCharsets.US_ASCII)).compact();
        assertEquals(buffFinal.flip(), bb.flip());
    }


    @Test
    public void basicHeaderReaderTest() {
        var reader = new HTTPHeaderReader();
        var bb = ByteBuffer.allocate(1024);
        bb.put("HTTP/1.1 200 OK\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        assertEquals(Reader.ProcessStatus.DONE, reader.process(bb));
        var header = reader.get();
        assertNotNull(header);
        assertEquals(200, header.getCode());
    }

    @Test
    public void headerWithContentReaderTest() throws HTTPException {
        var reader = new HTTPHeaderReader();
        var bb = ByteBuffer.allocate(1024);
        bb.put("HTTP/1.1 200 OK\r\nContent-Length: 10\r\n\r\n".getBytes(StandardCharsets.US_ASCII));
        assertEquals(Reader.ProcessStatus.DONE, reader.process(bb));
        var header = reader.get();
        assertNotNull(header);
        assertEquals(200, header.getCode());
        assertEquals(10, header.getContentLength());
    }

    @Test
    public void simpleHeaderWithBodyTest(){
        var httpReader = new HTTPReader();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        byte[] responseBytes = ("""
                HTTP/1.1 200 OK\r
                Content-Type: text/plain\r
                Content-Length: 12\r
                \r
                Hello World!""").getBytes();
        byteBuffer.put(responseBytes);
        var status = httpReader.process(byteBuffer);

        assertEquals(Reader.ProcessStatus.DONE, status);

        var responseBody = httpReader.get();

        assertEquals(12, responseBody.length);
        assertEquals("Hello World!", new String(responseBody));
        assertArrayEquals("Hello World!".getBytes(), responseBody);
    }
    @Test
    public void incompleteHttpResponseTest(){
        var httpReader = new HTTPReader();
        ByteBuffer byteBuffer = ByteBuffer.allocate(1024);

        byte[] incompleteResponseBytes = ("""
                HTTP/1.1 200 OK\r
                Content-Type: text/plain\r
                Content-Length: 12\r
                \r
                Hello\s""").getBytes();
        var responseBytesLeft = ("World!").getBytes();
        byteBuffer.put(incompleteResponseBytes);
        var status = httpReader.process(byteBuffer);

        assertEquals(Reader.ProcessStatus.REFILL, status);

        byteBuffer.put(responseBytesLeft);
        status = httpReader.process(byteBuffer);
        assertEquals(Reader.ProcessStatus.DONE, status);

        var responseBody = httpReader.get();

        assertEquals(12, responseBody.length);
        assertEquals("Hello World!", new String(responseBody));
        assertArrayEquals("Hello World!".getBytes(), responseBody);
    }
}