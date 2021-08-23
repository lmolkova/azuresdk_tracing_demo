import { SpanStatusCode } from "@opentelemetry/api";
import express from "express";
import { initializeEventHub } from "./eventhub";
import { configureTracing } from "./tracing";

const PORT = process.env.PORT || 5000;

const app = express();
app.use(express.json());

configureTracing();

// TODO: what HTTP endpoints should this service accept?
const { producer } = initializeEventHub();

app.post("/message", async (req, res) => {
  await producer.sendBatch([{ body: req.body }]);
  res.json({ message: "Sent" });
});

app.listen(PORT, async () => {
  console.log(`Express app listening at http://localhost:${PORT}`);
});
