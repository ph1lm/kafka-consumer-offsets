package io.confluent.consumer.offsets.processor;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CompositeProcessorTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private Processor<Object, Object> processor1;

  @Mock
  private Processor<Object, Object> processor2;

  @Mock
  private Processor<Object, Object> failingProcessor;

  private Processor<Object, Object> compositeProcessor;

  @Before
  public void setUp() throws Exception {
    doThrow(new RuntimeException("test exception")).when(this.failingProcessor).process(any(), any());

    this.compositeProcessor = new CompositeProcessor.Builder<>()
        .process(this.processor1)
        .process(this.processor2)
        .build();
  }

  @Test
  public void testProcess() throws Exception {
    this.compositeProcessor.process(new Object(), new Object());
    verify(this.processor1).process(any(), any());
    verify(this.processor2).process(any(), any());
  }

  @Test
  public void testClose() throws Exception {
    this.compositeProcessor.close();
    verify(this.processor1).close();
    verify(this.processor2).close();
  }

  @Test
  public void testFailingProcess() throws Exception {
    this.compositeProcessor = new CompositeProcessor.Builder<>()
        .process(this.processor1)
        .process(this.failingProcessor)
        .process(this.processor2)
        .build();

    this.compositeProcessor.process(new Object(), new Object());

    verify(this.processor1).process(any(), any());
    verify(this.processor2).process(any(), any());
  }

  @Test
  public void shouldCopyDelegates() throws Exception {
    CompositeProcessor.Builder<Object, Object> builder = new CompositeProcessor.Builder<>();
    this.compositeProcessor = builder.process(this.processor1).build();
    builder.process(this.processor2).build();

    this.compositeProcessor.process(new Object(), new Object());

    verify(this.processor1).process(any(), any());
    verify(this.processor2, never()).process(any(), any());
  }
}