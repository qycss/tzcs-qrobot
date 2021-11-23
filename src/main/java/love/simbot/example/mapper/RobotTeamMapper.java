package love.simbot.example.mapper;

import love.simbot.example.domain.RobotTeamDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RobotTeamMapper {

    List<RobotTeamDTO> selectRobotTeamList(RobotTeamDTO robotTeamDTO);

    List<RobotTeamDTO> selectOpenRobotTeamByGroupId(String groupId);

    int openRobotTeam(RobotTeamDTO robotTeamDTO);

    int closeRobotTeam(RobotTeamDTO robotTeamDTO);

    int chooseRobotTeamFirstInInstances(RobotTeamDTO robotTeamDTO);

}
