// configure tracing before any other imports to allow them to patch other imports
import { configureTracing } from "./tracing";
configureTracing();
import express from "express";
import { initializeEventHub } from "./eventhub";
import { getEnvironmentVariable } from "./utils";

const PORT = getEnvironmentVariable("SERVER_PORT");

const app = express();
app.use(express.json());

const { producer } = initializeEventHub();

app.post("/message", async (req, res) => {
  await producer.sendBatch([{ body: req.body }]);
  res.json({ message: "Successfully sent a message to Event Hubs" });
});

app.listen(PORT, async () => {
  console.log(`Express app listening at http://localhost:${PORT}`);
});
