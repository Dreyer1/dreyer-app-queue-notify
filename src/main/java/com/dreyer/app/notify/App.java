package com.dreyer.app.notify;

import com.dreyer.app.notify.core.NotifyQueue;
import com.dreyer.app.notify.core.NotifyTask;
import com.dreyer.common.page.PageParam;
import com.dreyer.common.page.Pager;
import com.dreyer.facade.notify.entity.NotifyRecord;
import com.dreyer.facade.notify.service.NotifyFacade;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.log4j.Logger;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.DelayQueue;

/**
 * @author: Dreyer
 * @date: 16/6/14 上午10:13
 * @description
 */
public class App {
    private static final Logger logger = Logger.getLogger(App.class);
    /**
     * 存放任务的延时队列
     */
    public static DelayQueue<NotifyTask> tasks = new DelayQueue<NotifyTask>();
    /**
     * 线程池
     */
    private static ThreadPoolTaskExecutor threadPool;

    private static ClassPathXmlApplicationContext context;

    public static NotifyFacade notifyFacade;

    private static NotifyQueue notifyQueue;

    public static void main(String[] args) {
        try {
            context = new ClassPathXmlApplicationContext(new String[]{"spring/spring-context.xml"});
            context.start();
            threadPool = (ThreadPoolTaskExecutor) context.getBean("threadPool");
            notifyFacade = (NotifyFacade) context.getBean("notifyFacade");
            notifyQueue = (NotifyQueue) context.getBean("notifyQueue");
            startInitFromDB();
            startThread();
            logger.info("== context start");
        } catch (Exception e) {
            logger.error("== application start error:", e);
            return;
        }
        synchronized (App.class) {
            while (true) {
                try {
                    App.class.wait();
                } catch (InterruptedException e) {
                    logger.error("== synchronized error:", e);
                }
            }
        }
    }

    private static void startThread() {
        logger.info("startThread");

        threadPool.execute(new Runnable() {
            public void run() {
                try {
                    while (true) {
                        Thread.sleep(1);
                        // 如果当前活动线程等于最大线程，那么不执行
                        if (threadPool.getActiveCount() < threadPool.getMaxPoolSize()) {
                            final NotifyTask task = tasks.poll();
                            if (task != null) {
                                threadPool.execute(new Runnable() {
                                    public void run() {
                                        logger.info(threadPool.getActiveCount() + "---------");
                                        tasks.remove(task);
                                        task.run();
                                    }
                                });
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 从数据库中取一次数据用来当系统启动时初始化
     */
    @SuppressWarnings("unchecked")
    private static void startInitFromDB() {
        logger.info("get data from database");

        int currentPageIndex = 1;
        int pageSize = 500;
        PageParam pageParam = new PageParam(pageSize, currentPageIndex);

        // 查询状态和通知次数符合以下条件的数据进行通知
        String[] status = new String[]{"101", "102", "200", "201"};
        Integer[] notifyTime = new Integer[]{0, 1, 2, 3, 4};
        // 组装查询条件
        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("statusList", status);
        paramMap.put("notifyTimeList", notifyTime);

        Pager<NotifyRecord> pager = notifyFacade.queryNotifyRecordListPage(pageParam, paramMap);
        int totalSize = (pager.getPageSize() - 1) / pageSize + 1;//总页数
        while (currentPageIndex <= totalSize) {
            List<NotifyRecord> list = pager.getPageData();
            for (int i = 0; i < list.size(); i++) {
                NotifyRecord notifyRecord = list.get(i);
                notifyRecord.setLastNotifyTime(new Date());
                notifyQueue.addElementToList(notifyRecord);
            }
            currentPageIndex++;
            logger.info(String.format("调用通知服务.notifyFacade.notiFyReCordListPage(%s, %s, %s)", currentPageIndex, pageSize, paramMap));
            pageParam = new PageParam(pageSize, currentPageIndex);
            pager = notifyFacade.queryNotifyRecordListPage(pageParam, paramMap);
        }
    }

}
