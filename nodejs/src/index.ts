import { SpanStatusCode } from "@opentelemetry/api";
import express from "express";
import { initializeEventHub } from "./eventhub";
import { configureTracing, getTracer } from "./tracing";

const PORT = process.env.PORT || 5000;

const app = express();
app.use(express.json());

configureTracing();

// TODO: what HTTP endpoints should this service accept?
const { producer } = initializeEventHub();

app.post("/message", async (req, res) => {
  const tracer = getTracer();
  const span = tracer.startSpan("send-message");
  try {
    await producer.sendBatch([{ body: req.body }]);
    res.json({ message: "Sent" });
    span.setStatus({ code: SpanStatusCode.OK });
  } catch (err) {
    span.recordException(err);
    span.setStatus({ code: SpanStatusCode.ERROR, message: err.message });
    throw err;
  } finally {
    span.end();
  }
});

app.listen(PORT, async () => {
  console.log(`Express app listening at http://localhost:${PORT}`);
});
