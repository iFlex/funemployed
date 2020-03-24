import http.server
import socketserver
from http_handler import RestHttpHandler

PORT = 8000

with socketserver.TCPServer(("", PORT), RestHttpHandler) as httpd:
    print("serving at port", PORT)
    httpd.serve_forever()