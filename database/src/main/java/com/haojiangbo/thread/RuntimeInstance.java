package com.haojiangbo.thread;

import com.haojiangbo.protocol.config.ErrorCode;
import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import com.haojiangbo.protocol.proto.ErrorPacket;
import com.haojiangbo.protocol.proto.OkPackert;
import com.haojiangbo.protocol.proto.ResultSetPacket;
import com.haojiangbo.router.SQLRouter;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

@Slf4j
public class RuntimeInstance  implements Runnable{

    public RuntimeInstance(BaseMysqlPacket packet) {
        this.packet = packet;
        SQLRouter.setDbPath("D:/work/springCloudOnline/stinger/proxy/");
    }

    /**
     * 当前线程获取的packet
     */
    public static  final   ThreadLocal<BaseMysqlPacket> currentThreadPacket = new ThreadLocal<>();
    private BaseMysqlPacket packet;

    @Override
    public void run() {
        currentThreadPacket.set(this.packet);
        String command = packet.payload.toString(Charset.forName("utf-8"));
        log.info("QUERY == {}",command);
        SQLRouter.router(command);
        //SHOW DATABASES
        //此处没有设计数据库的概念 所以这块需要手动返回
        if("SHOW DATABASES".equals(command)
                || " SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA".equals(command)){
            ResultSetPacket resultSetPacket = new ResultSetPacket();
            resultSetPacket.write(packet.context.channel(),packet);
        } else if("SHOW STATUS".equals(command)){
            ErrorPacket err = new ErrorPacket();
            err.packetId = (byte) (packet.packetNumber + 1);
            err.errcode = ErrorCode.ER_UNKNOWN_COM_ERROR;
            err.message = "Unknown command";
            err.write(packet.context.channel());
        } else {
            OkPackert okPackert = new OkPackert();
            okPackert.success(packet.context, (byte) (packet.packetNumber + 1));
        }
        ReferenceCountUtil.release(packet.payload);
    }
}
