import express from "express";

const PORT = process.env.PORT || 5000;

if (!PORT) {
  throw new Error("Missing PORT env var"); // should we default it?
}

const app = express();
app.use(express.json());

app.listen(PORT, () => {
  console.log(`Express app listening at http://localhost:${PORT}`);
});
