package cc.kenai.weather.server;

import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.ServletException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by kenai on 14-3-14.
 */
public class saf extends ServletContainer {
    final Thread task = new Thread() {
        @Override
        public void run() {
            while (true) {
                tq.doTask();
            }
        }
    };
    TaskQueue tq;

    @Override
    public void init() throws ServletException {
        super.init();

        tq = new TaskQueue();
        getServletContext().setAttribute("usernum", tq);
        task.start();
    }

    public interface Task {
        public void task();
    }

    public class TaskQueue {
        private List<Task> queue = new LinkedList<Task>();  // 添加一项任务

        public void addTask(Task task) {
            if (task != null) {
                queue.add(task);
            }
        }

        protected void doTask() {
            Iterator<Task> it = queue.iterator();
            Task task;
            while (it.hasNext()) {
                task = it.next();
                // 寻找一个新建的任务
                queue.remove(task);
                task.task();
            }
            return;
        }
    }
}
