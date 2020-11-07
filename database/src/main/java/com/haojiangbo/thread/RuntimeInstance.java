package com.haojiangbo.thread;

import com.haojiangbo.protocol.config.ErrorCode;
import com.haojiangbo.protocol.proto.BaseMysqlPacket;
import com.haojiangbo.protocol.proto.ErrorPacket;
import com.haojiangbo.protocol.proto.OkPackert;
import com.haojiangbo.protocol.proto.ResultSetPacket;
import com.haojiangbo.router.SQLRouter;
import com.haojiangbo.utils.PathUtils;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;
import java.util.List;

@Slf4j
public class RuntimeInstance  implements Runnable{

    public RuntimeInstance(BaseMysqlPacket packet) {
        this.packet = packet;
        SQLRouter.setDbPath("E:/work/venomous_sting/proxy/");
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
        //SHOW DATABASES
        //此处没有设计数据库的概念 所以这块需要手动返回
        if("SHOW DATABASES".contains(command)
                || "SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME FROM information_schema.SCHEMATA".contains(command)){
            ResultSetPacket resultSetPacket = new ResultSetPacket();
            resultSetPacket.write(packet.context.channel(),packet);
        } else if("SHOW STATUS".equals(command)){
            ErrorPacket err = new ErrorPacket();
            err.packetId = (byte) (packet.packetNumber + 1);
            err.errcode = ErrorCode.ER_UNKNOWN_COM_ERROR;
            err.message = "Unknown command";
            err.write(packet.context.channel());
        } else {
            Object re =  SQLRouter.router(command);
            if((re instanceof  Boolean && re.equals(true))){

            }else if(re instanceof List){

            }else {
                OkPackert okPackert = new OkPackert();
                okPackert.success(packet.context, (byte) (packet.packetNumber + 1));
            }
        }
        ReferenceCountUtil.release(packet.payload);
    }
}
