package love.simbot.example.cache;

import cn.hutool.core.collection.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import love.simbot.example.domain.RobotRouterDTO;
import love.simbot.example.mapper.RobotRouterMapper;
import love.simbot.example.strategy.RouterStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RouterStrategyCache implements ApplicationListener<ContextRefreshedEvent> {

    private static final Map<String, RouterStrategy> ROUTER_STRATEGY_MAP = new ConcurrentHashMap<>();

    @Autowired
    RobotRouterMapper robotRouterMapper;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {

        List<RobotRouterDTO> robotTeamDTOS = robotRouterMapper.selectRobotRouterList();
        if (CollectionUtil.isEmpty(robotTeamDTOS)) {
            log.error("当前代码无策略实现类.");
            return;
        }
        ApplicationContext applicationContext = contextRefreshedEvent.getApplicationContext();
        robotTeamDTOS.forEach(item -> {
            String routerClass = item.getRouterClass();
            RouterStrategy bean = applicationContext.getBean(routerClass, RouterStrategy.class);
            if (bean == null) {
                log.error("当前指令无具体的策略实现类，策略名：{}", routerClass);
                return;
            }
            ROUTER_STRATEGY_MAP.put(item.getRouterName(), bean);
        });
    }

    public static RouterStrategy getRouter(String routerName) {
        return ROUTER_STRATEGY_MAP.get(routerName);
    }

    public static void setRouter(String routerName, RouterStrategy bean) {
        ROUTER_STRATEGY_MAP.put(routerName, bean);
    }

    public static void removeRouter(String routerName) {
        if (ROUTER_STRATEGY_MAP.containsKey(routerName)) {
            ROUTER_STRATEGY_MAP.remove(routerName);
        }
    }
}
