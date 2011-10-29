from pprint import pprint
from twisted.web.server import Site
from twisted.web.resource import Resource
from twisted.web.static import File
from twisted.internet import reactor

class LiftResource(Resource):
    isLeaf =True
    
    def render_POST(self, request):
        pprint(request.__dict__)
        return 'thanks'

def main():
    root = File('static')
    root.putChild('lift', LiftResource())
    reactor.listenTCP(8080, Site(root))
    reactor.run()

print 'rofl'
if __name__ == '__main__':
    main()
