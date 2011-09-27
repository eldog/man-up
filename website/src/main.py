import sys

from google.appengine.dist import use_library
use_library('django', '1.2')

from google.appengine.ext.webapp import WSGIApplication
from google.appengine.ext.webapp.template import register_template_library
from google.appengine.ext.webapp.util import run_wsgi_app

from handlers import AccountHandler, AdminNewsHandler, AdminHandler, \
    BadgeHandler, BadgesHandler, BadgeApplicationHandler, CalendarHandler, \
    ContactHandler, EditHandler, FAQHandler, FileNotFoundHandler, \
    GuideHandler, HackathonHandler, LoginHandler, ManualHandler, \
    MemberHandler, MembersHandler, MessagesHandler, NewsHandler, TalksHandler

register_template_library('templatetags.customtags')

application = WSGIApplication(
    ((r'^/$'                   , NewsHandler),
     (r'^/news$'               , NewsHandler),
     (r'^/account$'            , AccountHandler),
     (r'^/admin$'              , AdminHandler),
     (r'^/admin/news$'         , AdminNewsHandler),
     (r'^/admin/news/([^/]+)$' , EditHandler),
     (r'^/badges/$'            , BadgesHandler),
     (r'^/badges/([^/]+)$'     , BadgeHandler),
     (r'^/badge_application$'  , BadgeApplicationHandler),
     (r'^/calendar$'           , CalendarHandler),
     (r'^/contact$'            , ContactHandler),
     (r'^/faq$'                , FAQHandler),
     ('^/guide$'               , GuideHandler),
     (r'^/hack-a-thon$'        , HackathonHandler),
     (r'^/login$'              , LoginHandler),
     (r'^/manual$'             , ManualHandler),
     (r'^/members/$'           , MembersHandler),
     (r'^/members/([^/]+)$'    , MemberHandler),
     (r'^/messages/(\d+)$'     , MessagesHandler),
     (r'^/talks$'              , TalksHandler),
     (r'(.*)'                  , FileNotFoundHandler)),
     debug=True)

def main(argv=None):
    if argv is None:
        argv = sys.argv
    run_wsgi_app(application)
    return 0

if __name__ == '__main__':
    exit(main())
