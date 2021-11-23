package love.simbot.example.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;
import love.simbot.example.domain.RobotTeamDTO;
import love.simbot.example.domain.RobotUserTeamDTO;
import love.simbot.example.mapper.RobotTeamMapper;
import love.simbot.example.mapper.RobotUserTeamMapper;
import love.simbot.example.strategy.RouterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Optional;
import java.util.Random;

@Component("FirstInInstancesStrategy")
@Slf4j
public class FirstInInstancesStrategyImpl implements RouterStrategy {

    @Autowired
    RobotUserTeamMapper robotUserTeamMapper;

    @Autowired
    RobotTeamMapper robotTeamMapper;

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

        //团队存在性校验
        List<RobotTeamDTO> robotTeamDTOS = robotTeamMapper.selectOpenRobotTeamByGroupId(groupId);
        if (CollectionUtil.isEmpty(robotTeamDTOS)) {
            sender.sendGroupMsg(groupMsg, "不存在已开启的团队");
            return;
        }

        //获取已进组团队列表
        Long teamId = robotTeamDTOS.get(0).getTeamId();
        String teamName = robotTeamDTOS.get(0).getTeamName();
        List<RobotUserTeamDTO> robotUserTeamDTOS = robotUserTeamMapper.selectOpenRobotUserTeamListByTeamId(teamId);
        if (CollectionUtils.isEmpty(robotUserTeamDTOS)) {
            sender.sendGroupMsg(groupMsg, "当前团队：" + teamName + "还未有人进组");
            return;
        }

        //随机选择黑本人
        try {
            Random random = new Random();
            int i = random.nextInt(robotUserTeamDTOS.size());
            RobotUserTeamDTO robotUserTeamDTO = robotUserTeamDTOS.get(i);
            RobotTeamDTO robotTeamDTO = new RobotTeamDTO();
            robotTeamDTO.setTeamId(teamId);
            robotTeamDTO.setFirstInInstances(robotUserTeamDTO.getUserName() + "（" + robotUserTeamDTO.getUserCode() + "）");
            robotTeamMapper.chooseRobotTeamFirstInInstances(robotTeamDTO);
            sender.sendGroupMsg(groupMsg, "当前团队：" + teamName + "，黑本人为：" + robotTeamDTO.getFirstInInstances());
        } catch (Exception e) {
            sender.sendGroupMsg(groupMsg, "选择黑本人异常：" + e.getMessage());
            log.error("选择黑本人异常", e);
        }
    }

}
