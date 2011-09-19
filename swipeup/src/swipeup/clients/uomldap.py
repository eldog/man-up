from __future__ import division
from __future__ import print_function
import logging
import threading

import ldap

_logger = logging.getLogger('LdapClient')

class UomLdapClient(threading.Thread):
    def __init__(self, host, port, person_id_queue, outgoing_queues=(), callbacks=()):
        super(UomLdapClient, self).__init__()
        self.daemon = True

        self._host = host
        self._port = port

        self._person_info_queue = person_id_queue
        self._outgoing_queues = outgoing_queues
        self._callbacks = callbacks

    def run(self):
        while True:
            student_card = self._person_info_queue.get()
            response = self._query_ldap_server(student_card)
            if response:
                for queue in self._outgoing_queues:
                    queue.put(response)
                for callback in self._callbacks:
                    callback(response)
                self._person_info_queue.task_done()

    def _query_ldap_server(self, person_id):
        conn = ldap.initialize('ldap://%s:%d' % (self._host, self._port))
        _logger.info('Looking up %r', person_id)
        try:
            ldap_result = conn.search_s('', ldap.SCOPE_SUBTREE,
                '(umanPersonID=%s)' % person_id)
        except ldap.SERVER_DOWN:
            _logger.error('Failed to connect to LDAP server.')
        else:
            if ldap_result and len(ldap_result[0]) >= 1:
                return ldap_result[0][1]
            _logger.warning('No results for Student ID: %s', person_id)

