package com.dreyer.app.notify.message;

import com.alibaba.dubbo.rpc.RpcException;
import com.alibaba.fastjson.JSONObject;
import com.dreyer.app.notify.core.NotifyPersist;
import com.dreyer.app.notify.core.NotifyQueue;
import com.dreyer.common.exception.BizException;
import com.dreyer.common.util.JacksonUtil;
import com.dreyer.facade.notify.entity.NotifyRecord;
import com.dreyer.facade.notify.enums.NotifyStatusEnum;
import com.dreyer.facade.notify.service.NotifyFacade;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.jms.listener.SessionAwareMessageListener;
import org.springframework.stereotype.Component;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Date;

/**
 * @author: Dreyer
 * @date: 16/6/14 上午10:32
 * @description 消息队列监听器
 */
@Component
public class ConsumerSessionAwareMessageListener implements SessionAwareMessageListener {
    @Autowired
    private JmsTemplate notifyJmsTemplate;
    @Autowired
    private Destination sessionAwareQueue;
    @Autowired
    private NotifyQueue notifyQueue;
    @Autowired
    private NotifyFacade notifyFacade;

    private Logger logger = Logger.getLogger(ConsumerSessionAwareMessageListener.class);

    @Override
    public void onMessage(Message message, Session session) throws JMSException {
        try {
            ActiveMQTextMessage msg = (ActiveMQTextMessage) message;
            final String ms = msg.getText();
            logger.info("== receive message:" + ms);
            NotifyRecord notifyRecord = JSONObject.parseObject(ms, NotifyRecord.class);
            System.out.println("装好:" + JacksonUtil.objectToJson(notifyRecord));
            if (notifyRecord == null) {
                return;
            }
            Integer createdStatus = NotifyStatusEnum.CREATED.getValue();
            notifyRecord.setStatus(new Short(createdStatus.toString()));
            notifyRecord.setCreateTime(new Date());
            notifyRecord.setLastNotifyTime(new Date());
            notifyRecord.setVersion(new Short("1"));
            if (notifyRecord.getId() == null) {// 判断数据库中是否已有通知记录
                while (notifyFacade == null) {
                    Thread.currentThread().sleep(1000); // 主动休眠，防止类notifyRecordFacade未加载完成，监听服务就开启监听出现空指针异常
                }
                try {
                    // 将获取到的通知先保存到数据库中
                    long notifyId = NotifyPersist.saveNotifyRecord(notifyRecord);
                    notifyRecord.setId(notifyId); // 插入后，立即返回ID
                    // 添加到通知队列
                    notifyQueue.addElementToList(notifyRecord);
                } catch (RpcException e) {
                    notifyJmsTemplate.send(sessionAwareQueue, new MessageCreator() {
                        public Message createMessage(Session session) throws JMSException {
                            return session.createTextMessage(ms);
                        }
                    });
                    logger.error("RpcException :", e);
                } catch (BizException e) {
                    logger.error("BizException :", e);
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(e);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e);
        }
    }
}
