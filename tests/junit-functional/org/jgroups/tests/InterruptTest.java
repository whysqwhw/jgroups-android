// $Id: InterruptTest.java,v 1.1 2007/07/04 07:29:33 belaban Exp $

package org.jgroups.tests;


import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;


/**
 * Tests Thread.interrupt() against InputStream.read(), Object.wait() and Thread.sleep()
 *
 * @author Bela Ban Oct 5 2001
 */
public class InterruptTest extends TestCase {
    static final long TIMEOUT=3000;
    static final int SLEEP=1;
    static final int WAIT=2;
    static final int READ=3;
    static final int SOCKET_READ=4;


    public InterruptTest(String name) {
        super(name);
    }


    String modeToString(int m) {
        switch(m) {
        case SLEEP:
            return "SLEEP";
        case WAIT:
            return "WAIT";
        case READ:
            return "READ";
        case SOCKET_READ:
            return "SOCKET_READ";
        default:
            return "<unknown>";
        }
    }


    /**
     * Starts the Interruptible and interrupts after TIMEOUT milliseconds. Then joins thread
     * (waiting for TIMEOUT msecs). PASS if thread dead, FAIL if still alive
     */
    public void testSleepInterrupt() {
        SleeperThread thread=new SleeperThread(SLEEP);
        runTest(thread);
    }


    public void testWaitInterrupt() {
        SleeperThread thread=new SleeperThread(WAIT);
        runTest(thread);
    }

/*    public void testSocketReadInterrupt() {
        SleeperThread thread=new SleeperThread(SOCKET_READ);
        runTest(thread);
    }


    public void testReadInterrupt() {
        SleeperThread thread=new SleeperThread(READ);
        runTest(thread);
    }*/


    void runTest(SleeperThread thread) {
        System.out.println();
        System.out.println("InterruptTest.runTest(" + modeToString(thread.getMode()) + "): starting other thread");
        thread.start();
        System.out.println("InterruptTest.runTest(" + modeToString(thread.getMode()) + "): starting other thread -- done");

        System.out.println("InterruptTest.runTest(" + modeToString(thread.getMode()) + "): sleeping for " + TIMEOUT + " msecs");
        sleep(TIMEOUT);
        System.out.println("InterruptTest.runTest(" + modeToString(thread.getMode()) + "): sleeping -- done");

        System.out.println("InterruptTest.runTest(" + modeToString(thread.getMode()) + "): interrupting other thread");
        thread.interrupt();
        System.out.println("InterruptTest.runTest(" + modeToString(thread.getMode()) + "): interrupting other thread -- done");

        System.out.println("InterruptTest.runTest(" + modeToString(thread.getMode()) + "): joining other thread (timeout=" + TIMEOUT + " msecs");
        try {
            thread.join(TIMEOUT);
        }
        catch(InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("InterruptTest.runTest(" + modeToString(thread.getMode()) + "): joining other thread -- done");

        System.out.println("InterruptTest.runTest(" + modeToString(thread.getMode()) + "): thread.isAlive()=" + thread.isAlive());
        assertTrue(!thread.isAlive());
    }


    void sleep(long msecs) {
        try {
            Thread.sleep(msecs);
        }
        catch(Exception ex) {
            System.err.println("InterruptTest.sleep(): " + ex);
        }
    }





    class SleeperThread extends Thread {
        int mode;
        DatagramSocket sock=null;


        SleeperThread(int mode) {
            this.mode=mode;
        }


        public int getMode() {
            return mode;
        }


        public void run() {
            switch(mode) {
            case SLEEP:
                runSleep();
                break;
            case WAIT:
                runWait();
                break;
            case READ:
                runRead();
                break;
            case SOCKET_READ:
                runSocketRead();
                break;
            default:
                break;
            }
        }


        void runSleep() {
            try {
                Thread.sleep(TIMEOUT);
            }
            catch(InterruptedException ex) {
                System.err.println("InterruptTest.SleeperThread.runSleep(): " + ex);
            }
        }

        void runWait() {
            Object mutex=new Object();
            synchronized(mutex) {
                try {
                    mutex.wait();
                }
                catch(InterruptedException ex) {
                    System.err.println("InterruptTest.SleeperThread.runWait(): " + ex);
                }
            }
        }

        void runRead() {
            try {
                System.in.read();
            }
            catch(Exception ex) {
                System.err.println("InterruptTest.SleeperThread.runRead(): " + ex);
            }
        }

        void runSocketRead() {
            byte[] buf=new byte[2];
            DatagramPacket packet;

            try {
                sock=new DatagramSocket(12345, InetAddress.getLocalHost());
                // System.out.println("** mcast_sock=" + mcast_sock.getLocalAddress() + ":" + mcast_sock.getLocalPort());
                packet=new DatagramPacket(buf, buf.length);
                //System.out.println("** receive(): start");
                sock.receive(packet);
                //System.out.println("** receive(): done");
            }
            catch(Exception e) {
                //System.out.println("** receive(): done, exception=" + e);
                System.err.println(e);
            }
        }
    }


    public static Test suite() {
        TestSuite s=new TestSuite(InterruptTest.class);
        return s;
    }

    public static void main(String[] args) {
        junit.textui.TestRunner.run(suite());
    }
}


