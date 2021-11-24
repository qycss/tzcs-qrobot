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

    // 根据报名昵称查找报名qq
    String selectUserTeamUserCodeByUserName(String userName);

    // 删除团队的所有报名记录
    int deleteUserTeamByTeamId(Long teamId);
}
