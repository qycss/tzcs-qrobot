package love.simbot.example.strategy.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.mysql.cj.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;
import love.simbot.example.cache.GroupCache;
import love.simbot.example.constants.RotConstants;
import love.simbot.example.domain.RobotTeamDTO;
import love.simbot.example.mapper.RobotTeamMapper;
import love.simbot.example.strategy.RouterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("CloseTeamStrategy")
@Slf4j
public class CloseTeamStrategyImpl implements RouterStrategy {

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
            sender.sendGroupMsg(groupMsg, "不存在可关闭的团队");
            return;
        }

        //构造团队数据
        try {
            String result = "关闭团队成功";
            RobotTeamDTO robotTeamDTO = new RobotTeamDTO();
            robotTeamDTO.setGroupId(groupId);

            //获取总工资（如果存在）
            String[] s = groupMsg.getMsgContent().refactor(item -> item.at().remove()).getMsg().trim().split("\\s+");
            if (s.length >= 2) {
                robotTeamDTO.setTeamAmount(s[1]);
                result = result + "，此次团队总工资为：" + s[1];
                String firstInInstances = robotTeamDTOS.get(0).getFirstInInstances();
                if (!StringUtils.isNullOrEmpty(firstInInstances)) {
                    result = result + "，此次黑本人：" + firstInInstances;
                }
            }
            robotTeamDTO.setTeamStatus(RotConstants.CLOSE_GROUP);
            robotTeamMapper.closeRobotTeam(robotTeamDTO);
            GroupCache.removeGroupCache(groupId);
            sender.sendGroupMsg(groupMsg, result);
        } catch (Exception e) {
            sender.sendGroupMsg(groupMsg, "关闭团队异常：" + e.getMessage());
            log.error("关闭团队异常", e);
        }
    }

}