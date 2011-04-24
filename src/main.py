import sys

from google.appengine.dist import use_library
use_library('django', '1.2')

from google.appengine.ext.webapp import WSGIApplication
from google.appengine.ext.webapp.template import register_template_library
from google.appengine.ext.webapp.util import run_wsgi_app

from handlers import AccountHandler, AdminNewsHandler, AdminHandler, \
    BadgeHandler, BadgesHandler, BadgeApplicationHandler, CalendarHandler, \
    ContactHandler, EditHandler, FAQHandler, FileNotFoundHandler, \
    HackathonHandler, LoginHandler, ManualHandler, MemberHandler, \
    MembersHandler, MessagesHandler, NewsHandler, TalksHandler

register_template_library('templatetags.customtags')

application = WSGIApplication(
    (('/'                           , NewsHandler),
     ('/account'                    , AccountHandler),
     ('/admin/news'                 , AdminNewsHandler),
     ('/admin'                      , AdminHandler),
     ('/badges/'                    , BadgesHandler),
     ('/badges/([^/]+)'             , BadgeHandler),
     ('/badge_application'          , BadgeApplicationHandler),
     ('/calendar'                   , CalendarHandler),
     ('/contact'                    , ContactHandler),
     ('/edit/([^/]+)'               , EditHandler),
     ('/faq'                        , FAQHandler),
     ('/hack-a-thon'                , HackathonHandler),
     ('/login'                      , LoginHandler),
     ('/manual'                     , ManualHandler),
     ('/members/'                   , MembersHandler),
     ('/members/([^/]+)'            , MemberHandler),
     ('/messages/([^/]+)'           , MessagesHandler),
     ('/talks'                      , TalksHandler),
     ('/(.+)'                       , FileNotFoundHandler)),
    debug=True)

def main(argv=None):
    if argv is None:
        argv = sys.argv
    run_wsgi_app(application)
    return 0

if __name__ == '__main__':
    exit(main())
