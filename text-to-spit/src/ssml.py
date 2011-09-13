from xml.dom.minidom import getDOMImplementation
_imp = getDOMImplementation()

class SsmlDocument:
    def __init__(self):
        self._doc = _imp.createDocument('http://www.w3.org/2001/10/synthesis',
            'speak', None)
        self.toxml = self._doc.toxml
        self.toprettyxml = self._doc.toprettyxml
        self._speak = self._doc.getElementsByTagName('speak')[0]
        self._speak.setAttribute('version', '1.0')
        self._speak.setAttribute('xml:lang', 'en-US')
        self._speak.setAttribute('xmlns', 'http://www.w3.org/2001/10/synthesis')
        self._speak.setAttribute('xsi:schemaLocation',
            'http://www.w3.org/2001/10/synthesis '
            'http://www.w3.org/TR/speech-synthesis/synthesis.xsd')
        self._speak.setAttribute('xmlns:xsi',
            'http://www.w3.org/2001/XMLSchema-instance')

    def append_sentance(self, sentence):
        element = self._doc.createElement('s')
        self._speak.appendChild(element)
        text = self._doc.createTextNode(sentence)
        element.appendChild(text)
