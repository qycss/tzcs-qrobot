package love.simbot.example.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.api.message.MessageContent;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;
import love.simbot.example.cache.GroupCache;
import love.simbot.example.domain.RobotTeamDTO;
import love.simbot.example.domain.RobotUserTeamDTO;
import love.simbot.example.mapper.RobotTeamMapper;
import love.simbot.example.strategy.RouterStrategy;
import love.simbot.example.utils.ImageUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("LatestTeamStrategy")
@Slf4j
public class LatestTeamStrategyImpl implements RouterStrategy {
    @Autowired
    RobotTeamMapper robotTeamMapper;

    @Depend
    @Autowired
    private MessageContentBuilderFactory messageContentBuilderFactory;

    @Override
    public void router(GroupMsg groupMsg, Sender sender, Getter getter, Setter setter) {
        //群号校验
        String groupId = Optional.ofNullable(groupMsg).map(a -> a.getGroupInfo()).map(b -> b.getGroupCode()).orElse(null);
        if (groupId == null) {
            log.error("获取群号失败");
            return;
        }

        try {
            Map<String, RobotUserTeamDTO> userCache = GroupCache.getUserCache(groupId);

            if (userCache == null) {
                sender.sendGroupMsg(groupMsg, "查看失败，当前暂无开团计划！");
                return;
            }

            // 获取本群最新招募团队编号
            List<RobotTeamDTO> robotTeamDTOS = robotTeamMapper.selectOpenRobotTeamByGroupId(groupId);

            if (CollectionUtils.isEmpty(robotTeamDTOS)) {
                sender.sendGroupMsg(groupMsg, "缓存与数据库数据不一致，清联系管理员");
                return;
            }

            List<RobotUserTeamDTO> userList = new ArrayList(userCache.values());

            MessageContent build = messageContentBuilderFactory.getMessageContentBuilder().image(ImageUtils.myGraphicsGeneration(userList, robotTeamDTOS.get(0).getTeamName())).build();
            sender.sendGroupMsg(groupMsg, build);

        } catch (Exception e) {
            sender.sendGroupMsg(groupMsg, "团队列表异常：" + e.getMessage());
            log.error("团队列表异常", e);
        }
    }
}