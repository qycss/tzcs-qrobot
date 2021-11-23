package love.simbot.example.mapper;

import love.simbot.example.domain.RobotRouterDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RobotRouterMapper {

    List<RobotRouterDTO> selectRobotRouterList();

    RobotRouterDTO selectRobotRouterByRouterName(String routerName);

    int insertRobotRouter(RobotRouterDTO robotRouterDTO);

    int updateRobotRouter(RobotRouterDTO robotRouterDTO);

}