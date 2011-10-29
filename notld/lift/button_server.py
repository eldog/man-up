#!/usr/bin/env python2.7

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
        # save the ipv4 hostname in the post request
        host = request.host.host    
        print 'send msg %s' % host 
        self.pipe.send(host)
        return ''

def main(pipe):
    root = File('static')
    root.putChild('lift', LiftResource(pipe))
    reactor.listenTCP(8080, Site(root))
    reactor.run()

class DummyPipe(object):
    def send(*args, **kwargs):
        pass

if __name__ == '__main__':
    main(DummyPipe())
