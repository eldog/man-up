from google.appengine.ext import db, webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext.admin import ImageHandler

class Member(db.Model):
    student_id = db.StringProperty(required=True)
    signature = db.BlobProperty()


class MemberHandler(webapp.RequestHandler):
    def get(self, student_id):
        for member in Member.all():
            self.response.out.write(
                '<p>%s</p><img src="/images/%s"/><br/>'
                % (member.student_id, member.key()))

    def post(self, student_id):
        signature = self.request.get('signature')
        if signature:
            member = Member(student_id=student_id)
            member.signature = db.Blob(signature)
            member.put()
            self.response.headers['Content-Type'] = 'text/plain'
            self.response.out.write(str(member.key()))
        else:
            self.error(400)


class ImageHandler(webapp.RequestHandler):
    def get(self, member_key):
        if member_key:
            try:
                member = db.get(member_key)
            except db.BadKeyError:
                self.error(400)
            else:
                if member:
                    self.response.headers['Content-Type'] = 'image/png'
                    self.response.out.write(member.signature)
                else:
                    self.error(404)
        else:
            self.error(400)

application = webapp.WSGIApplication(
    (('^/images/([^/]+)$', ImageHandler),
     ('^/members/(\d+)?$', MemberHandler)),
    debug=True)

def main():
    run_wsgi_app(application)

if __name__ == '__main__':
    main()
