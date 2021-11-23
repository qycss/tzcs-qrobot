package love.simbot.example.strategy.impl;

import com.mysql.cj.util.StringUtils;
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
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Slf4j
@Component("CommonReplyStrategy")
public class CommonReplyStrategyImpl implements RouterStrategy {

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
        String robotRevice = "defaultRevice";
        //指令参数校验
        String[] s = groupMsg.getMsgContent().refactor(item -> item.at().remove()).getMsg().trim().split("\\s+");
        if (s.length >= 1 && !StringUtils.isNullOrEmpty(s[0])) {
            robotRevice = s[0];
        }
        List<RobotAutomaticReplyDTO> robotAutomaticReplyDTOS = robotAutomaticReplyMapper.selectRobotRouterList(robotRevice);
        if (CollectionUtils.isEmpty(robotAutomaticReplyDTOS)) {
            //sender.sendGroupMsg(groupMsg, "当前指令无效");
            return;
        }
        Random random = new Random();
        int i = random.nextInt(robotAutomaticReplyDTOS.size());
        sender.sendGroupMsg(groupMsg, robotAutomaticReplyDTOS.get(i).getRobotReply());
    }

}
