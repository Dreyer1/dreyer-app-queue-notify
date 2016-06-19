import com.dreyer.common.page.PageParam;
import com.dreyer.common.page.Pager;
import com.dreyer.common.util.JacksonUtil;
import com.dreyer.facade.notify.entity.NotifyRecord;
import com.dreyer.facade.notify.service.NotifyFacade;
import org.apache.log4j.Logger;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Dreyer
 * @date: 16/6/19 上午10:56
 * @description:
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/spring-context.xml"})
public class Test {
    @Autowired
    private NotifyFacade notifyFacade;

    private Logger logger = Logger.getLogger(Test.class);

    @org.junit.Test
    public void startInitFromDB() {

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
            }
            currentPageIndex++;
            logger.info(String.format("调用通知服务.notifyFacade.notiFyReCordListPage(%s, %s, %s)", currentPageIndex, pageSize, paramMap));
            pageParam = new PageParam(pageSize, currentPageIndex);
            pager = notifyFacade.queryNotifyRecordListPage(pageParam, paramMap);
        }
        System.out.println("么么哒:" + JacksonUtil.objectToJson(pager));


    }
}
