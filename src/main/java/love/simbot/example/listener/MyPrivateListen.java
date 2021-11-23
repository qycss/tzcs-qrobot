package love.simbot.example.listener;

import love.forte.common.ioc.annotation.Beans;
import love.forte.simbot.annotation.OnPrivate;
import love.forte.simbot.annotation.OnPrivateMsgRecall;
import love.forte.simbot.api.message.MessageContentBuilderFactory;
import love.forte.simbot.api.message.events.PrivateMsg;
import love.forte.simbot.api.message.events.PrivateMsgRecall;
import love.forte.simbot.api.sender.Sender;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 私聊消息监听的示例类。
 * 所有需要被管理的类都需要标注 {@link Beans} 注解。
 *
 * @author ForteScarlet
 */
@Service
public class MyPrivateListen {

    /**
     * 通过依赖注入获取一个 "消息正文构建器工厂"。
     */
    private String[][] members = {{"0", "0", "0", "0", "0"}, {"0", "0", "0", "0", "0"}, {"0", "0", "0", "0", "0"}, {"0", "0", "0", "0", "0"}, {"0", "0", "0", "0", "0"}};

    @Autowired
    private MessageContentBuilderFactory messageContentBuilderFactory;

    @Autowired
    private MyGroupListen myGroupListen;

    /**
     * 此监听函数监听一个私聊消息，并会复读这个消息，然后再发送一个表情。
     * 此方法上使用的是一个模板注解{@link OnPrivate}，
     * 其代表监听私聊。
     * 由于你监听的是私聊消息，因此参数中要有个 {@link PrivateMsg} 来接收这个消息实体。
     * <p>
     * 其次，由于你要“复读”这句话，因此你需要发送消息，
     * 因此参数中你需要一个 "消息发送器" {@link Sender}。
     * <p>
     * 当然，你也可以使用 {@link love.forte.simbot.api.sender.MsgSender}，
     * 然后 {@code msgSender.SENDER}.
     */
    @OnPrivate
    public void replyPrivateMsg1(PrivateMsg privateMsg, Sender sender) {

        if (privateMsg.getMsg().indexOf("团队列表") >= 0) {
            sender.sendPrivateMsg(privateMsg, "111");
//            MessageContent build = messageContentBuilderFactory.getMessageContentBuilder().image(ImageUtils.myGraphicsGeneration(myGroupListen.members)).build();
//            sender.sendPrivateMsg(privateMsg, build);
        }

    }

    @OnPrivateMsgRecall
    public void onPrivateMsgRecall(PrivateMsgRecall privateMsgRecall, Sender sender) {
        sender.sendPrivateMsg(privateMsgRecall, "111");
    }

}
