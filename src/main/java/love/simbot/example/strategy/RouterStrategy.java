package love.simbot.example.strategy;

import love.forte.simbot.api.message.events.GroupMsg;
import love.forte.simbot.api.sender.Getter;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.api.sender.Setter;

public interface RouterStrategy {

    void router(GroupMsg groupMsg, Sender sender, Getter getter, Setter setter);

}
