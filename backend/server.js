// Bunny Client badge backend. No dependencies: `node server.js`
// Clients POST /heartbeat {"uuid": "..."} every ~20s; GET /users returns UUIDs seen in the last 60s.
const http = require("http");

const PORT = process.env.PORT || 8080;
const TTL_MS = 60_000;
const UUID_RE = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;
const seen = new Map(); // uuid -> lastSeen

setInterval(() => {
  const now = Date.now();
  for (const [id, t] of seen) if (now - t > TTL_MS) seen.delete(id);
}, 15_000);

http.createServer((req, res) => {
  res.setHeader("Content-Type", "application/json");

  if (req.method === "POST" && req.url === "/heartbeat") {
    let body = "";
    req.on("data", (c) => { body += c; if (body.length > 1024) req.destroy(); });
    req.on("end", () => {
      try {
        const { uuid } = JSON.parse(body);
        if (typeof uuid === "string" && UUID_RE.test(uuid)) {
          if (!seen.has(uuid.toLowerCase())) console.log("New player online:", uuid);
          seen.set(uuid.toLowerCase(), Date.now());
        } else {
          console.log("Rejected heartbeat:", body.slice(0, 100));
        }
        res.end('{"ok":true}');
      } catch {
        res.statusCode = 400;
        res.end('{"ok":false}');
      }
    });
    return;
  }

  if (req.method === "GET" && req.url === "/users") {
    res.end(JSON.stringify([...seen.keys()]));
    return;
  }

  res.statusCode = 404;
  res.end("{}");
}).listen(PORT, () => console.log(`Bunny backend on :${PORT}`));
