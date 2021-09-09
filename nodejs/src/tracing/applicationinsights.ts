import { setup as setupAppInsights } from "applicationinsights";

export function configure() {
  // Application Insights will automatically read the instrumentation
  // key from the APPINSIGHTS_INSTRUMENTATIONKEY environment variable.
  setupAppInsights()
    .setAutoDependencyCorrelation(true)
    .setAutoCollectConsole(true)
    .setAutoCollectPerformance(true)
    .setAutoCollectRequests(true)
    .setAutoCollectExceptions(true)
    .setAutoCollectDependencies(true)
    .setAutoCollectPerformance(true)
    .setUseDiskRetryCaching(true)
    .start();
}
