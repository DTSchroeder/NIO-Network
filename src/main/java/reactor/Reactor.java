package reactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

// ------------------------------------------------------------
//                          REACTOR
// ------------------------------------------------------------
//
//
// PROBLEM
// Event-driven  applications in  a dist. system,  particularly
// servers, [1] must be  prepared  to  handle multiple  service
// requests   simultaneously,  even  if   those  requests   are
// ultimately  processed serially  within the  application. The
// arrival  of   each  request  is  identified  by  a  specific
// indication event, such as the CONNECT and READ events in our
// logging example. Before executing specific services serially
// therefore, an event-driven application  must demultiplex and
// dispatch the  concurrently-arriving indication events to the
// corresponding service implementations.
//
// Resolving  this problem effectively  requires the resolution
// of four forces:
//
// -- To improve scalability and latency, an application should
//    not block on any  single source of  indication events and
//    exclude other event sources,because blocking on one event
//    source can degrade the servers responsiveness to clients.
// -- To maximize throughput,any unnecessary context switching,
//    synchronization, and  data movement  among CPUs should be
//    avoided, as outlined in the Example section.
// -- Integrating  new   or  improved  services  with  existing
//    indication event demultiplexing and dispatching mechanism
//    should require minimal effort.
// -- Application  code  should largely  be  shielded  from the
//    complexity of multithreading & synchronization  mechanism
//
//
// SOLUTION:
// Synchronously  wait for  the arrival of indication events on
// one or more event sources, such as connected socket handles.
// Integrate  the mechanisms that  demultiplex and dispatch the
// events to services that  process them. Decouple  these event
// demultiplexing   and   dispatching   mechanisms   from   the
// application-specific  processing of indication events within
// the  services. In  detail: for  each  service an application
// offers, introduce  a separate  event handler  that processes
// certain  types of  events from  certain event sources. Event
// handlers  register with a  reactor, which uses a synchronous
// event demultiplexer  to wait  for indication events to occur
// on one or  more event sources. When indication events occur,
// the synchronous  event  demultiplexer  notifies the reactor,
// which  then  synchronously  dispatches  the   event  handler
// associated  with  the  event  so  that  it  can  perform the
// requested service.
//
//
public class Reactor implements Runnable {

    final ServerSocketChannel serverSocket;

    final Selector selector;

    // -- ACCEPTOR --------------------------------------
    //
    class Acceptor implements Runnable {

        public void run() {

            try {

                SocketChannel channel = serverSocket.accept();

                if(channel != null) {

                    new Handlers.HandlerStateObjectPattern(selector, channel);
                }
            }
            catch (IOException e) {

                e.printStackTrace();
            }
        }
    }


    Reactor(int port) throws IOException {

        selector = Selector.open();

        serverSocket = ServerSocketChannel.open();

        serverSocket.socket().bind(new InetSocketAddress(port));

        serverSocket.configureBlocking(false);

        serverSocket.register(selector, SelectionKey.OP_ACCEPT)

                .attach(new Acceptor());
    }


    public void run() {

        try {

            while (!Thread.interrupted()) {

                selector.select();

                Set selected = selector.selectedKeys();

                Iterator it = selected.iterator();

                while (it.hasNext()) {

                    dispatch((SelectionKey) it.next());
                }

                selected.clear();
            }
        }
        catch (IOException e) {

            e.printStackTrace();
        }
    }


    void dispatch(SelectionKey key) {

        Runnable runnable = (Runnable) key.attachment();

        if(runnable != null) {

            runnable.run();
        }
    }
}
