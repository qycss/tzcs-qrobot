package love.simbot.example.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;
import love.simbot.example.domain.RobotAutomaticReplyDTO;
import love.simbot.example.mapper.RobotAutomaticReplyMapper;
import love.simbot.example.strategy.RouterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("InsertAutomaticReplyStrategy")
@Slf4j
public class InsertAutomaticReplyStrategyImpl implements RouterStrategy {

    @Autowired
    RobotAutomaticReplyMapper robotAutomaticReplyMapper;

    @Override
    public void router(GroupMsg groupMsg, Sender sender, Getter getter, Setter setter) {
        //群号校验
        String groupId = Optional.ofNullable(groupMsg).map(a -> a.getGroupInfo()).map(b -> b.getGroupCode()).orElse(null);
        if (groupId == null) {
            log.error("获取群号失败");
            return;
        }

        //权限校验
        if (!groupMsg.getAccountInfo().getPermission().isOwnerOrAdmin()) {
            sender.sendGroupMsg(groupMsg, "无权限操作");
            return;
        }

        //构造团队数据
        String[] s = groupMsg.getMsgContent().refactor(item -> item.at().remove()).getMsg().trim().split("\\s+");
        if (s.length < 3) {
            sender.sendGroupMsg(groupMsg, "新增自动回复格式异常，请参考：\"新增自动回复 下次一定 滚 \"");
            return;
        }
        try {
            RobotAutomaticReplyDTO robotAutomaticReplyDTO = new RobotAutomaticReplyDTO();
            robotAutomaticReplyDTO.setRobotRevice(s[1]);
            robotAutomaticReplyDTO.setRobotReply(s[2]);
            robotAutomaticReplyMapper.insertRobotAutomaticReply(robotAutomaticReplyDTO);
            sender.sendGroupMsg(groupMsg, "新增自动回复成功");
        } catch (Exception e) {
            sender.sendGroupMsg(groupMsg, "新增自动回复异常：" + e.getMessage());
            log.error("新增自动回复异常", e);
        }
    }

}