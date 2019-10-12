package com.example.demo.principle.state;

/**
 * 状态抽象类,即Context 上下文，包含所有的状态
 * @author YJL
 */
public abstract class State {

    // 扣除积分
    public abstract void deductMoney();

    // 是否抽中奖品
    public abstract boolean raffle();

    // 发放奖品
    public abstract void dispensePrize();
}
