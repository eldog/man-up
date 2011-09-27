import datetime
import os
import urllib

from google.appengine.api import users, datastore_errors
from google.appengine.api.mail import send_mail
from google.appengine.ext.webapp import RequestHandler, template
from google.appengine.ext.db import Key, BadKeyError

import utils
from models import Award, Badge, Member, NewsArticle, Talk, \
                   GeneralSiteProperties

get_path = utils.path_getter(__file__)

class BaseHandler(RequestHandler):

    login_required = False
    title = None

    def render_template(self, template_name, template_dict=None):
        try:
            tag_line = GeneralSiteProperties.get_properties().tag_line
        except:
            tag_line = 'Next meeting soon!'

        if template_dict is None:
            template_dict = {}

        user = Member.get_current_member()

        if user:
            if self.login_required:
                redirect_target = '/'
            else:
                redirect_target = self.request.path
            url_creator = users.create_logout_url
        else:
            redirect_target = '/login?url=%s' % self.request.path
            url_creator = users.create_login_url

        defaults = {
            'user': user,
            'is_admin': users.is_current_user_admin(),
            'log_url': url_creator(redirect_target),
            'tag_line': tag_line,
            'title': self.title
        }

        for key in defaults:
            if key not in template_dict:
                template_dict[key] = defaults[key]

        template_path = get_path(
            os.path.join('templates', '%s.html' % template_name)
        )
        self.response.out.write(template.render(template_path, template_dict))


class AccountHandler(BaseHandler):

    login_required = True

    title = 'Account'

    valid_letters = (
        'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789'
    )

    banned_names = {
        u'neo': "Fat chance are you Neo. If you are, I'm not gonna get my hopes up",
    }

    def post(self):
        if len(self.request.POST) == 4 and 'handle' in self.request.POST \
                and 'real_name' in self.request.POST \
                and 'email' in self.request.POST \
                and 'bio' in self.request.POST:

            handle = self.request.POST.getall('handle')[0]
            template_dict = {}
            member = Member.get_current_member()
            other = Member.gql('WHERE handle = :1', handle).get()

            if (not handle or len(handle) > 12 or
                any(l not in self.valid_letters for l in handle)):
                template_dict['error'] = 'Pick something sensible, you moron.'

            elif other and other.user_id != member.user_id:
                template_dict['error'] = 'Sorry, already taken.'

            elif handle.lower() in self.banned_names:
                template_dict['error'] = self.banned_names[handle]

            else:
                real_name = self.request.POST.getall('real_name')[0]
                if real_name:
                    member.real_name = real_name
                email = self.request.POST.getall('email')[0]
                if email:
                    member.email = email
                bio = self.request.POST.getall('bio')[0]
                if bio:
                    member.bio = bio
                member.handle = handle
                member.save()
                template_dict['error'] = 'Profile updated'
            self.render_template('account', template_dict)

    def get(self):
        self.render_template('account')


class AdminHandler(BaseHandler):

    login_required = True

    def post(self):
        post = self.request.POST
        if post['kind'] == 'badge':
            badge = Badge(
                name=post['name'],
                description=post['description'],
                category=post['category'],
                image=post['image'],
                value=int(post['value'])
            )
            badge.save()
        elif post['kind'] == 'award':
            badge = Badge.get_by_id(int(post['badge']))
            for member in post.getall('members'):
                member = Member.get_by_id(int(member))
                award = Award(
                    member=member,
                    badge=badge,
                    date=datetime.date.today(),
                    proof=post['proof']
                )
                award.save()
                member.score += badge.value
                member.save()
        elif post['kind'] == 'talk':
            talk = Talk(
                title=post['title'],
                date=utils.parse_date(post['date']),
                description=post['description'],
                member=Member.get_by_id(int(post['member'])),
                video=post['video']
            )
            talk.put()
        elif post['kind'] == 'taglineform':
            properties = GeneralSiteProperties.all().get()
            if properties == None:
                properties = GeneralSiteProperties(tag_line=post['tagline'])
                properties.put()
            else:
                properties.tag_line = post['tagline']
                properties.put()
        self.get()

    def get(self):
        self.render_template('admin', {
            'badges': Badge.all(),
            'members': Member.all(),
        })


class AdminNewsHandler(BaseHandler):
    def get(self):
        self.render_template('admin_news',
            {'news_list' : NewsArticle.all().order('-date')})

    def post(self):
        articles_deleted = 0
        for article_key in self.request.POST.getall('delete_article'):
            try:
                article_key = Key(article_key)
            except BadKeyError:
                # Wrong syntax for a key, move on to next key.
                continue
            article = NewsArticle.get(article_key)
            if article:
                article.delete()
                articles_deleted += 1
            # Else, not article has this key.

        self.render_template('admin_news',
            {'news_list': NewsArticle.all().order('-date'),
             'delete_successful': '%d article(s) deleted.' % articles_deleted})


class BadgeHandler(BaseHandler):

    def get(self, id):
        self.render_template('badge', {
            'badge': Badge.get_by_id(int(id))
        })


class BadgeApplicationHandler(BaseHandler):

    login_required = True

    def post(self):
        post = self.request.POST
        if len(post) == 2 and 'badge' in post and 'proof' in post:
            body = 'Member: %s\nBadge: %s\nProof:\n%s' % (
                Member.get_current_member().handle,
                post['badge'],
                post['proof']
            )
            send_mail(
                sender='petersutton2009@gmail.com',
                to='petersutton2009@gmail.com',
                subject='Badge application',
                body=body
            )
            self.render_template('badge_application', {
                'message': 'Application submitted. \
                            It will be reviewed as soon as possible.'
            })

    def get(self):
        selected_badge = self.request.GET.getall('badge')
        if selected_badge:
            selected_badge = selected_badge[0]
        else:
            selected_badge = None
        badges = Badge.gql('ORDER BY name')
        self.render_template('badge_application', {
            'badges': badges,
            'selected_badge': selected_badge
        })


class BadgesHandler(BaseHandler):

    title = 'Badges'

    def get(self):
        order = self.request.GET.get('order', 'value')
        if order == 'receivers':
            badges = list(Badge.all())
            badges.sort(key=lambda i:i.awards.count())
        else:
            badges = Badge.gql('ORDER BY ' + order)
        self.render_template('badges', {
            'badges': badges
        })


class CalendarHandler(BaseHandler):

    def get(self):
        self.render_template('calendar')


class ContactHandler(BaseHandler):

    def get(self):
        self.render_template('contact')


class EditHandler(BaseHandler):

    def get(self, key):
        template_dict = {'key': key, 'show_form' : True}
        if key == 'new':
            template_dict['form_data'] = {
                'title': '',
                'author': Member.get_current_member().handle,
                'date': datetime.date.today(),
                'body': ''}
        else:
            try:
                template_dict['form_data'] = NewsArticle.get(Key(key))
            except BadKeyError:
                template_dict['message'] = \
                    'Could not find article with key %r.' % key
                template_dict['show_form'] = False
        self.render_template('edit', template_dict)

    def post(self, key):
        post = self.request.POST
        form_data = dict((k, post.get(k, ''))
                          for k in ('title', 'author', 'date', 'body'))
        template_dict = {'form_data': form_data, 'key': key, 'show_form' : True}
        if 'delete_article' in post:
            try:
                NewsArticle.get(Key(key)).delete()
            except datastore_errors.Error:
                template_dict['message'] = \
                    'Could not delete article with key %r.' % key
            else:
                template_dict['message'] = 'Article deleted.'
                template_dict['show_form'] = False
        else:
            try:
                date = utils.parse_date(form_data['date'])
            except ValueError:
                template_dict['message'] = \
                    'Date is not in the correct format (YYYY-MM-DD).'
            else:
                if key == 'new':
                    try:
                        article = NewsArticle(title=form_data['title'],
                                              author=form_data['author'],
                                              date=date,
                                              body=form_data['body'])
                        article.put()
                    except datastore_errors.Error:
                        template_dict['message'] = \
                            'Could not create new article.'
                    else:
                        template_dict['message'] = 'Article created.'
                        template_dict['show_form'] = False
                else:
                    try:
                        article = NewsArticle.get(Key(key))
                    except BadKeyError:
                        template_dict['message'] = \
                            'Could not find article with key %r.' % key
                    else:
                        article.title = form_data['title']
                        article.author = form_data['author']
                        article.date = date
                        article.body = form_data['body']
                        try:
                            article.put()
                        except datastore_errors.Error:
                            template_dict['message'] = \
                                'Could not save changes to article.'
                        else:
                            template_dict['form_data'] = article
                            template_dict['message'] = 'Changes saved.'
        self.render_template('edit', template_dict)


class FAQHandler(BaseHandler):

    def get(self):
        self.render_template('faq')


class FileNotFoundHandler(BaseHandler):
    def get(self, url=None):
        self.render_template('404', {'url': url})

class GuideHandler(BaseHandler):
    def get(self):
        self.render_template('guide')

class HackathonHandler(BaseHandler):

    def get(self):
        self.render_template('hack-a-thon')

# This handler is a hack to force people to select handles.
class LoginHandler(BaseHandler):

    def get(self):
        if 'url' in self.request.GET:
            member = Member.get_current_member()
            if member.handle.isdigit() and len(member.handle) == 21:
                self.redirect('/account')
            else:
                self.redirect(self.request.GET.getall('url')[0])
        else:
            self.redirect('/')


class ManualHandler(BaseHandler):

    def get(self):
        self.render_template('manual')


class MembersHandler(BaseHandler):

    def get(self):
        members = list(Member.all())
        if members:
            members.sort(key=lambda member:(member.score, member.handle))
            rank = 0
            ranked_members = [(rank, members[-1])]
            for i in range(len(members) - 2, -1, -1):
                if members[i + 1].score != members[i].score:
                    rank += 1
                ranked_members.append((rank, members[i]))
        else:
            ranked_members = []

        self.render_template('members', {
            'members': ranked_members
        })


class MemberHandler(BaseHandler):

    def get(self, handle):
        query = Member.gql('WHERE handle = :1', urllib.unquote(handle))
        member = iter(query).next() if query.count() else None
        self.render_template('member', {
            'member': member
        })


class MessagesHandler(BaseHandler):

    def get(self, message_index):
        message_file = None
        try:
            message_file = open('static/messages/%s.html' % message_index)
            self.response.out.write(message_file.read())
        except:
            self.render_template('404',
                {'url': 'message number %s' % message_index})
        finally:
            if message_file is not None:
                message_file.close()


class PaginationHandler(BaseHandler):
    DEF_ERROR_MESSAGE = "That page doesn't exist, why not look at this."
    ITEM_PER_PAGE = 5
    _model = None
    _template = None

    def get(self):
        try:
            page_num = int(self.request.GET.get('page', 0))
        except ValueError:
            page_num = 0
            message = self._DEF_ERROR_MESSAGE
        else:
            message = None

        items = self._model.all().order('-date');

        last_page = items.count() // self.ITEM_PER_PAGE

        if page_num > last_page:
            page_num = last_page
            message = self._DEF_ERROR_MESSAGE
        elif page_num < 0:
            page_num = 0
            message = self._DEF_ERROR_MESSAGE

        pagination_dict = {'num': page_num,
                           'prev': page_num - 1,
                           'next': page_num + 1,
                           'hasNext': page_num != last_page,
                           'hasPrev': page_num != 0}

        first_page_item = page_num * self.ITEM_PER_PAGE
        last_page_item = (page_num + 1) * self.ITEM_PER_PAGE

        self.render_template(self._template,
            {'content_list': items.fetch(last_page_item, first_page_item),
             'message': message,
             'pagedata': pagination_dict})


class NewsHandler(PaginationHandler):
    _model = NewsArticle
    _template = 'news'


class TalksHandler(PaginationHandler):
    _model = Talk
    _template = 'talks'
