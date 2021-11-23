package love.simbot.example.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RobotRouterDTO {

    private Long id;

    private String routerName;

    private String routerClass;

    private String remark;

}
