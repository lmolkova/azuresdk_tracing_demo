import { getEnvironmentVariable } from "../utils";

export function configureTracing() {
  if (getEnvironmentVariable("TELEMETRY_DESTINATION") === "jaeger") {
    require("./opentelemetry").configure();
  } else {
    require("./applicationinsights").configure();
  }
}
