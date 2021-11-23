package love.simbot.example.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.containers.AccountInfo;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;
import love.simbot.example.cache.GroupCache;
import love.simbot.example.constants.RotConstants;
import love.simbot.example.domain.RobotTeamDTO;
import love.simbot.example.domain.RobotUserTeamDTO;
import love.simbot.example.mapper.RobotTeamMapper;
import love.simbot.example.mapper.RobotUserTeamMapper;
import love.simbot.example.strategy.RouterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component("JoinTeamStrategy")
@Slf4j
public class JoinTeamStrategyImpl implements RouterStrategy {

    @Autowired
    RobotTeamMapper robotTeamMapper;

    @Autowired
    RobotUserTeamMapper robotUserTeamMapper;


    @Override
    public void router(GroupMsg groupMsg, Sender sender, Getter getter, Setter setter) {
        //群号校验
        String groupId = Optional.ofNullable(groupMsg).map(a -> a.getGroupInfo()).map(b -> b.getGroupCode()).orElse(null);
        if (groupId == null) {
            log.error("获取群号失败");
            return;
        }

        String[] s = groupMsg.getMsgContent().refactor(item -> item.at().remove()).getMsg().trim().split("\\s+");
        if (s.length != 1) {
            sender.sendGroupMsg(groupMsg, "进组格式异常，请使用关键词：\"进组\" or \"报名\" ");
            return;
        }

        Map<String, RobotUserTeamDTO> userCache = GroupCache.getUserCache(groupId);
        if (userCache == null) {
            sender.sendGroupMsg(groupMsg, "进组失败，当前暂无开团计划！");
            return;
        }

        if (userCache.size() >= 25) {
            sender.sendGroupMsg(groupMsg, "进组失败，团队人数已满！");
            return;
        }

        try {
            AccountInfo accountInfo = groupMsg.getAccountInfo();

            // 重复进组检测
            if (userCache.containsKey(accountInfo.getAccountCode())) {
                sender.sendGroupMsg(groupMsg, accountInfo.getAccountRemarkOrNickname() + "已进组，请勿重复报名！");
                return;
            }

            // 获取本群最新招募团队编号
            List<RobotTeamDTO> robotTeamDTOS = robotTeamMapper.selectOpenRobotTeamByGroupId(groupId);
            if (CollectionUtils.isEmpty(robotTeamDTOS)) {
                sender.sendGroupMsg(groupMsg, "缓存与数据库数据不一致，清联系管理员");
                return;
            }

            Long teamId = robotTeamDTOS.get(0).getTeamId();
            RobotUserTeamDTO robotUserTeamDTO = new RobotUserTeamDTO();
            robotUserTeamDTO.setTeamId(teamId);
            robotUserTeamDTO.setUserCode(groupMsg.getAccountInfo().getAccountCode());
            robotUserTeamDTO.setUserName(groupMsg.getAccountInfo().getAccountRemarkOrNickname());
            robotUserTeamDTO.setUserType(RotConstants.ORDINARY_MEMBER);
            robotUserTeamMapper.addRobotUserTeam(robotUserTeamDTO);
            GroupCache.putUserCache(groupId, robotUserTeamDTO);

            sender.sendGroupMsg(groupMsg, "「" + accountInfo.getAccountRemarkOrNickname() + "」加入团队「" + robotTeamDTOS.get(0).getTeamName() + "」成功！\n当前还剩" + (25 - userCache.size()) + "个位置。");

        } catch (Exception e) {
            sender.sendGroupMsg(groupMsg, "进组异常：" + e.getMessage());
            log.error("进组异常", e);
        }

    }

}
