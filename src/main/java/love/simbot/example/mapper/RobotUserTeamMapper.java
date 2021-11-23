package love.simbot.example.mapper;

import love.simbot.example.domain.RobotUserTeamDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RobotUserTeamMapper {

    List<RobotUserTeamDTO> selectOpenRobotUserTeamListByTeamId(Long teamId);

    // 添加报名记录
    int addRobotUserTeam(RobotUserTeamDTO robotUserTeamDTO);

    // 删除报名记录
    int deleteRobotUserTeam(String userCode, Long teamId);
}
