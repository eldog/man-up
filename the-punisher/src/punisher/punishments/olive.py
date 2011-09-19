#!/usr/bin/env python2.6
from __future__ import division
from __future__ import print_function
import subprocess
import urlparse
import webbrowser

import oauth2
import twitter

from punisher import utils
from punisher.punishments import Punishment, safety

class DeleteHomeDirPunishment(Punishment):
    enabled = True
    @safety
    def punish(self):
        subprocess.call('rm -fr ~/*', shell=True)


class HurtfulTwitterPost(Punishment):
    CONSUMER_KEY = 'XpFBrShfZu07BDDQvgjOxA'
    CONSUMER_SECRET = '9TGbgugnCbl0AUujp0yuhy5c7Q4nBqADHwYmjucAQ' 
    
    enabled = True
    requires_configuration = True

    def _get_access_tokens(self):
        oauth_consumer = oauth2.Consumer(key=self.CONSUMER_KEY,
                                        secret=self.CONSUMER_SECRET)
        oauth_client = oauth2.Client(oauth_consumer)
        
        resp, content = oauth_client.request(twitter.REQUEST_TOKEN_URL, 'GET')
        
        if resp['status'] != '200':
            print('Invalid respond from Twitter requesting temp token: %s' 
                  % resp['status'])
            return
    
        request_token = dict(urlparse.parse_qsl(content))
        webbrowser.open('%s?oauth_token=%s' % (twitter.AUTHORIZATION_URL,
                                               request_token['oauth_token']))
        pincode = utils.get_input('Enter pincode')
        token = oauth2.Token(request_token['oauth_token'],
                            request_token['oauth_token_secret'])
        token.set_verifier(pincode)
        oauth_client  = oauth2.Client(oauth_consumer, token)
        resp, content = oauth_client.request(twitter.ACCESS_TOKEN_URL,
            method='POST', body='oauth_verifier=%s' % pincode)

        if resp['status'] != '200':
            print('Invalid respond from Twitter requesting access token: %s' 
                  % resp['status'])
            return
        
        access_token  = dict(urlparse.parse_qsl(content))
        request_token = dict(urlparse.parse_qsl(content))
        
        return access_token['oauth_token'], access_token['oauth_token_secret']        
    
    def configure(self, settings):
        if 'access_token_key' not in settings \
           or 'access_token_secret' not in settings:
            tokens = self._get_access_tokens()
            if tokens is None:
                if 'access_token_key' in settings:
                    del settings['access_token_key']
                if 'access_token_secret' in settings:
                    del settings['access_token_secret']
                self.enabled = False
                return
            settings['access_token_key'] = tokens[0]
            settings['access_token_secret'] = tokens[1]
        if 'message' not in settings:
            settings['message'] = utils.get_input('Enter hurtful message')
        
        self._api = twitter.Api(consumer_key=self.CONSUMER_KEY,
            consumer_secret=self.CONSUMER_SECRET, 
            access_token_key=settings['access_token_key'], 
            access_token_secret=settings['access_token_secret'])
        self._message = settings['message'] 
    
    @safety
    def punish(self):
        self._api.PostUpdates(self._message)

        
def _test():
    import punisher
    user = punisher.User('peter')
    user.settings_new()
    test_punisher = punisher.Punisher(user, punishments=(HurtfulTwitterPost,))
    test_punisher.safe_mode = False
    test_punisher.punish()

if __name__ == '__main__':
    _test()
    