import datetime

from django.utils import simplejson as json

from google.appengine.ext import db, webapp
from google.appengine.ext.webapp.util import run_wsgi_app
from google.appengine.ext.admin import ImageHandler

class DictableModel(db.Model):
    def dump(self):
        values = {}
        for name, prop in self.properties().iteritems():
            value = prop.__get__(self, self.__class__)
            if isinstance(value, datetime.datetime):
                value = str(value)
            values[name] = value
        return values


class JsonableModel(DictableModel):
    def dump(self):
        return json.dumps(super(JsonableModel, self).dump())


class Member(JsonableModel):
    given_name = db.StringProperty()
    surname = db.StringProperty()
    email = db.EmailProperty()
    student_id = db.StringProperty(required=True)
    created = db.DateTimeProperty(auto_now_add=True)
    modified = db.DateTimeProperty(auto_now=True)
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
            member = Member.get_or_insert(student_id, student_id=student_id)
            member.signature = db.Blob(signature)
            member.put()
            self.response.headers['Content-Type'] = 'text/plain'
            self.response.out.write(str(member.key()))
        else:
            self.error(400)


class SwipeHandler(webapp.RequestHandler):
    def post(self):
        student_id = self.request.get('student_id')
        if student_id:
            member = Member.get_or_insert(student_id, student_id=student_id)
            member.given_name = self.request.get('given_name')
            member.surname = self.request.get('surname')
            member.email = self.request.get('email')
            member.student_id = self.request.get('student_id')
            member.key_name = self.request.get('student_id')
            member.put()
            self.response.out.write(member.dump())
        else:
            self.error(400)

    def get(self):
        members = Member.all()
        for m in members:
            self.response.out.write('<b>%s</b><br/>' % str(m.given_name))


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
     ('^/members/(\d+)?$', MemberHandler),
     ('^/swipe/$', SwipeHandler)),
    debug=True)

def main():
    run_wsgi_app(application)

if __name__ == '__main__':
    main()
