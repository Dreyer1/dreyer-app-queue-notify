package com.dreyer.app.notify.entity;

import java.util.Map;

/**
 * @author: Dreyer
 * @date: 16/6/16 上午11:41
 * @description 通知参数, 可以由配置notify.xml来修改参数
 */
public class NotifyParam {
    /**
     * 通知时间次数map
     */
    private Map<Integer, Integer> notifyParams;
    /**
     * 通知后用于判断是否成功的返回值。由HttpResponse获取
     */
    private String successValue;

    public Map<Integer, Integer> getNotifyParams() {
        return notifyParams;
    }

    public void setNotifyParams(Map<Integer, Integer> notifyParams) {
        this.notifyParams = notifyParams;
    }

    public String getSuccessValue() {
        return successValue;
    }

    public void setSuccessValue(String successValue) {
        this.successValue = successValue;
    }

    public Integer getMaxNotifyTime() {
        return notifyParams == null ? 0 : notifyParams.size();
    }

}
