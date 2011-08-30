from __future__ import division
from __future__ import print_function
import Tkinter as tk

class SwipeUpGui(tk.Tk, object):
    def __init__(self, *args, **kwargs):
        super(SwipeUpGui, self).__init__(*args, **kwargs)
        self.title('Swipe Up')
        self.columnconfigure(1, weight=1)

        def make_labelled_entry(text, row):
            l = tk.Label(
                master=self,
                text='%s:' % text)
            l.grid(row=row, column=0, padx=5, pady=5, stick=tk.E)
            s = tk.StringVar(master=self)
            e = tk.Entry(
                master=self,
                textvariable=s)
            e.grid(row=row, column=1, padx=5, pady=5, stick=tk.EW)
            return s

        self._name = make_labelled_entry('Name', 0)
        self._email = make_labelled_entry('Email', 1)
        self._student_id = make_labelled_entry('Student ID', 2)

        b = tk.Button(
            master=self,
            command=self.quit,
            text='Exit')
        b.grid(row=3, column=0, columnspan=2, padx=5, pady=5, stick=tk.E)

    def json_update(self, json):
        self._name.set(json['cn'][0])
        self._email.set(json['mail'][0])
        self._student_id.set(json['umanPersonID'][0])


def _test():
    tk.NoDefaultRoot()
    gui = SwipeUpGui()
    gui.mainloop()

if __name__ == '__main__':
    _test()
