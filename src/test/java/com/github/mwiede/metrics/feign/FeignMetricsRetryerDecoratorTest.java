package com.github.mwiede.metrics.feign;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import feign.RetryableException;
import feign.Retryer;

public class FeignMetricsRetryerDecoratorTest {

  MetricRegistry metricRegistry;

  @Before
  public void init() {
    metricRegistry = new MetricRegistry();
  }

  @Test
  public void continueOrPropagate() {

    FeignMetricsInvocationHandlerFactoryDecorator.ACTUAL_METHOD.set(this.getClass().getDeclaredMethods()[0]);

    new FeignMetricsRetryerDecorator(new Retryer.Default(), metricRegistry).clone().continueOrPropagate(
        new RetryableException("message", new Date()));

    assertEquals("wrong number of meter metrics.", 1, metricRegistry.getMeters().values().size());

    final Set<Map.Entry<String, Meter>> entries = metricRegistry.getMeters().entrySet();

    entries.forEach(entry -> {
      assertEquals(String.format("wrong number of invocations in metric %s", entry.getKey()), 1,
          entry.getValue().getCount());
    });
  }


  @Test
  public void testClone() {
    final FeignMetricsRetryerDecorator feignMetricsRetryerDecorator =
        new FeignMetricsRetryerDecorator(new Retryer.Default(), metricRegistry);
    final Retryer clone = feignMetricsRetryerDecorator.clone();
    assertNotEquals(feignMetricsRetryerDecorator, clone);
  }

}
