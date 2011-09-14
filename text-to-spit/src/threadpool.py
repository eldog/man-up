from time import sleep
import threading

class ThreadPool:
    def __init__(self, pool_size):
        self._threads = []
        self._resize_lock = threading.Condition(threading.Lock())
        self._task_lock = threading.Condition(threading.Lock())
        self._tasks = []
        self._is_joining = False
        self.set_pool_size(pool_size)

    def set_pool_size(self, pool_size):
        if self._is_joining:
            return False

        self._resize_lock.acquire()
        try:
            self._set_pool_size(pool_size)
        finally:
            self._resize_lock.release()
        return True

    def _set_pool_size(self, pool_size):
        # Grow pool
        while pool_size > len(self._threads):
            newThread = ThreadPoolThread(self)
            self._threads.append(newThread)
            newThread.start()

        # Shrink pool
        while pool_size < len(self._threads):
            self._threads[0].go_away()
            del self._threads[0]

    def get_thread_count(self):
        self._resize_lock.acquire()
        try:
            return len(self._threads)
        finally:
            self._resize_lock.release()

    def queue_task(self, task, args=None, task_callback=None):
        if self._is_joining:
            return False
        if not callable(task):
            return False

        self._task_lock.acquire()
        try:
            self._tasks.append((task, args, task_callback))
            return True
        finally:
            self._task_lock.release()

    def get_next_task(self):
        self._task_lock.acquire()
        try:
            if self._tasks:
                return self._tasks.pop(0)
            else:
                return None, None, None
        finally:
            self._task_lock.release()

    def join_all(self, wait_for_tasks=True, wait_for_threads=True):
        # Mark the pool as joining to prevent any more task queueing
        self._is_joining = True

        # Wait for tasks to finish
        if wait_for_tasks:
            while self._tasks:
                sleep(.1)

        # Tell all the threads to quit
        self._resize_lock.acquire()
        try:
            self._set_pool_size(0)
            self._is_joining = True

            # Wait until all threads have exited
            if wait_for_threads:
                for t in self._threads:
                    t.join()

        finally:
            self._resize_lock.release()

        # Reset the pool for potential reuse
        self._is_joining = False


class ThreadPoolThread(threading.Thread):
    thread_sleep_time = 0.1

    def __init__(self, pool):
        super().__init__()
        self._pool = pool
        self._is_dying = False

    def run(self):
        while not self._is_dying:
            cmd, args, callback = self._pool.get_next_task()
            if cmd is not None:
                result = cmd(*args)
                if callback is not None:
                    callback(result)
            else:
                sleep(self.thread_sleep_time)

    def go_away(self):
        self._is_dying = True


if __name__ == '__main__':

    from random import randrange

    # Sample task 1: given a start and end value, shuffle integers,
    # then sort them

    def sortTask(data):
        print ("SortTask starting for ", data)
        numbers = list(range(data[0], data[1]))
        for a in numbers:
            rnd = randrange(0, len(numbers) - 1)
            a, numbers[rnd] = numbers[rnd], a
        print("SortTask sorting for ", data)
        numbers.sort()
        print ("SortTask done for ", data)
        return ("Sorter ", data)

    # Sample task 2: just sleep for a number of seconds.

    def waitTask(data):
        print ("WaitTask starting for ", data)
        print ("WaitTask sleeping for %d seconds" % data)
        sleep(data)
        return ("Waiter", data)

    # Both tasks use the same callback

    def taskCallback(data):
        print ("Callback called for", data)

    # Create a pool with three worker threads

    pool = ThreadPool(3)

    # Insert tasks into the queue and let them run
    pool.queue_task(sortTask, (1000, 100000), taskCallback)
    pool.queue_task(waitTask, 5, taskCallback)
    pool.queue_task(sortTask, (200, 200000), taskCallback)
    pool.queue_task(waitTask, 2, taskCallback)
    pool.queue_task(sortTask, (3, 30000), taskCallback)
    pool.queue_task(waitTask, 7, taskCallback)

    # When all tasks are finished, allow the threads to terminate
    pool.join_all()
## end of http://code.activestate.com/recipes/203871/ }}}
