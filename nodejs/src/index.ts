// configure tracing before any other imports to allow them to patch other imports
import { configureTracing } from "./tracing";
configureTracing();
import express from "express";
import { initializeEventHub } from "./eventhub";

const PORT = process.env.PORT || 5000;

const app = express();
app.use(express.json());

// TODO: what HTTP endpoints should this service accept?
const { producer } = initializeEventHub();

app.post("/message", async (req, res) => {
  await producer.sendBatch([{ body: req.body }]);
  res.json({ message: "Sent" });
});

app.listen(PORT, async () => {
  console.log(`Express app listening at http://localhost:${PORT}`);
});
