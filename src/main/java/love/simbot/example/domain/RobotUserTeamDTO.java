package love.simbot.example.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RobotUserTeamDTO {

    private Long teamId;

    private Long id;

    private String userType;

    private String userName;

    private String userCode;

}