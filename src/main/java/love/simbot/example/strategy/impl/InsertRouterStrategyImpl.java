package love.simbot.example.strategy.impl;

import lombok.extern.slf4j.Slf4j;
import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;
import love.simbot.example.cache.RouterStrategyCache;
import love.simbot.example.domain.RobotRouterDTO;
import love.simbot.example.mapper.RobotRouterMapper;
import love.simbot.example.strategy.RouterStrategy;
import love.simbot.example.utils.ApplicationContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("InsertRouterStrategy")
@Slf4j
public class InsertRouterStrategyImpl implements RouterStrategy {

    @Autowired
    RobotRouterMapper robotRouterMapper;

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
            sender.sendGroupMsg(groupMsg, "新增指令参数格式异常，请参考：\"新增指令 开团 OpenTeamStrategy 开团指令备注\"");
            return;
        }
        String routerName = s[1];
        try {
            String routerClass = s[2];
            RouterStrategy bean = ApplicationContextUtil.getBeansByNameAndType(routerClass, RouterStrategy.class);
            if (bean == null) {
                sender.sendGroupMsg(groupMsg, "当前指令找不到具体的策略实现类，策略类名：" + routerClass);
                return;
            }
            RobotRouterDTO robotRouterDTO = new RobotRouterDTO();
            robotRouterDTO.setRouterName(routerName);
            robotRouterDTO.setRouterClass(routerClass);
            if (s.length >= 3) {
                robotRouterDTO.setRemark(s[3]);
            }
            RouterStrategyCache.setRouter(routerName, bean);
            RobotRouterDTO robotRouter = robotRouterMapper.selectRobotRouterByRouterName(routerName);
            if (robotRouter == null) {
                robotRouterMapper.insertRobotRouter(robotRouterDTO);
                sender.sendGroupMsg(groupMsg, "新增指令成功");
            } else {
                robotRouterMapper.updateRobotRouter(robotRouterDTO);
                sender.sendGroupMsg(groupMsg, "更新指令成功");
            }
        } catch (Exception e) {
            RouterStrategyCache.removeRouter(routerName);
            sender.sendGroupMsg(groupMsg, "新增指令异常：" + e.getMessage());
            log.error("新增指令异常", e);
        }
    }

}