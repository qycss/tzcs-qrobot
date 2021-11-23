package love.simbot.example.strategy.impl;

import lombok.extern.slf4j.Slf4j;
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
import java.util.Optional;

@Slf4j
@Component("OpenTeamStrategy")
public class OpenTeamStrategyImpl implements RouterStrategy {

    @Autowired
    RobotTeamMapper robotTeamMapper;

    @Autowired
    RobotUserTeamMapper robotUserTeamMapper;


    @Override
    public synchronized void router(GroupMsg groupMsg, Sender sender, Getter getter, Setter setter) {
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

        //同一个群团队唯一性校验
        List<RobotTeamDTO> robotTeamDTOS = robotTeamMapper.selectOpenRobotTeamByGroupId(groupId);
        if (!CollectionUtils.isEmpty(robotTeamDTOS)) {
            sender.sendGroupMsg(groupMsg, "已存在团队，团名：" + robotTeamDTOS.get(0).getTeamName());
            return;
        }

        //指令参数校验
        String[] s = groupMsg.getMsgContent().refactor(item -> item.at().remove()).getMsg().trim().split("\\s+");
        if (s.length < 2) {
            sender.sendGroupMsg(groupMsg, "开团参数格式异常，请参考：\"棍来 周四晚8点25棍僧打瘤子\"");
            return;
        }

        //构造团队数据
        try {
            RobotTeamDTO robotTeamDTO = new RobotTeamDTO();
            robotTeamDTO.setTeamName(s[1]);
            robotTeamDTO.setGroupId(groupId);
            robotTeamDTO.setTeamStatus(RotConstants.OPEN_GROUP);
            robotTeamMapper.openRobotTeam(robotTeamDTO);
            sender.sendGroupMsg(groupMsg, "开团成功，团名：" + s[1]);

            //团长自动进组
            RobotUserTeamDTO robotUserTeamDTO = new RobotUserTeamDTO();
            robotUserTeamDTO.setTeamId(robotTeamDTO.getTeamId());
            robotUserTeamDTO.setUserCode(groupMsg.getAccountInfo().getAccountCode());
            robotUserTeamDTO.setUserName(groupMsg.getAccountInfo().getAccountRemarkOrNickname());
            robotUserTeamDTO.setUserType(RotConstants.REGIMENTAL_COMMANDER);
            robotUserTeamMapper.addRobotUserTeam(robotUserTeamDTO);
            GroupCache.putUserCache(groupId, robotUserTeamDTO);

            //设置群公告
            String groupNoticeText = "新开团，要来的人艾特机器人报名";
            sender.sendGroupNotice(groupMsg, "新开团通知：" + s[1], s[1] + groupNoticeText, true, false, false, true);
        } catch (Exception e) {
            sender.sendGroupMsg(groupMsg, "开团异常：" + e.getMessage());
            log.error("创建团队异常", e);
        }
    }

}