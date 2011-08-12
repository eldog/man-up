#!/usr/bin/env python
import os
import pickle
import sys
import tkinter as tk
from tkinter.filedialog import askopenfilename

API_DEV = 'http://localhost:8080/messages/'
API_PRO = 'http://man-up.appspot.com/messages/'

NAMES = ('first', 'second', 'third', 'forth', 'postgrad')

CONFIG_PATH = os.path.join(os.path.dirname(__file__), 'config')

class Config:
    def __init__(self):
        self.emails = []
        self.subject = '[Man-UP] '
        self.image = ''
        self.fb = ''
        self.body = ''
        self.cs = ''
        self.csp = ''
        self.csc = ''
        self.gmail = ''
        self.gmailp = ''
        self.api = API_PRO

    def write(self):
        with open(CONFIG_PATH, 'wb') as f:
            pickle.dump(self, f)

    @staticmethod
    def read():
        with open(CONFIG_PATH, 'rb') as f:
            return pickle.load(f)

class EmailIt(tk.Tk):
    def __init__(self):
        super().__init__()

        self.title('Email It')

        self.columnconfigure(1, weight=1)
        self.protocol('WM_DELETE_WINDOW', self.close)

        l = tk.Label(master=self, text='To:')
        l.grid(row=0, column=0, padx=5, pady=5, stick=tk.E)

        f = tk.Frame(master=self)
        f.grid(row=0, column=1, columnspan=2, padx=5, pady=5, stick=tk.NSEW)

        self.emails = []
        i, j = 0, 0
        for name in NAMES:
            email = '%s@cs.man.ac.uk' % name
            v = tk.StringVar(master=self)
            self.emails.append(v)
            c = tk.Checkbutton(master=f, text=email, onvalue=email,
                offvalue='', variable=v)
            c.select()
            c.grid(row=i, column=j, padx=5, pady=5, stick=tk.W)
            if j == 1:
                i, j = i+1, 0
            else:
                j += 1

        l = tk.Label(master=self, text='Subject:', )
        l.grid(row=1, column=0, padx=5, pady=5, stick=tk.E)

        self.subject = tk.StringVar(master=self, value='[Man-UP] ')
        e = tk.Entry(master=self, textvariable=self.subject)
        e.grid(row=1, column=1, columnspan=2, padx=5, pady=5, stick=tk.EW)

        l = tk.Label(master=self, text='Image:')
        l.grid(row=2, column=0, padx=5, pady=5, stick=tk.E)

        self.image = tk.StringVar(master=self)
        e = tk.Entry(master=self, textvariable=self.image)
        e.grid(row=2, column=1, padx=5, pady=5, stick=tk.EW)

        b = tk.Button(master=self, command=self.browse_image, text='Browse')
        b.grid(row=2, column=2, padx=5, pady=5)

        l = tk.Label(master=self, text='FB Event ID:')
        l.grid(row=3, column=0, padx=5, pady=5, stick=tk.E)

        self.fb = tk.StringVar(master=self)
        e = tk.Entry(master=self, textvariable=self.fb)
        e.grid(row=3, column=1, columnspan=2, padx=5, pady=5, stick=tk.EW)

        self.body = tk.Text(master=self, width=80, height=15)
        self.body.grid(row=4, column=0, columnspan=3, padx=5, pady=6,
            stick=tk.NSEW)

        l = tk.Label(master=self, text='CS Username:')
        l.grid(row=5, column=0, padx=5, pady=5, stick=tk.E)

        self.cs = tk.StringVar(master=self)
        e = tk.Entry(master=self, textvariabl=self.cs)
        e.grid(row=5, column=1, columnspan=2, padx=5, pady=5, stick=tk.EW)

        l = tk.Label(master=self, text='CS Password:')
        l.grid(row=6, column=0, padx=5, pady=5, stick=tk.E)

        self.csp = tk.StringVar(master=self)
        e = tk.Entry(master=self, show='*', textvariable=self.csp)
        e.grid(row=6, column=1, columnspan=2, padx=5, pady=5, stick=tk.EW)

        l = tk.Label(master=self, text='CS Computer:')
        l.grid(row=7, column=0, padx=5, pady=5, stick=tk.E)

        self.csc = tk.StringVar(master=self)
        e = tk.Entry(master=self, textvariable=self.csc)
        e.grid(row=7, column=1, columnspan=2, padx=5, pady=5, stick=tk.EW)

        l = tk.Label(master=self, text='GMail Address:')
        l.grid(row=8, column=0, padx=5, pady=5, stick=tk.E)

        self.gmail = tk.StringVar(master=self)
        e = tk.Entry(master=self, textvariable=self.gmail)
        e.grid(row=8, column=1, columnspan=2, padx=5, pady=5, stick=tk.EW)

        l = tk.Label(master=self, text='GMail Password:')
        l.grid(row=9, column=0, padx=5, pady=5, stick=tk.E)

        self.gmailp = tk.StringVar(master=self)
        e = tk.Entry(master=self, show='*', textvariable=self.gmailp)
        e.grid(row=9, column=1, columnspan=2, padx=5, pady=5, stick=tk.EW)

        l = tk.Label(master=self, text='Msg API:')
        l.grid(row=10, column=0, padx=5, pady=5, stick=tk.E)

        f = tk.Frame(master=self)
        f.grid(row=10, column=1, columnspan=2, stick=tk.NSEW)

        self.api = tk.StringVar(master=self, value=API_PRO)

        r = tk.Radiobutton(master=f, text=API_PRO, variable=self.api,
            value=API_PRO)
        r.grid(row=0, column=0, padx=5, pady=5, stick=tk.W)

        r = tk.Radiobutton(master=f, text=API_DEV, variable=self.api,
            value=API_DEV)
        r.grid(row=1, column=0, padx=5, pady=5, stick=tk.W)

        b = tk.Button(master=self, text='Check & Send')
        b.grid(row=11, column=0, columnspan=3, padx=5, pady=10)

        self.config_load()

    def close(self):
        self.config_write()
        self.destroy()

    def browse_image(self):
        filename = askopenfilename(master=self, filetypes=(('PNG', '*.png'),),
            initialdir=os.path.expanduser('~'))
        if filename:
            self.image.set(filename)

    def config_load(self):
        try:
            self._config = Config.read()
        except IOError as e:
            print(e)
            self._config = Config()
        self.subject.set(self._config.subject)
        self.image.set(self._config.image)
        self.fb.set(self._config.fb)
        self.body.insert('0.0', self._config.body)
        self.cs.set(self._config.cs)
        self.csc.set(self._config.csc)
        self.gmail.set(self._config.gmail)
        self.api.set(self._config.api)

    def config_write(self):
        self._config.subject = self.subject.get()
        self._config.image = self.image.get()
        self._config.fb = self.fb.get()
        self._config.body = self.body.get('0.0', tk.END).strip()
        self._config.cs = self.cs.get()
        self._config.csc = self.csc.get()
        self._config.gmail = self.gmail.get()
        self._config.api = self.api.get()
        try:
            self._config.write()
        except IOError as e:
            print(e)



def main(argv=None):
    if argv is None:
        argv = sys.argv
    tk.NoDefaultRoot()
    emailit = EmailIt()
    emailit.mainloop()
    return 0

if __name__ == '__main__':
    exit(main())