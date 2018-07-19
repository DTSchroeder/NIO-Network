package playground;

import java.nio.ByteBuffer;

public class DirectBufferTests {

    public static void main(String[] args) {

        ByteBuffer dbb = ByteBuffer.allocateDirect(10000);
    }
}
