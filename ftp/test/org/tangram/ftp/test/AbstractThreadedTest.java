/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.tangram.ftp.test;


/**
 * Provided concurrency means for FTP tests so that threads don't wait for themselves.
 */
public class AbstractThreadedTest {

    private Thread t;


    protected void concurrentSleep(MockSession session) throws InterruptedException {
        t = new Thread(session);
        t.start();
        Thread.sleep(100);
    } // concurrentSleep()


    protected void join() throws InterruptedException {
        t.join(5000);
    } // join()

} // AbstractThreadedTest
