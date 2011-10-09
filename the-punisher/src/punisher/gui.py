#!/usr/bin/env python2.6
import datetime
import os
import sys
import tkFont
import Tkinter as tk

from PIL import Image, ImageTk

from punisher import Punisher, PunisherError
import punisher.utils

class _StartFrame(tk.Frame, object):
    def __init__(self, on_start=None, *args, **kwargs):
        super(_StartFrame, self).__init__(*args, **kwargs)
        
        self.punish_datetime = None
        
        self.columnconfigure(1, weight=1)
        
        self._time_label = tk.Label(
            master = self,
            text = 'Judgement Time:')
        self._time_label.grid(row=1, column=0, padx=5, pady=5, stick=tk.E)
        
        self._time_var = tk.StringVar(master=self, value='09:00')
        self._time_var.trace_variable('w', lambda *_:self._validate())
        
        self._time_entry = tk.Entry(
            master = self,
            textvariable = self._time_var)
        self._time_entry.grid(row=1, column=1, padx=5, pady=5, stick=tk.EW)

        self._user_label = tk.Label(
            master = self,
            text = 'Username:')
        self._user_label.grid(row=2, column=0, padx=5, pady=5, stick=tk.E)
        
        self._user_var = tk.StringVar(master=self)
        self._user_var.trace_variable('w', lambda *_:self._validate())
        
        self._user_entry = tk.Entry(
            master = self,
            textvariable = self._user_var)
        self._user_entry.grid(row=2, column=1, padx=5, pady=5, stick=tk.EW)        
        
        self._password_label = tk.Label(
            master = self,
            text = 'Password:')
        self._password_label.grid(row=3, column=0, padx=5, pady=5, stick=tk.E)
        
        self._password_var = tk.StringVar(master=self)
        self._password_var.trace_variable('w', lambda *_:self._validate())
        
        self._password_entry = tk.Entry(
            master = self,
            show = '*',
            textvariable=self._password_var)
        self._password_entry.grid(row=3, column=1, padx=5, pady=5, stick=tk.EW)
        
        self._passwordc_label = tk.Label(
            master = self,
            text = 'Confirm Password:')
        self._passwordc_label.grid(row=4, column=0, padx=5, pady=5, stick=tk.E)
        
        self._passwordc_var = tk.StringVar(master=self)
        self._passwordc_var.trace_variable('w', lambda *_:self._validate())
        
        self._passwordc_entry = tk.Entry(
            master = self,
            show = '*',
            textvariable=self._passwordc_var)
        self._passwordc_entry.grid(row=4, column=1, padx=5, pady=5, stick=tk.EW)
        
        self._safe_var = tk.BooleanVar(master=self, value=True)
        self._safe_chkbox = tk.Checkbutton(master=self, text='Safe Mode',
                                             variable=self._safe_var)
        self._safe_chkbox.grid(row=5, column=0, columnspan=2, padx=5, pady=5,
                                 stick=tk.W)
        
        self._start_button = tk.Button(
            command = on_start,
            master = self,
            state = tk.DISABLED,
            text = 'Bring The Punishment!')
        self._start_button.grid(row=6, column=0, columnspan=2, padx=5, pady=5,
            stick=tk.EW)
        

        self._validate()
    
    @property
    def safe_mode(self):
        return self._safe_var.get()
    
    def _validate(self):
        time_text = self._time_var.get().strip()
        try:
            punish_time = datetime.datetime.strptime(time_text, '%H:%M')
        except ValueError:
            new_state = tk.DISABLED
        else:
            new_state = tk.NORMAL
            punish_time = datetime.time(punish_time.hour, punish_time.minute,
                punish_time.second, punish_time.microsecond,
                punish_time.tzinfo)
            punish_datetime = datetime.datetime.combine(datetime.date.today(),
                punish_time)
            if punish_datetime <= datetime.datetime.now():
                punish_datetime = punish_datetime + datetime.timedelta(days=1)
            self.punish_datetime = punish_datetime
        
        self.username = self._user_var.get().strip()
        
        self.password = self._password_var.get()
        passwordc = self._passwordc_var.get()
        if new_state == tk.NORMAL:
            if self.password and self.password == passwordc and self.username:
                new_state = tk.NORMAL 
            else:
                new_state = tk.DISABLED
        
        self._start_button.config(state=new_state)


class _RunFrame(tk.Frame, object):
    def __init__(self, punish_at=None, on_punish=None,
        on_stopped=None, *args, **kwargs):
        super(_RunFrame, self).__init__(*args, **kwargs)
        
        if not isinstance(punish_at, datetime.datetime):
            raise ValueError('punish_at must be datetime')
        
        self.password = ''

        self._punish_at = punish_at
        self._on_punish = on_punish
        
        self._timer_label = tk.Label(
            font = tkFont.Font(root=self, size=70, weight=tkFont.BOLD),
            master = self)
        self._timer_label.grid(row=0, column=0, columnspan=2, padx=5, pady=5,
            stick=tk.NSEW)
        
        self._password_label = tk.Label(
            master = self,
            text = 'Password:')
        self._password_label.grid(row=1, column=0, padx=5, pady=5, stick=tk.E)
        
        self._password_var = tk.StringVar(master=self)
        self._password_var.trace_variable('w', lambda *_:self._validate())
        
        self._password_entry = tk.Entry(
            master = self,
            show = '*',
            textvariable=self._password_var)
        self._password_entry.grid(row=1, column=1, padx=5, pady=5, stick=tk.EW)
        
        self._stop_button = tk.Button(
            command = on_stopped,
            master = self,
            text = 'Stop, have mercy...')
        self._stop_button.grid(row=2, column=0, columnspan=2, padx=5, pady=5,
            stick=tk.EW)
        self._update_timer()
    
    def _validate(self):
        self.password = self._password_var.get()
        
    def _update_timer(self):
        td = (self._punish_at - datetime.datetime.now())
        total_seconds = (td.microseconds + (td.seconds + td.days * 24 * 3600)
            * 10**6) / 10**6
        if total_seconds < 0:
            total_seconds = 0
        hours, remaining = divmod(total_seconds, 60 * 60)
        minutes, seconds = divmod(remaining, 60)
        self._timer_label.config(text='%02d:%02d:%02d' % (hours, minutes,
            seconds))
        if total_seconds > 0:
            self.after(1000, self._update_timer)
        else:
            self._on_punish()


class PunisherGUI(tk.Tk, object):
    def __init__(self, *args, **kwargs):
        super(PunisherGUI, self).__init__(*args, **kwargs)
        self._punisher = None
        self.columnconfigure(0, weight=1)
        self.rowconfigure(1, weight=1)
        self.title('The Punisher')
        
        self._image = Image.open(punisher.utils.abspath(__file__, 'banner.png'))
        self._photo = ImageTk.PhotoImage(self._image, master=self)
        
        self._banner = tk.Label(master = self, image=self._photo)
        self._banner.grid(row=0, column=0, padx=5, pady=5)
        
        self._frame = _StartFrame(master=self, on_start=self._on_start)
        self._frame.grid(row=1, column=0, padx=5, pady=5)
    
    def _on_punish(self):
        self.destroy()
    
    def _on_start(self):
        punish_datetime = self._frame.punish_datetime
        self._punisher = Punisher(self._frame.username)
        self._punisher.user.settings_save()
        self._punisher.safe_mode = self._frame.safe_mode
        self._punisher.activate(self._frame.punish_datetime,
            self._frame.password)
        self.title('The Punisher' + (' [Safe Mode]' 
                                     if self._punisher.safe_mode else ''))
        self._frame.destroy()
        self._frame = _RunFrame(master=self,
            on_punish=self._on_punish, on_stopped=self._on_stopped,
            punish_at=punish_datetime)
        self._frame.grid(row=1, column=0, padx=5, pady=5)
        self.protocol('WM_DELETE_WINDOW', lambda:None)

    
    def _on_stopped(self):
        try:
            self._punisher.deactivate(self._frame.password)
        except PunisherError:
            return
        self.destroy()
    
    def destroy(self):
        if self._punisher is not None:
            self._punisher._timer.join()
        super(PunisherGUI, self).destroy()
        
        
def main(argv=None):
    if argv is None:
        argv = sys.argv
    tk.NoDefaultRoot()
    punisher_gui = PunisherGUI()
    punisher_gui.mainloop()

if __name__ == '__main__':
    exit(main())
