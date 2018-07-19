package playground;

import java.nio.ByteBuffer;

public class DirectBufferTests {

//    public static void main(String[] args) {
//
//        ByteBuffer dbb = ByteBuffer.allocateDirect(10000);
//    }


    public static void main(String[] args) {

        final String HEADER = "header";

        final String BODY = "This is the body";


        ByteBuffer headerBuffer = ByteBuffer.allocateDirect(HEADER.length());

        ByteBuffer bodyBuffer   = ByteBuffer.allocateDirect(BODY.length());

        ByteBuffer[] buffers = { headerBuffer, bodyBuffer };


        for (int i = 0; i < HEADER.length(); i++)

            headerBuffer.putChar(HEADER.charAt(i));


        for (int i = 0; i < BODY.length(); i++)

            bodyBuffer.putChar(BODY.charAt(i));



    }


}
