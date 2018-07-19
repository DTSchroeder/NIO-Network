package reactor;

import com.sun.java.swing.plaf.windows.resources.windows;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.Channels;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import static java.nio.channels.SelectionKey.OP_READ;
import static java.nio.channels.SelectionKey.OP_WRITE;

// ------------------------------------------------------------
//                          HANDLERS
// ------------------------------------------------------------
//
//
public enum Handlers {
    ;
    static final int MAX_IN  = 1024;

    static final int MAX_OUT = 1024;

    static final int READING = 0;

    static final int SENDING = 1;


    // -- HANDLER ---------------------------------------
    //
    interface Handler extends Runnable {

        void read() throws IOException;

        void send() throws IOException;

        boolean outputIsComplete();

        boolean inputIsComplete();

        void process();
    }


    // -- HANDLER IMPL I --------------------------------
    //
    static final class HandlerStateObjectPattern implements Handler {

        // Channels are the second major innovation of java.nio. They are not an extension or
        // enhancement, but a new, first-class Java I/O paradigm. They provide direct connections to I/O
        // services. A Channel is a conduit that transports data efficiently between byte buffers and the
        // entity on the other end of the channel (usually a file or socket).
        //
        // A good metaphor for a channel is a pneumatic tube, the type used at drive-up bank-teller
        // windows. Your paycheck would be the information you're sending. The carrier would be like
        // a buffer. You fill the buffer (place your paycheck in the carrier), "write" the buffer to
        // the channel (drop the carrier into the tube), and the payload is carried to the I/O service (bank
        // teller) on the other end of the channel.
        //
        // In most cases, channels have a one-to-one relationship with operating-system file descriptors,
        // or file handles. Although channels are more generalized than file descriptors, most channels
        // you will use on a regular basis are connected to open file descriptors. The channel classes
        // provide the abstraction needed to maintain platform independence but still model the native
        // I/O capabilities of modern operating systems.
        //
        // Unlike buffers, the channel APIs are primarily specified by interfaces. Channel
        // implementations vary radically between operating systems, so the channel APIs simply
        // describe what can be done. Channel implementations often use native code, so this is only
        // natural. The channel interfaces allow you to gain access to low-level I/O services in a
        // controlled and portable way.
        //
        // SocketChannel is:
        //      - Channel
        //      -- InterruptibleChannel
        //      --- SelectableChannel
        //      -- ByteChannel
        //      --- ReadableByteChannel
        //      -- WritableByteChannel
        //      --- GatheringByteChannel
        //
        // The InterruptibleChannel interface is a marker that, when implemented by a channel,
        // indicates that the channel is interruptible. Interruptible channels behave in specific ways when
        // a thread accessing them is interrupted, which we will discuss in Section 3.1.3. Most, but not
        // all, channels are interruptible.
        //
        final SocketChannel socket;

        final SelectionKey key;

        int state = READING;

        // A Buffer object is a container for a fixed amount of data. It acts as a holding tank, or staging
        // area, where data can be stored and later retrieved. Buffers are filled and drained, as we
        // discussed in Chapter 1. There is one buffer class for each of the nonboolean primitive data
        // types. Although buffers act upon the primitive data types they store, buffers have a strong bias
        // toward bytes. Nonbyte buffers can perform translation to and from bytes behind the scenes,
        // depending on how the buffer was created. We'll examine the implications of data storage
        // within buffers later in this chapter.
        //
        // Buffers work hand in glove with channels. Channels are portals through which I/O transfers
        // take place, and buffers are the sources or targets of those data transfers. For outgoing
        // transfers, data you want to send is placed in a buffer, which is passed to a channel. For
        // inbound transfers, a channel deposits data in a buffer you provide. This hand-off of buffers
        // between cooperating objects (usually objects you write and one or more Channel objects) is
        // key to efficient data handling. Channels will be covered in detail in Chapter 3.
        //
        // Conceptually, a buffer is an array of primitive data elements wrapped inside an object.
        // The advantage of a Buffer class over a simple array is that it encapsulates data content and
        // information about the data into a single object. The Buffer class and its specialized subclasses
        // define a API for processing data buffers.
        //
        ByteBuffer output;

        ByteBuffer input;


        HandlerStateObjectPattern(Selector selector, SocketChannel _socket) throws IOException {

            // Nondirect buffer! There is a heap array backing this buffer
            output = ByteBuffer.allocate(MAX_OUT);

            input = ByteBuffer.allocate(MAX_IN);

            socket = _socket;

            socket.configureBlocking(false);

            key = socket.register(selector, 0);

            key.attach(this);

            key.interestOps(OP_READ);

            selector.wakeup();
        }


        public void run() {

            try {

                if (state == READING) read();

                else if (state == SENDING) send();

            } catch (IOException e) {

                e.printStackTrace();
            }
        }


        public void read() throws IOException {

            socket.read(input);

            if (inputIsComplete()) {

                process();

                state = SENDING;

                key.interestOps(OP_WRITE);
            }
        }

        public void send() throws IOException {

            socket.write(output);

            if (outputIsComplete()) key.cancel();
        }

        public boolean inputIsComplete() { throw new NotImplementedException(); }

        public boolean outputIsComplete() { throw new NotImplementedException(); }

        public void process() { throw new NotImplementedException(); }
    }

    // -- HANDLER IMPL II -------------------------------
    //
    static final class HandlerThreadPool implements Handler {

        //static Pooled TODO

        static final int PROCESSING = 3;

        final SocketChannel socket;

        final SelectionKey key;

        int state = READING;

        ByteBuffer output;

        ByteBuffer input;


        HandlerThreadPool(Selector selector, SocketChannel _socket) throws IOException {

            output = ByteBuffer.allocate(MAX_OUT);

            input = ByteBuffer.allocate(MAX_IN);

            socket = _socket;

            socket.configureBlocking(false);

            key = socket.register(selector, 0);

            key.attach(this);

            key.interestOps(OP_READ);

            selector.wakeup();
        }


        public synchronized void read() throws IOException {

            socket.read(input);

            if(inputIsComplete()) {

                state = PROCESSING;
            }
        }

        public void send() throws IOException {

        }

        public boolean outputIsComplete() {
            return false;
        }

        public boolean inputIsComplete() {
            return false;
        }

        public void process() {

        }

        public void run() {

        }
    }

    public static void main(String[] args) {

        ByteOrder nbo = ByteOrder.nativeOrder();

        ByteBuffer bb = ByteBuffer.allocate(100);

        ByteOrder bbo = bb.order();

        System.out.println("NATIVE BYTE ORDER: " + nbo + "  BYTEBUFFER ORDER IS ALWAYS " + bbo);
    }
}





// Handles are  provided by operating systems to identify event
// sources, such as network connections or open files, that can
// generate and  queue indication events. Indication events can
// originate  from external sources, such  as CONNECT events or
// READ  events  sent to  a service  from  clients, or internal
// sources, such as time-outs. When  an indication event occurs
// on an event  source, the event is  queued on  its associated
// handle  and  the handle is marked as 'ready'. At this point,
// an operation, such as an accept() or read(),can be performed
// on the handle without blocking the calling thread.