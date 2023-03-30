package fr.ramatellier.greed.server.compute;

import org.junit.jupiter.api.Test;

import java.net.InetSocketAddress;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SharingProcessExecutorTest {
    @Test
    public void testSizeAfterComputing(){
        var availableSocketList = List.of(
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8080), 10),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8081), 10),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8082), 10),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8083), 10),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8084), 10),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8085), 10),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8086), 10),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8087), 10),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8088), 10),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8089), 10)
        );
        var sharingProcessExecutor = new SharingProcessExecutor(availableSocketList, 100);
        var socketRangeList = sharingProcessExecutor.shareAndGet();
        assertEquals(socketRangeList.size(), 10);
    }

    @Test
    public void ifSocketHasSize0ItShouldBeIgnore(){
        var availableSocketList = List.of(
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8080), 0),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8081), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8082), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8083), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8084), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8085), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8086), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8087), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8088), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8089), 100)
        );
        var sharingProcessExecutor = new SharingProcessExecutor(availableSocketList, 100);
        var socketRangeList = sharingProcessExecutor.shareAndGet();
        assertEquals(socketRangeList.size(), 9);
    }
    @Test
    public void sharingShouldBeFairWhenHeCan(){
        var availableSocketList = List.of(
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8080), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8081), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8082), 100),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8083), 100)

        );
        var sharingProcessExecutor = new SharingProcessExecutor(availableSocketList, 99);
        var result = sharingProcessExecutor.shareAndGet();
        assertTrue(
                result.stream().
                allMatch(socketRange -> socketRange.range().delta(false) >= 23)
        );
    }
    @Test
    public void sharingShouldBeFairEvenWithUcGap(){
        var availableSocketList = List.of(
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8080), 40),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8081), 25),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8082), 20),
                new SocketUcIdentifier(new InetSocketAddress("localhost", 8083), 15)
        );
        var sharingProcessExecutor = new SharingProcessExecutor(availableSocketList, 50);
        var result = sharingProcessExecutor.shareAndGet();
        assertEquals(50,
                result.stream().
                        mapToLong(socketRange -> socketRange.range().delta(false)).sum()
        );
        assertTrue(
                result.stream().
                        allMatch(socketRange -> socketRange.range().delta(false) >= 12 && socketRange.range().delta(false) <= 13)
        );
    }
}
