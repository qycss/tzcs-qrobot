package love.simbot.example.cache;

import love.simbot.example.domain.RobotTeamDTO;
import love.simbot.example.domain.RobotUserTeamDTO;
import love.simbot.example.mapper.RobotTeamMapper;
import love.simbot.example.mapper.RobotUserTeamMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class GroupCache implements ApplicationListener<ContextRefreshedEvent> {

    private static final Map<String, Map<String, RobotUserTeamDTO>> GROUP_CACHE = new ConcurrentHashMap<>();

    @Autowired
    RobotUserTeamMapper robotUserTeamMapper;

    @Autowired
    RobotTeamMapper robotTeamMapper;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        List<RobotTeamDTO> robotTeamDTOS = robotTeamMapper.selectOpenRobotTeamByGroupId(null);
        robotTeamDTOS.forEach(item -> {
            String groupId = item.getGroupId();
            List<RobotUserTeamDTO> robotUserTeamDTOS = robotUserTeamMapper.selectOpenRobotUserTeamListByTeamId(item.getTeamId());

            //Map<String, RobotUserTeamDTO> robotUserTeamMap = robotUserTeamDTOS.stream().collect(Collectors.toConcurrentMap(RobotUserTeamDTO::getUserCode, Function.identity(), (key1, key2) -> key2));
            Map<String, RobotUserTeamDTO> robotUserTeamMap = mapResort(robotUserTeamDTOS);

            GROUP_CACHE.put(groupId, robotUserTeamMap);
        });
    }

    public static void putUserCache(String groupId, RobotUserTeamDTO robotUserTeamDTO) {
        Map<String, RobotUserTeamDTO> robotUserTeamDTOMap = GROUP_CACHE.get(groupId);
        if (robotUserTeamDTOMap == null) {
            robotUserTeamDTOMap = new ConcurrentHashMap<>();
            robotUserTeamDTOMap.put(robotUserTeamDTO.getUserCode(), robotUserTeamDTO);
            GROUP_CACHE.put(groupId, robotUserTeamDTOMap);
            return;
        }
        robotUserTeamDTOMap.put(robotUserTeamDTO.getUserCode(), robotUserTeamDTO);
    }

    public static void removeUserCache(String groupId, RobotUserTeamDTO robotUserTeamDTO) {
        GROUP_CACHE.get(groupId).remove(robotUserTeamDTO.getUserCode());
    }

    public static void removeGroupCache(String groupId) {
        GROUP_CACHE.remove(groupId);
    }

    public static Map<String, RobotUserTeamDTO> getUserCache(String groupId) {
        return GROUP_CACHE.get(groupId);
    }

    // 自定义比较器：根据UserTeamDTO的id(也即报名时间)排序
    static Comparator<Map.Entry<String, RobotUserTeamDTO>> idCmp = new Comparator<Map.Entry<String, RobotUserTeamDTO>>() {
        @Override
        public int compare(Map.Entry<String, RobotUserTeamDTO> o1, Map.Entry<String, RobotUserTeamDTO> o2) {
            return (int) (o1.getValue().getId() - o2.getValue().getId());
        }
    };

    public static Map<String, RobotUserTeamDTO> mapResort(List<RobotUserTeamDTO> robotUserTeamDTOS) {
        Map<String, RobotUserTeamDTO> unsortedMap = robotUserTeamDTOS.stream().collect(Collectors.toConcurrentMap(RobotUserTeamDTO::getUserCode, Function.identity(), (key1, key2) -> key2));
        // 将map转换为list
        List<Map.Entry<String, RobotUserTeamDTO>> robotUserTeamlist = new ArrayList<Map.Entry<String, RobotUserTeamDTO>>(unsortedMap.entrySet());
        Collections.sort(robotUserTeamlist, idCmp);

        // list转回map
        Map<String, RobotUserTeamDTO> sortedMap = new LinkedHashMap<String, RobotUserTeamDTO>();
        Iterator<Map.Entry<String, RobotUserTeamDTO>> iter = robotUserTeamlist.iterator();
        Map.Entry<String, RobotUserTeamDTO> tmpEntry = null;
        while(iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }
}
