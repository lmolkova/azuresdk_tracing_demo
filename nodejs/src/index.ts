import { context } from "@opentelemetry/api";
import express from "express";
import { initializeEventHub } from "./eventhub";
import { configureTracing } from "./tracing";

const PORT = process.env.PORT || 5000;

const app = express();
app.use(express.json());

configureTracing();

app.listen(PORT, async () => {
  const { consumer, producer } = await initializeEventHub();

  app.post("/message", async (req, res) => {
    await context.with(context.active(), async () => {
      console.log(req);
      producer.sendBatch([{ body: new Date().toString() }]);
      res.json({ message: "Sent" });
    });
  });
  console.log(`Express app listening at http://localhost:${PORT}`);
});
