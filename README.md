# metrics-feign
A decorator wrapping [Feign](https://github.com/OpenFeign/feign) client method handlers in order to provide [Dropwizard Metrics](http://metrics.dropwizard.io) of calls to feign target interfaces.

## Usage


``` java

    @Timed
    @Metered
    @ExceptionMetered
    interface GitHub {
        @RequestLine("GET /repos/{owner}/{repo}/contributors")
        List<Contributor> contributors(@Param("owner") String owner, @Param("repo") String repo);
    }

    static class Contributor {
        String login;
        int contributions;
    }

    public static void main(String... args) {

        MetricRegistry metricRegistry = new MetricRegistry();

        final ConsoleReporter reporter = ConsoleReporter.forRegistry(metricRegistry)
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        
        GitHub github = Feign.builder().invocationHandlerFactory(
                new FeignOutboundMetricsDecorator(new InvocationHandlerFactory.Default(), metricRegistry))
                .decoder(new GsonDecoder()).target(GitHub.class, "https://api.github.com");

        // Fetch and print a list of the contributors to this library.
        List<Contributor> contributors = github.contributors("mwiede", "metrics-feign");
        for (Contributor contributor : contributors) {
            System.out.println(contributor.login + " (" + contributor.contributions + ")");
        }

        reporter.report();
    }



```


