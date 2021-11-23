package love.simbot.example.listener;

import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.simbot.annotation.Filter;
import love.forte.simbot.annotation.OnGroup;
import love.forte.simbot.annotation.OnGroupMsgRecall;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.message.events.GroupMsgRecall;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;
import love.forte.simbot.filter.MatchType;
import love.simbot.example.cache.RouterStrategyCache;
import love.simbot.example.strategy.RouterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * 群消息监听的示例类。
 * 所有需要被管理的类都需要标注 {@link Beans} 注解。
 *
 * @author ForteScarlet
 */
@Service
@Slf4j
public class MyGroupListen {

    @Autowired
    private MessageContentBuilderFactory builderFactory;

    private volatile Long beginDate = 0L;

    @Autowired
    @Qualifier("CommonReplyStrategy")
    RouterStrategy commonReplyStrategy;

    @OnGroup
    @Filter(atBot = true, trim = true)
    public void onGroupAtMsg(GroupMsg groupMsg, Sender sender, Getter getter, Setter setter) {

        String instructions = groupMsg.getMsgContent().refactor(item -> item.at().remove()).getMsg().trim().split("\\s+")[0];

        RouterStrategy router = RouterStrategyCache.getRouter(instructions);
        if (router == null) {
            commonReplyStrategy.router(groupMsg, sender, getter, setter);
            return;
        }
        router.router(groupMsg, sender, getter, setter);
    }

    @OnGroupMsgRecall
    public void OnGroupMsgRecall(GroupMsgRecall groupMsgRecallMsg, Sender sender, Getter getter, Setter setter) {
        if (lock()) {
            sender.sendGroupMsg(groupMsgRecallMsg, groupMsgRecallMsg.getAccountInfo().getAccountRemarkOrNickname() + "怀孕了就直说了啊，大家一起帮你想办法，撤回干什么。");
        }
    }

    @OnGroup
    @Filter(matchType = MatchType.CONTAINS)
    public void onGroupMsg(GroupMsg groupMsg, Sender sender, Getter getter, Setter setter) {
        if (lock()) {
            //commonReplyStrategy.router(groupMsg, sender, getter, setter);
        }
    }

    private synchronized Boolean lock() {
        if ((System.currentTimeMillis() - beginDate) > 10000) {
            beginDate = System.currentTimeMillis();
            return true;
        }
        return false;
    }
}