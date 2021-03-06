package love.simbot.example.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.containers.AccountInfo;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;
import love.simbot.example.cache.GroupCache;
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

@Component("QuitTeamStrategy")
@Slf4j
public class QuitTeamStrategyImpl implements RouterStrategy {

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
        if (s.length != 1 && s.length != 2) {
            sender.sendGroupMsg(groupMsg, "退组格式异常，本人退组：\"退组/取消报名\" \n代他人退组：\"退组/取消报名 已报名ID\" ");
            return;
        }

        Map<String, RobotUserTeamDTO> userCache = GroupCache.getUserCache(groupId);
        if (userCache == null) {
            sender.sendGroupMsg(groupMsg, "当前暂无开团计划！");
            return;
        }

        try {
            AccountInfo accountInfo = groupMsg.getAccountInfo();
            String username = s.length==2 ? s[1] : accountInfo.getAccountRemarkOrNickname();
            String usercode = s.length==2 ? robotUserTeamMapper.selectUserTeamUserCodeByUserName(username) : accountInfo.getAccountCode();

            // 退组检测
            if (!userCache.containsKey(usercode)) {
                sender.sendGroupMsg(groupMsg, "退组失败，「" + username + "」并未报名！");
                return;
            }

            // 获取本群最新招募团队编号
            List<RobotTeamDTO> robotTeamDTOS = robotTeamMapper.selectOpenRobotTeamByGroupId(groupId);
            if (CollectionUtils.isEmpty(robotTeamDTOS)) {
                sender.sendGroupMsg(groupMsg, "缓存与数据库数据不一致，清联系管理员");
                return;
            }
            Long teamId = robotTeamDTOS.get(0).getTeamId();

            // 删除报名信息
            robotUserTeamMapper.deleteRobotUserTeam(usercode, teamId);
            userCache.remove(usercode);
            sender.sendGroupMsg(groupMsg, "「" + username + "」退出团队「" + robotTeamDTOS.get(0).getTeamName() + "」成功！\n当前还剩" + (25 - userCache.size()) + "个位置。");

        } catch (Exception e) {
            sender.sendGroupMsg(groupMsg, "退组异常：" + e.getMessage());
            log.error("退组异常", e);
        }

    }
}
