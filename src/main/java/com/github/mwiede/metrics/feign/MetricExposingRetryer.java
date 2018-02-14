package com.github.mwiede.metrics.feign;

import java.lang.reflect.Method;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;

import feign.RetryableException;
import feign.Retryer;

/**
 * A {@link Retryer} which exposes a metric which shows the number of attempts being made during
 * invocation on the target. It can only be used together with {@link FeignOutboundMetricsDecorator}
 * because it takes the actual invoked method from its threadlocal.
 *
 */
public class MetricExposingRetryer extends Retryer.Default {

  private final MetricRegistry metricRegistry;

  class RetryerDecorator implements Retryer {

    private final Retryer delegate;

    RetryerDecorator(final Retryer delegate) {
      this.delegate = delegate;
    }

    @Override
    public void continueOrPropagate(final RetryableException e) {

      final Meter meter = getMetric(e);

      try {
        delegate.continueOrPropagate(e);
        meter.mark();
      } catch (final Exception ex) {
        throw ex;
      }

    }

    @Override
    public Retryer clone() {
      return this;
    }
  }

  public MetricExposingRetryer(final MetricRegistry metricRegistry) {
    this.metricRegistry = metricRegistry;
  }

  @Override
  public Retryer clone() {
    return new RetryerDecorator(super.clone());
  }

  private Meter getMetric(final RetryableException e) {
    final Method method = FeignOutboundMetricsDecorator.ACTUAL_METHOD.get();
    final String name =
        FeignOutboundMetricsDecorator.chooseName("", false, method, "re-attempts", "Metered");
    return metricRegistry.meter(name);
  }

}