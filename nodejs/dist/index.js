"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const tslib_1 = require("tslib");
const express_1 = tslib_1.__importDefault(require("express"));
const PORT = process.env.PORT;
if (!PORT) {
    throw new Error("Missing PORT env var"); // should we default it?
}
const app = express_1.default();
app.use(express_1.default.json());
app.listen(PORT, () => {
    console.log(`Express app listening at http://localhost:${PORT}`);
});
