package io.confluent.consumer.offsets.blacklist;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CompositeBlacklistTest {

  @Rule
  public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock
  private Blacklist<Object, Object> ignoreAllBlackList;

  @Mock
  private Blacklist<Object, Object> failingBlackList;

  private final Blacklist<Object, Object> ignoreNothingBlacklist = new IgnoreNothingBlacklist<>();
  private Blacklist<Object, Object> compositeBlacklist;

  @Before
  public void setUp() throws Exception {
    when(this.ignoreAllBlackList.shouldIgnore(any(), any())).thenReturn(true);
    doThrow(new RuntimeException("test exception")).when(this.failingBlackList).shouldIgnore(any(), any());

    this.compositeBlacklist = new CompositeBlacklist.Builder<>()
        .ignore(this.ignoreNothingBlacklist)
        .ignore(this.ignoreAllBlackList)
        .build();
  }

  @Test
  public void shouldIgnore() throws Exception {
    assertTrue(this.compositeBlacklist.shouldIgnore(new Object(), new Object()));
    verify(this.ignoreAllBlackList).shouldIgnore(any(), any());
  }

  @Test
  public void shouldIgnoreIfFail() throws Exception {
    this.compositeBlacklist = new CompositeBlacklist.Builder<>()
        .ignore(this.failingBlackList)
        .ignore(this.ignoreAllBlackList)
        .build();

    assertTrue(this.compositeBlacklist.shouldIgnore(new Object(), new Object()));
    verify(this.ignoreAllBlackList, never()).shouldIgnore(any(), any());
  }

  @Test
  public void shouldStopCheckingAfterTheFirstIgnore() throws Exception {
    this.compositeBlacklist = new CompositeBlacklist.Builder<>()
        .ignore(this.ignoreNothingBlacklist)
        .ignore(this.ignoreAllBlackList)
        .ignore(this.ignoreAllBlackList)
        .build();

    assertTrue(this.compositeBlacklist.shouldIgnore(new Object(), new Object()));
    verify(this.ignoreAllBlackList).shouldIgnore(any(), any());
  }

  @Test
  public void shouldCopyDelegates() throws Exception {
    CompositeBlacklist.Builder<Object, Object> builder = new CompositeBlacklist.Builder<>()
        .ignore(this.ignoreNothingBlacklist);
    Blacklist<Object, Object> blacklist = builder.build();
    builder.ignore(this.ignoreAllBlackList).build();
    assertFalse(blacklist.shouldIgnore(new Object(), new Object()));
  }
}