import express from "express";
import { initializeEventHub } from "./eventhub";

const PORT = process.env.PORT || 5000;

const app = express();
app.use(express.json());

app.listen(PORT, async () => {
  const { consumer, producer } = await initializeEventHub();

  app.post("message", async (req, res) => {
    await producer.sendBatch([{body: new Date().toString()}]);
  })
  console.log(`Express app listening at http://localhost:${PORT}`);
});
