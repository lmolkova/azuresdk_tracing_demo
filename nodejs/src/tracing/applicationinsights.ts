import { getEnvironmentVariable } from "../utils";
import { setup as setupAppInsights } from "applicationinsights";

export function configure() {
  const instrumentationKey = getEnvironmentVariable(
    "APPINSIGHTS_INSTRUMENTATIONKEY"
  );

  setupAppInsights(instrumentationKey)
    .setAutoCollectConsole(true)
    .setAutoCollectPerformance(true)
    .setAutoCollectRequests(true)
    .setAutoCollectExceptions(true)
    .setAutoCollectDependencies(true)
    .setAutoCollectPerformance(true)
    .setUseDiskRetryCaching(true)
    .start();
}
