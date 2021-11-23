package love.simbot.example.mapper;

import love.simbot.example.domain.RobotAutomaticReplyDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface RobotAutomaticReplyMapper {

    List<RobotAutomaticReplyDTO> selectRobotRouterList(String robotRevice);

    int insertRobotAutomaticReply(RobotAutomaticReplyDTO robotAutomaticReplyDTO);

}