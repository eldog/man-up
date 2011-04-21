import hashlib
import urllib

from google.appengine.ext.webapp.template import create_template_register

_GRAVATAR_HTML_TEMPLATE = (
    '<a href="http://www.gravatar.com/%s">'
        '<img src="http://www.gravatar.com/avatar/%s?%s" '
             'alt="gravatar"/>'
    '</a>')

def gravatar(email, size=80, rating='g', default_image='monsterid'):
    '''{% gravatar foo@bar.com %}'''
    if size < 1 or size > 512:
        raise ValueError('Size must be between 1 and 512 inclusive, got %r'
            % size)
    if rating not in ('g', 'pg', 'r', 'x'):
        raise ValueError("rating must be one of 'g', 'pg', 'r' or 'x', got %r"
            % rating)
    # Gravatar supports a '404' default options. However, we're not supporting
    # it so that the site still looks nice even when things are broken.
    if default_image not in ('mm', 'identicon', 'wavatar', 'monsterid',
                             'retro'):
        raise ValueError('default_image is not valid, got %r' % default_image)
    email_hash = hashlib.md5(email.strip().lower()).hexdigest()
    return _GRAVATAR_HTML_TEMPLATE % (
        email_hash, email_hash,
        urllib.urlencode({'s': size, 'r': rating, 'd': default_image}))

register = create_template_register()
register.simple_tag(gravatar)
