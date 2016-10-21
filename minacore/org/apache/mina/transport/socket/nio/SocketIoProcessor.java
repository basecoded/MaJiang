/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *  
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License. 
 *  
 */
package org.apache.mina.transport.socket.nio;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

import org.apache.mina.common.ByteBuffer;
import org.apache.mina.common.ExceptionMonitor;
import org.apache.mina.common.IdleStatus;
import org.apache.mina.common.IoSession;
import org.apache.mina.common.WriteTimeoutException;
import org.apache.mina.common.IoFilter.WriteRequest;
import org.apache.mina.util.NamePreservingRunnable;
import org.apache.mina.util.Queue;

import edu.emory.mathcs.backport.java.util.concurrent.Executor;

/**
 * Performs all I/O operations for sockets which is connected or bound. This class is used by MINA internally.
 *
 * @author The Apache Directory Project (mina-dev@directory.apache.org)
 * @version $Rev$, $Date$,
 */
class SocketIoProcessor {

    /**
     * The maximum loop count for a write operation until
     * {@link #write(IoSession, IoBuffer)} returns non-zero value.
     * It is similar to what a spin lock is for in concurrency programming.
     * It improves memory utilization and write throughput significantly.
     */
    private static final int WRITE_SPIN_COUNT = 256;
    
    private final Object lock = new Object();

    private final String threadName;

    private final Executor executor;

    /**
     * @noinspection FieldAccessedSynchronizedAndUnsynchronized
     */
    private Selector selector;

    private final Queue newSessions = new Queue();

    private final Queue removingSessions = new Queue();

    private final Queue flushingSessions = new Queue();

    private final Queue trafficControllingSessions = new Queue();

    private Worker worker;

    private long lastIdleCheckTime = System.currentTimeMillis();

    SocketIoProcessor(String threadName, Executor executor) {
        this.threadName = threadName;
        this.executor = executor;
    }

    void addNew(SocketSessionImpl session) throws IOException {
        synchronized (newSessions) {
            newSessions.push(session);
        }

        startupWorker();
    }

    void remove(SocketSessionImpl session) throws IOException {
        scheduleRemove(session);
        startupWorker();
    }

    private Selector getSelector() {
        synchronized (lock) {
            return this.selector;
        }
    }
    
    private void startupWorker() throws IOException {
        synchronized (lock) {
            if (worker == null) {
                selector = Selector.open();
                worker = new Worker();
                executor.execute(new NamePreservingRunnable(worker, threadName));
            }
            selector.wakeup();
        }
    }

    void flush(SocketSessionImpl session) {
        if (scheduleFlush(session)) {
            Selector selector = getSelector();
            if (selector != null) {
                selector.wakeup();
            }
        }
    }

    void updateTrafficMask(SocketSessionImpl session) {
        scheduleTrafficControl(session);
        Selector selector = getSelector();
        if (selector != null) {
            selector.wakeup();
        }
    }

    private void scheduleRemove(SocketSessionImpl session) {
        synchronized (removingSessions) {
            removingSessions.push(session);
        }
    }

    private boolean scheduleFlush(SocketSessionImpl session) {
        if (session.setScheduledForFlush(true)) {
            synchronized (flushingSessions) {
                flushingSessions.push(session);
            }
            
            return true;
        }
        
        return false;
    }

    private void scheduleTrafficControl(SocketSessionImpl session) {
        synchronized (trafficControllingSessions) {
            trafficControllingSessions.push(session);
        }
    }

    private void doAddNew() {
        if (newSessions.isEmpty())
            return;

        Selector selector = getSelector();
        for (;;) {
            SocketSessionImpl session;

            synchronized (newSessions) {
                session = (SocketSessionImpl) newSessions.pop();
            }

            if (session == null)
                break;

            SocketChannel ch = session.getChannel();
            try {
                ch.configureBlocking(false);
                session.setSelectionKey(ch.register(selector,
                        SelectionKey.OP_READ, session));

                // AbstractIoFilterChain.CONNECT_FUTURE is cleared inside here
                // in AbstractIoFilterChain.fireSessionOpened().
                session.getServiceListeners().fireSessionCreated(session);
            } catch (IOException e) {
                // Clear the AbstractIoFilterChain.CONNECT_FUTURE attribute
                // and call ConnectFuture.setException().
                session.getFilterChain().fireExceptionCaught(session, e);
            }
        }
    }

    private void doRemove() {
        if (removingSessions.isEmpty())
            return;

        for (;;) {
            SocketSessionImpl session;

            synchronized (removingSessions) {
                session = (SocketSessionImpl) removingSessions.pop();
            }

            if (session == null)
                break;

            SocketChannel ch = session.getChannel();
            SelectionKey key = session.getSelectionKey();
            // Retry later if session is not yet fully initialized.
            // (In case that Session.close() is called before addSession() is processed)
            if (key == null) {
                scheduleRemove(session);
                break;
            }
            // skip if channel is already closed
            if (!key.isValid()) {
                continue;
            }

            try {
                key.cancel();
                ch.close();
            } catch (IOException e) {
                session.getFilterChain().fireExceptionCaught(session, e);
            } finally {
                releaseWriteBuffers(session);
                session.getServiceListeners().fireSessionDestroyed(session);
            }
        }
    }

    private void process(Set selectedKeys) {
        Iterator it = selectedKeys.iterator();

        while (it.hasNext()) {
            SelectionKey key = (SelectionKey) it.next();
            SocketSessionImpl session = (SocketSessionImpl) key.attachment();

            if (key.isReadable() && session.getTrafficMask().isReadable()) {
                read(session);
            }

            if (key.isWritable() && session.getTrafficMask().isWritable()) {
                scheduleFlush(session);
            }
        }

        selectedKeys.clear();
    }

    private void read(SocketSessionImpl session) {
        ByteBuffer buf = ByteBuffer.allocate(session.getReadBufferSize());
        SocketChannel ch = session.getChannel();

        try {
            int readBytes = 0;
            int ret;

            try {
                while ((ret = ch.read(buf.buf())) > 0) {
                    readBytes += ret;
                }
            } finally {
                buf.flip();
            }

            session.increaseReadBytes(readBytes);

            if (readBytes > 0) {
                session.getFilterChain().fireMessageReceived(session, buf);
                buf = null;
                
                if (readBytes * 2 < session.getReadBufferSize()) {
                    session.decreaseReadBufferSize();
                } else if (readBytes == session.getReadBufferSize()) {
                    session.increaseReadBufferSize();
                }
            }
            if (ret < 0) {
                scheduleRemove(session);
            }
        } catch (Throwable e) {
            if (e instanceof IOException)
                scheduleRemove(session);
            session.getFilterChain().fireExceptionCaught(session, e);
        } finally {
            if (buf != null)
                buf.release();
        }
    }

    private void notifyIdleness() {
        // process idle sessions
        long currentTime = System.currentTimeMillis();
        if ((currentTime - lastIdleCheckTime) >= 1000) {
            lastIdleCheckTime = currentTime;
            Selector selector = getSelector();
            Set keys = selector.keys();
            if (keys != null) {
                for (Iterator it = keys.iterator(); it.hasNext();) {
                    SelectionKey key = (SelectionKey) it.next();
                    SocketSessionImpl session = (SocketSessionImpl) key
                            .attachment();
                    notifyIdleness(session, currentTime);
                }
            }
        }
    }

    private void notifyIdleness(SocketSessionImpl session, long currentTime) {
        notifyIdleness0(session, currentTime, session
                .getIdleTimeInMillis(IdleStatus.BOTH_IDLE),
                IdleStatus.BOTH_IDLE, Math.max(session.getLastIoTime(), session
                        .getLastIdleTime(IdleStatus.BOTH_IDLE)));
        notifyIdleness0(session, currentTime, session
                .getIdleTimeInMillis(IdleStatus.READER_IDLE),
                IdleStatus.READER_IDLE, Math.max(session.getLastReadTime(),
                        session.getLastIdleTime(IdleStatus.READER_IDLE)));
        notifyIdleness0(session, currentTime, session
                .getIdleTimeInMillis(IdleStatus.WRITER_IDLE),
                IdleStatus.WRITER_IDLE, Math.max(session.getLastWriteTime(),
                        session.getLastIdleTime(IdleStatus.WRITER_IDLE)));

        notifyWriteTimeout(session, currentTime, session
                .getWriteTimeoutInMillis(), session.getLastWriteTime());
    }

    private void notifyIdleness0(SocketSessionImpl session, long currentTime,
            long idleTime, IdleStatus status, long lastIoTime) {
        if (idleTime > 0 && lastIoTime != 0
                && (currentTime - lastIoTime) >= idleTime) {
            session.increaseIdleCount(status);
            session.getFilterChain().fireSessionIdle(session, status);
        }
    }

    private void notifyWriteTimeout(SocketSessionImpl session,
            long currentTime, long writeTimeout, long lastIoTime) {
        SelectionKey key = session.getSelectionKey();
        if (writeTimeout > 0 && (currentTime - lastIoTime) >= writeTimeout
                && key != null && key.isValid()
                && (key.interestOps() & SelectionKey.OP_WRITE) != 0) {
            session.getFilterChain().fireExceptionCaught(session,
                    new WriteTimeoutException());
        }
    }

    private void doFlush() {
        if (flushingSessions.size() == 0)
            return;

        for (;;) {
            SocketSessionImpl session;

            synchronized (flushingSessions) {
                session = (SocketSessionImpl) flushingSessions.pop();
            }

            if (session == null)
                break;
            
            session.setScheduledForFlush(false);

            if (!session.isConnected()) {
                releaseWriteBuffers(session);
                continue;
            }

            SelectionKey key = session.getSelectionKey();
            // Retry later if session is not yet fully initialized.
            // (In case that Session.write() is called before addSession() is processed)
            if (key == null) {
                scheduleFlush(session);
                break;
            }

            // Skip if the channel is already closed.
            if (!key.isValid()) {
                continue;
            }

            try {
                boolean flushedAll = doFlush(session);
                if (flushedAll && !session.getWriteRequestQueue().isEmpty() && !session.isScheduledForFlush()) {
                    scheduleFlush(session);
                }
            } catch (IOException e) {
                scheduleRemove(session);
                session.getFilterChain().fireExceptionCaught(session, e);
            }
        }
    }

    private void releaseWriteBuffers(SocketSessionImpl session) {
        Queue writeRequestQueue = session.getWriteRequestQueue();
        WriteRequest req;

        if ((req = (WriteRequest) writeRequestQueue.pop()) != null) {
            ByteBuffer buf = (ByteBuffer) req.getMessage();
            try {
                buf.release();
            } catch (IllegalStateException e) {
                session.getFilterChain().fireExceptionCaught(session, e);
            } finally {
                // The first unwritten empty buffer must be
                // forwarded to the filter chain.
                if (buf.hasRemaining()) {
                    req.getFuture().setWritten(false);
                } else {
                    session.getFilterChain().fireMessageSent(session, req);                    
                }
            }

            // Discard others.
            while ((req = (WriteRequest) writeRequestQueue.pop()) != null) {
                try {
                    ((ByteBuffer) req.getMessage()).release();
                } catch (IllegalStateException e) {
                    session.getFilterChain().fireExceptionCaught(session, e);
                } finally {
                    req.getFuture().setWritten(false);
                }
            }
        }
    }

    private boolean doFlush(SocketSessionImpl session) throws IOException {
        SocketChannel ch = session.getChannel();
        if (!ch.isConnected()) {
            scheduleRemove(session);
            return false;
        }
        
        // Clear OP_WRITE
        SelectionKey key = session.getSelectionKey();
        key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));

        Queue writeRequestQueue = session.getWriteRequestQueue();

        int writtenBytes = 0;
        int maxWrittenBytes = ((SocketSessionConfig) session.getConfig()).getSendBufferSize() << 1;
        try {
            for (;;) {
                WriteRequest req;
    
                synchronized (writeRequestQueue) {
                    req = (WriteRequest) writeRequestQueue.first();
                }
    
                if (req == null)
                    break;
    
                ByteBuffer buf = (ByteBuffer) req.getMessage();
                if (buf.remaining() == 0) {
                    synchronized (writeRequestQueue) {
                        writeRequestQueue.pop();
                    }
    
                    buf.reset();
                    
                    if (!buf.hasRemaining()) {
                        session.increaseWrittenMessages();
                    }
                    
                    session.getFilterChain().fireMessageSent(session, req);
                    continue;
                }
    
                int localWrittenBytes = 0;
                for (int i = WRITE_SPIN_COUNT; i > 0; i --) {
                    localWrittenBytes = ch.write(buf.buf());
                    if (localWrittenBytes != 0 || !buf.hasRemaining()) {
                        break;
                    }
                }

                writtenBytes += localWrittenBytes;

                if (localWrittenBytes == 0 || writtenBytes >= maxWrittenBytes) {
                    // Kernel buffer is full or wrote too much.
                    key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                    return false;
                }
            }
        } finally {
            session.increaseWrittenBytes(writtenBytes);
        }
        
        return true;
    }

    private void doUpdateTrafficMask() {
        if (trafficControllingSessions.isEmpty())
            return;

        for (;;) {
            SocketSessionImpl session;

            synchronized (trafficControllingSessions) {
                session = (SocketSessionImpl) trafficControllingSessions.pop();
            }

            if (session == null)
                break;

            SelectionKey key = session.getSelectionKey();
            // Retry later if session is not yet fully initialized.
            // (In case that Session.suspend??() or session.resume??() is 
            // called before addSession() is processed)
            if (key == null) {
                scheduleTrafficControl(session);
                break;
            }
            // skip if channel is already closed
            if (!key.isValid()) {
                continue;
            }

            // The normal is OP_READ and, if there are write requests in the
            // session's write queue, set OP_WRITE to trigger flushing.
            int ops = SelectionKey.OP_READ;
            Queue writeRequestQueue = session.getWriteRequestQueue();
            synchronized (writeRequestQueue) {
                if (!writeRequestQueue.isEmpty()) {
                    ops |= SelectionKey.OP_WRITE;
                }
            }

            // Now mask the preferred ops with the mask of the current session
            int mask = session.getTrafficMask().getInterestOps();
            key.interestOps(ops & mask);
        }
    }

    private class Worker implements Runnable {
        public void run() {
            Selector selector = getSelector();
            for (;;) {
                try {
                    int nKeys = selector.select(1000);
                    doAddNew();
                    doUpdateTrafficMask();

                    if (nKeys > 0) {
                        process(selector.selectedKeys());
                    }

                    doFlush();
                    doRemove();
                    notifyIdleness();

                    if (selector.keys().isEmpty()) {
                        synchronized (lock) {
                            if (selector.keys().isEmpty()
                                    && newSessions.isEmpty()) {
                                worker = null;

                                try {
                                    selector.close();
                                } catch (IOException e) {
                                    ExceptionMonitor.getInstance()
                                            .exceptionCaught(e);
                                } finally {
                                    selector = null;
                                }

                                break;
                            }
                        }
                    }
                } catch (Throwable t) {
                    ExceptionMonitor.getInstance().exceptionCaught(t);

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e1) {
                        ExceptionMonitor.getInstance().exceptionCaught(e1);
                    }
                }
            }
        }
    }
}
