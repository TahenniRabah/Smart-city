from http.server import BaseHTTPRequestHandler, HTTPServer
from pathlib import Path
from datetime import datetime, timezone

EVIDENCE = Path('/evidence/alertmanager-events.jsonl')

class Handler(BaseHTTPRequestHandler):
    def do_POST(self):
        length = int(self.headers.get('Content-Length', '0'))
        body = self.rfile.read(length)
        EVIDENCE.parent.mkdir(parents=True, exist_ok=True)
        with EVIDENCE.open('ab') as stream:
            timestamp = datetime.now(timezone.utc).isoformat().encode()
            stream.write(b'{"receivedAt":"' + timestamp + b'","payload":' + body + b'}\n')
        self.send_response(204)
        self.end_headers()

    def log_message(self, fmt, *args):
        print('%s - %s' % (self.address_string(), fmt % args), flush=True)

HTTPServer(('0.0.0.0', 5001), Handler).serve_forever()
