import http.server
import socketserver
from http_handler import RestHttpHandler
import sys

PORT = 8000
print(sys.argv)
if len(sys.argv) > 1:
	PORT = int(sys.argv[1])

with socketserver.TCPServer(("", PORT), RestHttpHandler) as httpd:
    print("serving at port", PORT)
    httpd.serve_forever()