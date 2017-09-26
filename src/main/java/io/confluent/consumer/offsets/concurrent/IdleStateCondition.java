package io.confluent.consumer.offsets.concurrent;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * This class helps to determine idle state of some process.
 * It allows supervisor process to block execution using {@link #sync()} method or wait for {@link #async} event.
 * till {@link #idleStateTimeoutSecs} occurred.
 * It basically schedules a timer task that is rescheduled every time {@link #postpone()} method is called by
 * process we've put an eye on.
 *
 * @author Philip Mikhailov
 */
public class IdleStateCondition {

  private static final int MAX_QUEUE_SIZE = 100;
  private final AtomicReference<ScheduledFuture<?>> currentScheduledFuture = new AtomicReference<>();
  private final ScheduledThreadPoolExecutor scheduledThreadPoolExecutor;
  private final long idleStateTimeoutSecs;
  private volatile Runnable idleStateCallback;

  public IdleStateCondition(long idleStateTimeoutSecs) {
    this.idleStateTimeoutSecs = idleStateTimeoutSecs;
    this.scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);
  }

  public void postpone() {
    reschedule();
    maybePurgeCanceledTasks();
  }

  private void reschedule() {
    if (this.idleStateCallback == null) {
      throw new NullPointerException("idleStateCallback");
    }
    reschedule(this.scheduledThreadPoolExecutor.schedule(this.idleStateCallback, this.idleStateTimeoutSecs,
        TimeUnit.SECONDS));
  }

  private void reschedule(ScheduledFuture<?> newScheduledFuture) {
    ScheduledFuture<?> oldScheduledFuture = this.currentScheduledFuture.getAndSet(newScheduledFuture);
    if (oldScheduledFuture != null) {
      oldScheduledFuture.cancel(false);
    }
  }

  private void maybePurgeCanceledTasks() {
    // double check locking just to avoid redundant synchronization
    if (this.scheduledThreadPoolExecutor.getQueue().size() > MAX_QUEUE_SIZE) {
      synchronized (this.scheduledThreadPoolExecutor) {
        if (this.scheduledThreadPoolExecutor.getQueue().size() > MAX_QUEUE_SIZE) {
          this.scheduledThreadPoolExecutor.purge();
        }
      }
    }
  }

  public void sync() {
    try {
      final CountDownLatch latch = new CountDownLatch(1);
      async(new Runnable() {
        @Override
        public void run() {
          latch.countDown();
        }
      });
      latch.await();
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }

  public void async(Runnable idleStateCallback) {
    this.idleStateCallback = idleStateCallback;
    reschedule();
  }

  public void close() {
    try {
      reschedule(null);
    } finally {
      this.scheduledThreadPoolExecutor.shutdownNow();
    }
  }
}
