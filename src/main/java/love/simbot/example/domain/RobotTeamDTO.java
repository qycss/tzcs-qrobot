package love.simbot.example.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RobotTeamDTO {

    private Long teamId;

    private String teamName;

    private String groupId;

    private String teamStatus;

    private String teamAmount;

    private String firstInInstances;

}
