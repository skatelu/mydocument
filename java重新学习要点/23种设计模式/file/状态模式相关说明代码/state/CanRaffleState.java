package com.example.demo.principle.state;

import java.util.Random;

/**
 * 这个是抽奖状态
 */
public class CanRaffleState extends State {

    RaffleActivity activity;

    public CanRaffleState(RaffleActivity activity) {
        this.activity = activity;
    }

    /**
     * 已经扣除积分，不能再扣
     */
    @Override
    public void deductMoney() {
        System.out.println("已经扣除过积分了");
    }

    /**
     * 可以抽奖，抽完奖后，根据实际情况，改为新的状态
     * @return
     */
    @Override
    public boolean raffle() {
        System.out.println("正在抽奖，请稍等！");
        Random r = new Random();
        int num = r.nextInt(10);
        // 10% 的中奖机会
        if (num == 0) {
            // 改变活动状态为发放奖品
            activity.setState(activity.getDispenseState());
            return true;
        }else {
            System.out.println("很遗憾，没有抽中奖品！");
            // 改变状态为不能抽奖
            activity.setState(activity.getNoRaffleState());
            return false;
        }
    }

    /**
     * 不能发放奖品
     */
    @Override
    public void dispensePrize() {
        System.out.println("没中奖，不能发放奖品！");
    }
}
