#!/usr/bin/env python
from __future__ import print_function
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
import argparse
import os
import subprocess
import sys

import markdown

def resolve_path(origin_path, relative_path):
    return os.path.join(os.path.dirname(origin_path), relative_path)

with open(resolve_path(__file__, 'template.html')) as template_file:
    TEMPLATE = template_file.read()

TEMPLATE_FACEBOOK_EVENT = '''
 | <a href="http://www.facebook.com/event.php?eid={event_id}" target="_blank" style="color: #336699; font-weight: normal; text-decoration: underline;">Facebook Event</a>
'''

TEMPLATE_IMAGE = '''
<img src="http://man-up.appspot.com/img/{image_name}" style="max-width: 560px; border: none; font-size: 14px; font-weight: bold; height: auto; line-height: 100%; outline: none; text-decoration: none; text-transform: capitalize; display: inline;">
'''

TEMPLATE_ONLINE_VERSION = '''
<td valign="top" width="190">
    <div style="color: #505050; font-family: Arial; font-size: 10px; line-height: 100%; text-align: left;">
        Not displaying correctly?<br>
        <a href="http://man-up.appspot.com/messages/{index}" target="_blank"   style="color: #336699; font-weight: normal; text-decoration: underline;">View browser</a>.
    </div>
</td>
'''

def main(argv=None):
    if argv is None:
        argv = sys.argv
    ap = argparse.ArgumentParser()
    ap.add_argument('-b', '--body', required=True)
    ap.add_argument('-c', '--computer', default='eng007')
    ap.add_argument('-f', '--facebook-event', type=int)
    ap.add_argument('-i', '--image-name')
    ap.add_argument('-l', '--image-link')
    ap.add_argument('-o', '--online-index', type=int)
    ap.add_argument('-s', '--subject', required=True)
    ap.add_argument('-t', '--to', required=True)
    ap.add_argument('-u', '--username', required=True)
    args = ap.parse_args(args=argv[1:])

    msg = MIMEMultipart('alternative')
    msg['Subject'] = args.subject
    msg['From'] = '%s@cs.man.ac.uk' % args.username
    msg['To'] = args.to


    with open(args.body) as body_file:
        body_plain = body_file.read()

    msg.attach(MIMEText(body_plain))

    if args.facebook_event:
        facebook_event = TEMPLATE_FACEBOOK_EVENT.format(
            event_id=args.facebook_event)
    else:
        facebook_event = ''

    if args.image_name:
        image = TEMPLATE_IMAGE.format(image_name=args.image_name)
        if args.image_link:
            image = '<a href="%s" target="_blank">%s</a>' % (args.image_link,
                image)
    else:
        image = ''


    if args.online_index is not None:
        online_version = TEMPLATE_ONLINE_VERSION.format(
            index=args.online_index)
    else:
        online_version = ''

    html = TEMPLATE.format(
        body=markdown.markdown(body_plain),
        image=image,
        facebook_event=facebook_event,
        online_version=online_version)

    msg.attach(MIMEText(html, 'html'))

    with open('/tmp/msg', 'w') as msg_file:
        msg_file.write(msg.as_string())

    subprocess.check_call('scp /tmp/msg %s@%s.cs.man.ac.uk:~/msg'
        % (args.username, args.computer), shell=True,
        stdout=open(os.devnull, 'w'))

    subprocess.check_call(
        "ssh %s@%s.cs.man.ac.uk '/usr/sbin/sendmail -v %s < ~/msg'"
        % (args.username, args.computer, args.to), shell=True,
        stdout=open(os.devnull, 'w'))

    return 0

if __name__ == '__main__':
    exit(main())

