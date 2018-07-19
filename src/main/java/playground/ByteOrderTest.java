package playground;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class ByteOrderTest {

    public static void main(String[] args) {

        ByteOrder nbo = ByteOrder.nativeOrder();

        ByteBuffer bb = ByteBuffer.allocate(100);

        java.nio.ByteOrder bbo = bb.order();

        System.out.println("native byte order is: " + nbo + "  but the order within a bytebuffer is always: " + bbo);
    }
}
