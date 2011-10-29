from multiprocessing import Pipe
from pprint import pprint
from twisted.web.server import Site
from twisted.web.resource import Resource
from twisted.web.static import File
from twisted.internet import reactor

class LiftResource(Resource):
    isLeaf =True
    
    def __init__(self, pipe):
        self.pipe = pipe

    def render_POST(self, request):
        self.pipe.send('button pressed')
        print 'thanks'

def main(pipe):
    root = File('static')
    root.putChild('lift', LiftResource(pipe))
    reactor.listenTCP(8080, Site(root))
    reactor.run()

class DummyPipe(object):
    def send(*args, **kwargs):
        pass

print 'rofl'
if __name__ == '__main__':
    main(DummyPipe())
