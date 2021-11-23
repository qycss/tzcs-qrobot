package love.simbot.example.timer;

import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.bot.BotManager;
import love.forte.simbot.timer.EnableTimeTask;
import love.forte.simbot.timer.Fixed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@EnableTimeTask
public class Timer {

    @Autowired
    private BotManager botManager;

    Long sum = 0L;

    Long groupSum = 0L;

    @Fixed(value = 2, timeUnit = TimeUnit.MINUTES)
    public void task() {
        sum++;
        Sender sender = botManager.getDefaultBot().getSender().SENDER;
        //sender.sendPrivateMsg("1852023741", "定时任务发送群消息，为了证明没有掉线并且躲过风控管理。次数：" + sum);
        sender.sendPrivateMsg("1844169164", "定时任务发送群消息，为了证明没有掉线并且躲过风控管理。次数：" + sum);
    }

    @Fixed(value = 30, timeUnit = TimeUnit.MINUTES)
    public void groupTask() {
        groupSum++;
        Sender sender = botManager.getDefaultBot().getSender().SENDER;
        //sender.sendGroupMsg("661692775", "定时任务发送群消息，为了证明没有掉线并且躲过风控管理。次数：" + groupSum);
        sender.sendGroupMsg("333982273", "定时任务发送群消息，为了证明没有掉线并且躲过风控管理。次数：" + groupSum);
    }
}