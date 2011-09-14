from collections import namedtuple
from queue import Empty, Queue
from threading import Condition, Event, Lock, Thread
from time import sleep

Task = namedtuple('Task', ('cmd', 'args', 'kwargs', 'callback'))

class ThreadPool:
    def __init__(self, pool_size):
        self._threads = []
        self._resize_lock = Condition(Lock())
        self._task_lock = Condition(Lock())
        self._tasks = Queue()
        self._is_joining = False
        self.set_pool_size(pool_size)

    def set_pool_size(self, pool_size):
        if self._is_joining:
            raise ValueError('Pool is joining, cannot change pool size.')

        self._resize_lock.acquire()
        try:
            self._set_pool_size(pool_size)
        finally:
            self._resize_lock.release()
        return True

    def _set_pool_size(self, pool_size):
        # Grow pool
        while pool_size > len(self._threads):
            new_thread = PoolThread(self)
            self._threads.append(new_thread)
            new_thread.start()

        # Shrink pool
        while pool_size < len(self._threads):
            self._threads[0].stop_soon()
            del self._threads[0]

    def get_thread_count(self):
        self._resize_lock.acquire()
        try:
            return len(self._threads)
        finally:
            self._resize_lock.release()

    def queue_task(self, cmd, args=None, kwargs=None, callback=None):
        if self._is_joining:
            raise ValueError('Pool is joining, cannot queue task.')

        if not args:
            args = ()
        if not kwargs:
            kwargs = {}

        self._task_lock.acquire()
        try:
            self._tasks.put(Task(cmd, args, kwargs, callback))
        finally:
            self._task_lock.release()

    def task_done(self):
        self._tasks.task_done()

    def get_next_task(self):
        self._task_lock.acquire()
        try:
            return self._tasks.get_nowait()
        finally:
            self._task_lock.release()

    def join_all(self, wait_for_tasks=True, wait_for_threads=True):
        self._is_joining = True

        if wait_for_tasks:
            self._tasks.join()

        if wait_for_threads:
            threads = list(self._threads)

        self._resize_lock.acquire()
        try:
            self._set_pool_size(0)
        finally:
            self._resize_lock.release()

        if wait_for_threads:
            for thread in threads:
                thread.join()

        self._is_joining = False


class StoppableThread(Thread):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._stop_event = Event()

    def join(self):
        self.stop_soon()
        super().join()

    def stop_soon(self):
        self._stop_event.set()


class PoolThread(StoppableThread):
    thread_sleep_time = 0.1

    def __init__(self, pool):
        super().__init__()
        self._pool = pool

    def run(self):
        while not self._stop_event.is_set():

            try:
                task = self._pool.get_next_task()
            except Empty:
                sleep(self.thread_sleep_time)
                continue

            result = task.cmd(*task.args, **task.kwargs)
            if task.callback:
                task.callback(result)

            self._pool.task_done()
