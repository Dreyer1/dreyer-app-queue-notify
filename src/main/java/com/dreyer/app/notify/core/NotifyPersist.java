package com.dreyer.app.notify.core;

import com.dreyer.facade.notify.entity.NotifyRecord;
import com.dreyer.facade.notify.entity.NotifyRecordLog;
import com.dreyer.facade.notify.service.NotifyFacade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * @author: Dreyer
 * @date: 16/6/16 上午11:43
 * @description: 该类提供了写通知表和通知日志表的两个方法
 */
@Component
public class NotifyPersist {
    public static NotifyFacade notifyFacade;

    /**
     * 创建商户通知记录.<br/>
     *
     * @param notifyRecord
     * @return
     */
    public static long saveNotifyRecord(NotifyRecord notifyRecord) {
        return notifyFacade.creatNotifyRecord(notifyRecord);
    }

    /**
     * 更新商户通知记录.<br/>
     *
     * @param id
     * @param notifyTimes 通知次数.<br/>
     * @param status      通知状态.<br/>
     * @return 更新结果
     */
    public static void updateNotifyRord(Long id, int notifyTimes, int status) {
        NotifyRecord notifyRecord = notifyFacade.getNotifyById(id);
        notifyRecord.setNotifyTimes(new Short(new Integer(notifyTimes).toString()));
        notifyRecord.setStatus(new Short(new Integer(status).toString()));
        notifyFacade.updateNotifyRecord(notifyRecord);
    }

    /**
     * 创建商户通知日志记录.<br/>
     *
     * @param notifyId        通知记录ID.<br/>
     * @param merchantNo      商户编号.<br/>
     * @param merchantOrderNo 商户订单号.<br/>
     * @param request         请求信息.<br/>
     * @param response        返回信息.<br/>
     * @param httpStatus      通知状态(HTTP状态).<br/>
     * @return 创建结果
     */
    public static long saveNotifyRecordLogs(long notifyId, String merchantNo, String merchantOrderNo, String request, String response,
                                            int httpStatus) {
        NotifyRecordLog notifyRecordLog = new NotifyRecordLog();
        notifyRecordLog.setHttpStatus(new Short(new Integer(httpStatus).toString()));
        notifyRecordLog.setMerchantNo(merchantNo);
        notifyRecordLog.setMerchantOrderNo(merchantOrderNo);
        notifyRecordLog.setNotifyId(notifyId);
        notifyRecordLog.setRequest(request);
        notifyRecordLog.setResponse(response);
        notifyRecordLog.setVersion(new Short("1"));
        notifyRecordLog.setCreateTime(new Date());
        return notifyFacade.creatNotifyRecordLog(notifyRecordLog);
    }

    @Autowired
    public void setNotifyFacade(NotifyFacade notifyFacade) {
        NotifyPersist.notifyFacade = notifyFacade;
    }
}
