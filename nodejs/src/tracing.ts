import { NodeTracerProvider } from "@opentelemetry/node";
import { getNodeAutoInstrumentations } from "@opentelemetry/auto-instrumentations-node";
import { SimpleSpanProcessor } from "@opentelemetry/tracing";
import { registerInstrumentations } from "@opentelemetry/instrumentation";
import { Resource } from "@opentelemetry/resources";
import { JaegerExporter } from "@opentelemetry/exporter-jaeger";

export function configureTracing() {
  const provider = new NodeTracerProvider({
    resource: new Resource({
      "service.name": "nodejs-tracing-sample"
    })
  });
  provider.addSpanProcessor(new SimpleSpanProcessor(new JaegerExporter()));
  provider.register();

  registerInstrumentations({
    instrumentations: [getNodeAutoInstrumentations()]
  });
  console.log(getNodeAutoInstrumentations());
}
