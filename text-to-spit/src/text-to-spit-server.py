import os
import sys

def abspath(path):
    return os.path.abspath(
        os.path.join(os.path.dirname(__file__), path))

_LIB_DIR = '../lib/'
_lib_path = abspath(_LIB_DIR)
if _lib_path not in sys.path:
    sys.path.append(_lib_path)
del _LIB_DIR, _lib_path

import cherrypy

class TextToSpit(object):
    @cherrypy.expose
    @cherrypy.tools.json_out()
    def index(self, message=None, number=None):
        print("Received message: %s from number %s" % (message, number))
        return [{'number' : number, 'message' : message}]
    index.exposed = True

cherrypy.config.update({'server.socket_host': '192.168.1.68'})
cherrypy.quickstart(TextToSpit())
