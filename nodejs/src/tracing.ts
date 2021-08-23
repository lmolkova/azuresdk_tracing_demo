import { NodeTracerProvider } from "@opentelemetry/node";
import { getNodeAutoInstrumentations } from "@opentelemetry/auto-instrumentations-node";
import {
  ConsoleSpanExporter,
  SimpleSpanProcessor
} from "@opentelemetry/tracing";
import { registerInstrumentations } from "@opentelemetry/instrumentation";
import { Resource } from "@opentelemetry/resources";
import { JaegerExporter } from "@opentelemetry/exporter-jaeger";
import { SemanticResourceAttributes } from "@opentelemetry/semantic-conventions";
import { getEnvironmentVariable } from "./utils";

export function configureTracing() {
  const provider = new NodeTracerProvider({
    resource: new Resource({
      [SemanticResourceAttributes.SERVICE_NAME]:
        getEnvironmentVariable("OTEL_SERVICE_NAME")
    })
  });
  provider.addSpanProcessor(new SimpleSpanProcessor(new JaegerExporter()));
  provider.addSpanProcessor(new SimpleSpanProcessor(new ConsoleSpanExporter()));
  provider.register();

  registerInstrumentations({
    instrumentations: [getNodeAutoInstrumentations()]
  });
}
