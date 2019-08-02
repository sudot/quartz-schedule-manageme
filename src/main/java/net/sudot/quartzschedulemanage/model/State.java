package net.sudot.quartzschedulemanage.model;

/**
 * 状态
 *
 * @author tangjialin on 2019-08-02.
 */
public enum State {
    ACTIVATE("已激活"),
    DISABLED("已禁用");
    private String memo;

    State(String memo) {
        this.memo = memo;
    }

    public String getMemo() {
        return memo;
    }
}
