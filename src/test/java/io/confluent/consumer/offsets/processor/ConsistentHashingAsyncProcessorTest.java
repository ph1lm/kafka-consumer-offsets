package io.confluent.consumer.offsets.processor;

import io.confluent.consumer.offsets.function.IdentityFunction;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.mockito.stubbing.Answer;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ConsistentHashingAsyncProcessorTest {

  private static final int NUMBER_OF_THREADS = 3;
  private static final int[] INTS = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private Processor<Object, Object> processor;

  private ConsistentHashingAsyncProcessor<Object, Object> consistentHashingAsyncProcessor;
  private ThreadIdCachingAnswer threadIdCachingAnswer;
  private LatchCountingAnswer closeAnswer;

  @Before
  public void setUp() throws Exception {
    this.threadIdCachingAnswer = new ThreadIdCachingAnswer(INTS.length);
    this.closeAnswer = new LatchCountingAnswer(NUMBER_OF_THREADS);
    doAnswer(this.threadIdCachingAnswer).when(this.processor).process(any(), any());
    doAnswer(this.closeAnswer).when(this.processor).close();

    this.consistentHashingAsyncProcessor = new ConsistentHashingAsyncProcessor<>(NUMBER_OF_THREADS,
        new IdentityFunction<>(), this.processor);
  }

  @After
  public void tearDown() throws Exception {
    this.consistentHashingAsyncProcessor.close();
    this.closeAnswer.latch.await();
    verify(this.processor, times(NUMBER_OF_THREADS)).close();
  }

  @Test
  public void testProcess() throws Exception {
    Map<Integer, Collection<Integer>> buckets = computeBuckets();

    for (int i = 0; i < INTS.length; i++) {
      this.consistentHashingAsyncProcessor.process(i, i);
    }

    this.threadIdCachingAnswer.latch.await();
    verify(this.processor, times(INTS.length)).process(anyInt(), anyInt());

    // check that all buckets have the same thread id
    for (Map.Entry<Integer, Collection<Integer>> bucket : buckets.entrySet()) {
      long threadId = -1;
      for (Integer anInt : bucket.getValue()) {
        long curThreadId = this.threadIdCachingAnswer.threadIdCache.get(anInt);
        if (threadId == -1) {
          threadId = curThreadId;
        }
        assertEquals(threadId, curThreadId);
      }
    }
  }

  private static Map<Integer, Collection<Integer>> computeBuckets() {
    Map<Integer, Collection<Integer>> buckets = new HashMap<>();
    for (int anInt : INTS) {
      int bucket = anInt % NUMBER_OF_THREADS;
      Collection<Integer> ints = buckets.get(bucket);
      if (ints == null) {
        ints = new HashSet<>();
      }
      ints.add(anInt);
      buckets.put(bucket, ints);
    }
    return buckets;
  }

  private static class ThreadIdCachingAnswer extends LatchCountingAnswer {

    final ConcurrentMap<Object, Long> threadIdCache = new ConcurrentHashMap<>();

    ThreadIdCachingAnswer(int numberOfTasks) {
      super(numberOfTasks);
    }

    @Override
    public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
      assertNull(this.threadIdCache.putIfAbsent(invocationOnMock.getArgumentAt(0, Object.class),
          Thread.currentThread().getId()));
      return super.answer(invocationOnMock);
    }
  }

  private static class LatchCountingAnswer implements Answer<Void> {

    final CountDownLatch latch;

    LatchCountingAnswer(int counts) {
      this.latch = new CountDownLatch(counts);
    }

    @Override
    public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
      this.latch.countDown();
      return null;
    }
  }
}